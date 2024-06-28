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
        val behov = packet["@behov"].map { it.asText() }.map { Behov.valueOf(it) }
        withLoggingContext(
            "behovId" to packet["@behovId"].asUUID().toString(),
            "avklaringId" to avklaringId.toString(),
        ) {
            try {
                logger.info { "Mottok løsning på behov: $behov" }
                sikkerlogg.info { "Mottok løsning på behov: $behov. Pakke: ${packet.toJson()}" }
                behov.forEach { behov ->
                    val svar =
                        if (packet["@løsning"][behov.name].isBoolean) {
                            packet["@løsning"][behov.name]
                        } else {
                            packet["@løsning"][behov.name]["verdi"]
                        }
                    val løsning =
                        when (behov) {
                            Behov.EØSArbeid -> booleanLøsningstolk.tolk(svar)
                            Behov.HarHattDagpengerSiste13Mnd -> booleanLøsningstolk.tolk(svar)
                            Behov.HarHattLukketSiste8Uker -> booleanLøsningstolk.tolk(svar)
                            Behov.HarRapportertInntektNesteMåned -> booleanLøsningstolk.tolk(svar)
                            Behov.JobbetUtenforNorge -> booleanLøsningstolk.tolk(svar)
                            Behov.SykepengerSiste36Måneder -> booleanLøsningstolk.tolk(svar)
                        }

                    avklaringRepository.løsning(avklaringId, løsning)
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
