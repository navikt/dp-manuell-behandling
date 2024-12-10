package no.nav.dagpenger.manuell.behandling.avklaring

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

internal class VurderAvklaringMottak(
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
            val behov = avklaringerTilBehovRegister[avklaringKode]
            if (behov == null) {
                // En avklaring som må håndteres av noen andre. En saksbehandler for eksempel
                logger.info { "Avklaring med kode $avklaringKode er ikke behandlet" }
                return
            }

            packet["@event_name"] = "behov"
            packet["@behov"] = listOf(behov.name)
            packet["@behovId"] = avklaringId
            packet["@avklaringsbehov"] = true
            packet["Virkningstidspunkt"] = packet["@opprettet"].asLocalDateTime().toLocalDate()

            logger.info { "Publiserer informasjonbehov med behov ${behov.name} for avklaring $avklaringKode" }

            context.publish(packet.toJson())
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger { }
    }
}
