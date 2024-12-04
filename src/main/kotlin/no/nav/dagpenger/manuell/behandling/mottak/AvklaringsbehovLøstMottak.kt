package no.nav.dagpenger.manuell.behandling.mottak

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.manuell.behandling.Metrikker.avklaringTeller
import no.nav.dagpenger.manuell.behandling.Metrikker.avklaringTidBrukt
import no.nav.dagpenger.manuell.behandling.asUUID
import no.nav.dagpenger.manuell.behandling.avklaring.Behov
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

internal class AvklaringsbehovLøstMottak(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    private val muligeBehov = Behov.entries.map { it.name }

    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    it.requireValue("@event_name", "behov")
                    it.requireValue("@avklaringsbehov", true)
                    it.requireKey("@løsning")
                    it.forbidValue("@final", true) // Ignorerer final behov fra behovsakkumulator
                    it.requireAllOrAny("@behov", muligeBehov)
                }
                validate {
                    it.requireKey("avklaringId", "@behovId", "behandlingId")
                    it.requireKey("ident")
                    it.requireKey("kode")
                    it.interestedIn("@id", "@opprettet")
                }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val avklaringId = packet["avklaringId"].asUUID()
        val ident = packet["ident"].asText()
        val kode = packet["kode"].asText()
        val behandlingId = packet["behandlingId"].asText()
        val behov = packet["@behov"].map { it.asText() }.map { Behov.valueOf(it) }
        withLoggingContext(
            "behovId" to packet["@behovId"].asUUID().toString(),
            "avklaringId" to avklaringId.toString(),
        ) {
            try {
                logger.info { "Mottok løsning på behov: $behov" }
                sikkerlogg.info { "Mottok løsning på behov: $behov. Pakke: ${packet.toJson()}" }
                behov.forEach { behov ->
                    val utfall = packet.utfall(behov)
                    if (utfall == Utfall.Automatisk) {
                        context.publish(
                            ident,
                            AvklaringIkkeRelevant(
                                avklaringId = avklaringId,
                                kode = kode,
                                behandlingId = behandlingId,
                                ident = ident,
                            ).toJson(),
                        )
                        logger.info { "Publisert AvklaringIkkeRelevant for avklaring $kode" }
                    } else {
                        logger.info { "Avklaring $kode må sjekkes, har tilstand $utfall" }
                    }
                    loggAvklaring(avklaringId, kode, utfall, packet)
                }
            } catch (e: Exception) {
                sikkerlogg.error(e) { "Feil ved mottak av løsning. Packet=${packet.toJson()}" }
                throw e
            }
        }
    }

    private fun loggAvklaring(
        avklaringId: UUID,
        kode: String?,
        utfall: Utfall,
        packet: JsonMessage,
    ) {
        logger.info { "Avklaring med id=$avklaringId, kode=$kode løst med utfall=$utfall" }
        avklaringTeller.labelValues(kode, utfall.name).inc()
        val tidBrukt = Duration.between(packet["@opprettet"].asLocalDateTime(), LocalDateTime.now())
        avklaringTidBrukt.labelValues(kode).observe(tidBrukt.toMillis().toDouble())
    }

    private fun JsonMessage.utfall(behov: Behov): Utfall {
        return this.svar(behov).utfall(behov)
    }

    private fun JsonMessage.svar(behov: Behov): JsonNode =
        if (this["@løsning"][behov.name].isBoolean) {
            this["@løsning"][behov.name]
        } else {
            this["@løsning"][behov.name]["verdi"]
        }

    private fun JsonNode.utfall(behov: Behov): Utfall =
        when (behov) {
            Behov.EØSArbeid -> booleanLøsningstolk.tolk(this)
            Behov.HarHattDagpengerSiste13Mnd -> booleanLøsningstolk.tolk(this)
            Behov.HarHattLukketSiste8Uker -> booleanLøsningstolk.tolk(this)
            Behov.HarRapportertInntektNesteMåned -> booleanLøsningstolk.tolk(this)
            Behov.JobbetUtenforNorge -> booleanLøsningstolk.tolk(this)
            Behov.SykepengerSiste36Måneder -> booleanLøsningstolk.tolk(this)
        }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
        metadata: MessageMetadata,
    ) {
        throw IllegalStateException("Forventer ikke feil her ${problems.toExtendedReport()}")
    }

    private data class AvklaringIkkeRelevant(
        val avklaringId: UUID,
        val kode: String,
        val behandlingId: String,
        val ident: String,
    ) {
        fun toJson() =
            JsonMessage
                .newMessage(
                    "AvklaringIkkeRelevant",
                    mutableMapOf(
                        "avklaringId" to avklaringId,
                        "kode" to kode,
                        "behandlingId" to behandlingId,
                        "ident" to ident,
                    ),
                ).toJson()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }
}
