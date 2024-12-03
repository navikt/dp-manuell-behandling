package no.nav.dagpenger.manuell.behandling.mottak

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.manuell.behandling.asUUID
import no.nav.dagpenger.manuell.behandling.avklaring.Behov

internal class NyAvklaringMottak(
    rapidsconnection: RapidsConnection,
) : River.PacketListener {
    init {
        River(rapidsconnection)
            .apply {
                precondition {
                    it.requireValue("@event_name", "NyAvklaring")
                }
                validate {
                    it.requireKey("@id", "@opprettet")
                    it.requireKey("avklaringId", "kode")
                    it.requireKey("ident", "behandlingId", "søknadId")
                }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val avklaringKode = packet["kode"].asText()
        val avklaringId = packet["avklaringId"].asUUID()

        withLoggingContext(
            "avklaringId" to avklaringId.toString(),
            "kode" to avklaringKode,
            "behandlingId" to packet["behandlingId"].asText(),
            "søknadId" to packet["søknadId"].asText(),
        ) {
            val behov =
                when (avklaringKode) {
                    "SvangerskapsrelaterteSykepenger" -> Behov.SykepengerSiste36Måneder
                    "EØSArbeid" -> Behov.EØSArbeid
                    "HattLukkedeSakerSiste8Uker" -> Behov.HarHattLukketSiste8Uker
                    "MuligGjenopptak" -> Behov.HarHattDagpengerSiste13Mnd
                    "InntektNesteKalendermåned" -> Behov.HarRapportertInntektNesteMåned
                    "JobbetUtenforNorge" -> Behov.JobbetUtenforNorge
                    else -> {
                        // En avklaring som må håndteres av noen andre. En saksbehandler for eksempel
                        logger.info { "Avklaring med kode $avklaringKode er behandlet ikke her" }
                        return
                    }
                }

            packet["@event_name"] = "behov"
            packet["@behov"] = listOf(behov.name)
            packet["@behovId"] = avklaringId
            packet["@avklaringsbehov"] = true
            packet.legacyParams().forEach { (nøkkel, verdi) ->
                packet[nøkkel] = verdi
            }

            logger.info { "Publiserer informasjonbehov med behov ${behov.name} for avklaring $avklaringKode" }

            context.publish(packet.toJson())
        }
    }

    private fun JsonMessage.legacyParams() =
        mapOf(
            "Virkningstidspunkt" to this["@opprettet"].asLocalDateTime().toLocalDate(),
            "søknad_uuid" to this["søknadId"].asUUID(),
            "identer" to
                listOf(
                    mapOf(
                        "type" to "folkeregisterident",
                        "historisk" to false,
                        "id" to this["ident"].asText(),
                    ),
                ),
        )

    private companion object {
        private val logger = KotlinLogging.logger { }
    }
}