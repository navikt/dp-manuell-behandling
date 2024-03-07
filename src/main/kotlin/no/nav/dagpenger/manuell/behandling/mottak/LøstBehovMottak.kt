package no.nav.dagpenger.manuell.behandling.mottak

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.manuell.behandling.Mediator
import no.nav.dagpenger.manuell.behandling.asUUID
import no.nav.dagpenger.manuell.behandling.avklaring.ArbeidIEØS
import no.nav.dagpenger.manuell.behandling.avklaring.Behov
import no.nav.dagpenger.manuell.behandling.avklaring.HattLukkedeSakerSiste8Uker
import no.nav.dagpenger.manuell.behandling.avklaring.InntektNesteKalendermåned
import no.nav.dagpenger.manuell.behandling.avklaring.JobbetUtenforNorge
import no.nav.dagpenger.manuell.behandling.avklaring.MuligGjenopptak
import no.nav.dagpenger.manuell.behandling.avklaring.SvangerskapsrelaterteSykepenger
import no.nav.dagpenger.manuell.behandling.avklaring.Utfall
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class LøstBehovMottak(rapidsConnection: RapidsConnection, private val mediator: Mediator) :
    River.PacketListener {
    private val muligeBehov = Behov.entries.map { it.name }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAllOrAny("@behov", muligeBehov) }
            validate { it.requireKey("ident", "manuellBehandlingId", "@behovId") }
            validate { it.requireKey("@løsning") }
            validate { it.rejectValue("@final", true) } // Ignorerer final behov fra behovsakkumulator
            validate { it.interestedIn("@id", "@opprettet") }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val behovMessage = BehovMessage(packet)
        withLoggingContext(
            "behovId" to packet["@behovId"].asUUID().toString(),
            "manuellBehandlingId" to behovMessage.manuellBehandlingId.toString(),
        ) {
            try {
                logger.info { "Mottok løsning på behov: ${behovMessage.løsteBehov}" }
                val hendelser = behovMessage.hendelser()
                hendelser.forEach { hendelse ->
                    mediator.håndter(hendelse)
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

internal class BehovMessage(private val packet: JsonMessage) {
    internal val løsteBehov = packet["@løsning"].fields().asSequence().map { Behov.valueOf(it.key) }.toList()
    private val meldingsreferanseId: UUID = packet["@id"].asText().let(UUID::fromString)
    private val ident: String = packet["ident"].asText()
    internal val manuellBehandlingId: UUID = packet["manuellBehandlingId"].asUUID()

    private fun utfall(løsning: Behov) =
        when (løsning) {
            Behov.EØSArbeid -> ArbeidIEØS.Løsningstolk
            Behov.HarHattLukketSiste8Uker -> HattLukkedeSakerSiste8Uker.Løsningstolk
            Behov.HarRapportertInntektNesteMåned -> InntektNesteKalendermåned.Løsningstolk
            Behov.SykepengerSiste36Måneder -> SvangerskapsrelaterteSykepenger.Løsningstolk
            Behov.HarHattDagpengerSiste13Mnd -> MuligGjenopptak.Løsningstolk
            Behov.JobbetUtenforNorge -> JobbetUtenforNorge.Løsningstolk
        }.tolk(packet["@løsning"][løsning.name])

    fun hendelser() =
        løsteBehov.map { løstBehov ->
            LøstBehovHendelse(manuellBehandlingId, løstBehov, utfall(løstBehov), meldingsreferanseId, ident)
        }
}

internal fun interface Løsningstolk {
    fun tolk(løsning: JsonNode): Utfall
}

// En standard tolk som funker for det mestee
internal val booleanLøsningstolk =
    Løsningstolk { løsning ->
        when (løsning.asBoolean()) {
            true -> Utfall.Manuell
            false -> Utfall.Automatisk
        }
    }

internal class LøstBehovHendelse(
    manuellVurderingId: UUID,
    val behov: Behov,
    val utfall: Utfall,
    meldingsreferanseId: UUID,
    ident: String,
) : ManuellBehandlingHendelse(manuellVurderingId, meldingsreferanseId, ident)
