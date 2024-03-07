package no.nav.dagpenger.manuell.behandling.mottak

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
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
import no.nav.dagpenger.manuell.behandling.hendelse.SøknadHendelse
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
            validate { it.requireKey("ident", "søknadId") }
            validate { it.requireKey("@løsning") }
            validate { it.interestedIn("@id", "@opprettet") }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        logger.info { "Mottok løsning på behov: ${packet["@behov"].map { it.asText() }}" }
        try {
            /*val hendelser = BehovMessage(packet).hendelse()
            hendelser.forEach { hendelse ->
                mediator.håndter(hendelse)
            }*/
        } catch (e: Exception) {
            sikkerlogg.error(e) { "Feil ved mottak av løsning. Packet=${packet.toJson()}" }
            throw e
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }
}

internal class BehovMessage(private val packet: JsonMessage) {
    private val løsteBehov = packet["@løsning"].fields().asSequence().map { Behov.valueOf(it.key) }.toList()
    private val meldingsreferanseId: UUID = packet["@id"].asText().let(UUID::fromString)
    private val ident: String = packet["ident"].asText()
    private val søknadId: UUID = packet["søknadId"].asUUID()

    private fun utfall(løsning: Behov) =
        when (løsning) {
            Behov.EØSArbeid -> ArbeidIEØS.Løsningstolk
            Behov.HarHattLukketSiste8Uker -> HattLukkedeSakerSiste8Uker.Løsningstolk
            Behov.HarRapportertInntektNesteMåned -> InntektNesteKalendermåned.Løsningstolk
            Behov.SykepengerSiste36Måneder -> SvangerskapsrelaterteSykepenger.Løsningstolk
            Behov.HarHattDagpengerSiste13Mnd -> MuligGjenopptak.Løsningstolk
            Behov.JobbetUtenforNorge -> JobbetUtenforNorge.Løsningstolk
        }.tolk(packet["@løsning"][løsning.name])

    fun hendelse() =
        løsteBehov.map { løstBehov ->
            LøstBehovHendelse(løstBehov, utfall(løstBehov), meldingsreferanseId, ident, søknadId)
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
    val behov: Behov,
    val utfall: Utfall,
    meldingsreferanseId: UUID,
    ident: String,
    søknadId: UUID,
) : SøknadHendelse(meldingsreferanseId, ident, søknadId)
