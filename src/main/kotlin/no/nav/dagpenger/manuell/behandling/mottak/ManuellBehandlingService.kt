package no.nav.dagpenger.manuell.behandling.mottak

import mu.KotlinLogging
import no.nav.dagpenger.manuell.behandling.Mediator
import no.nav.dagpenger.manuell.behandling.asUUID
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDate
import java.util.UUID

internal class ManuellBehandlingService(rapidsConnection: RapidsConnection, private val mediator: Mediator) :
    River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAllOrAny("@behov", listOf("AvklaringManuellBehandling")) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("ident") }
            // TODO: validate { it.requireKey("Søknadsdato???") }
            validate { it.requireKey("søknadId") }
            validate { it.interestedIn("@id", "@opprettet") }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        logger.info { "Mottok behov for manuell behandling" }
        val hendelse = ManuellBehandlingMessage(packet).hendelse()
        mediator.håndter(hendelse)
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
    }
}

private class ManuellBehandlingMessage(private val packet: JsonMessage) {
    private val meldingsreferanseId: UUID get() = packet["@id"].asText().let(UUID::fromString)
    private val behandlingsdato: LocalDate get() = LocalDate.now() // TODO: packet["Søknadsdato"].asLocalDate()
    private val ident: String get() = packet["ident"].asText()
    private val søknadId: UUID get() = packet["søknadId"].asUUID()

    fun hendelse() = ManuellBehandlingAvklaring(behandlingsdato, meldingsreferanseId, ident, søknadId)
}
