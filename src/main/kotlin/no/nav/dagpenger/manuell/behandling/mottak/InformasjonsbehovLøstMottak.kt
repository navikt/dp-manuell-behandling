package no.nav.dagpenger.manuell.behandling.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.manuell.behandling.asUUID
import no.nav.dagpenger.manuell.behandling.avklaring.Behov
import no.nav.dagpenger.manuell.behandling.repository.AvklaringRepository
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class InformasjonsbehovLøstMottak(
    rapidsConnection: RapidsConnection,
    private val avklaringRepository: AvklaringRepository,
) : River.PacketListener {
    private val muligeBehov = Behov.entries.map { it.name }

    init {
        River(rapidsConnection)
            .apply {
                validate { it.demandValue("@event_name", "behov") }
                validate { it.demandAllOrAny("@behov", muligeBehov) }
                validate { it.requireKey("avklaringId", "@behovId") }
                validate { it.requireKey("@løsning") }
                validate { it.rejectValue("@final", true) } // Ignorerer final behov fra behovsakkumulator
                validate { it.interestedIn("@id", "@opprettet") }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val avklaringId = packet["avklaringId"].asUUID()
        val behov = packet["@behov"].map { it.asText() }
        withLoggingContext(
            "behovId" to packet["@behovId"].asUUID().toString(),
            "avklaringId" to avklaringId.toString(),
        ) {
            try {
                logger.info { "Mottok løsning på behov: $behov" }
                sikkerlogg.info { "Mottok løsning på behov: $behov. Pakke: ${packet.toJson()}" }
                behov.forEach { løsning ->
                    avklaringRepository.løsning(avklaringId, booleanLøsningstolk.tolk(packet["@løsning"][løsning]))
                }
            } catch (e: Exception) {
                sikkerlogg.error(e) { "Feil ved mottak av løsning. Packet=${packet.toJson()}" }
                throw e
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }
}
