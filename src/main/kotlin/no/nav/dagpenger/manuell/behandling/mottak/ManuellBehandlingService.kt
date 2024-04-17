package no.nav.dagpenger.manuell.behandling.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.manuell.behandling.Mediator
import no.nav.dagpenger.manuell.behandling.asUUID
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
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
            validate { it.requireKey("behandlingId") }
            validate { it.requireKey("Virkningstidspunkt") }
            validate { it.requireKey("søknadId") }
            validate { it.interestedIn("@id", "@opprettet", "@behovId") }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val message = ManuellBehandlingMessage(packet)
        withLoggingContext(
            "søknadId" to message.søknadId.toString(),
            "behovId" to packet["@behovId"].asUUID().toString(),
        ) {
            logger.info { "Mottok behov for manuell behandling" }
            val hendelse = message.hendelse()
            mediator.håndter(hendelse)
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
    }
}

private class ManuellBehandlingMessage(private val packet: JsonMessage) {
    private val meldingsreferanseId: UUID get() = packet["@id"].asText().let(UUID::fromString)
    private val behandlingsdato: LocalDate get() = packet["Virkningstidspunkt"].asLocalDate()
    private val ident: String get() = packet["ident"].asText()
    val søknadId: UUID get() = packet["søknadId"].asUUID()
    private val behandlingId: UUID get() = packet["behandlingId"].asUUID()

    private val opprettet = packet["@opprettet"].asLocalDateTime()

    fun hendelse() =
        ManuellBehandlingAvklaring(
            behandlingsdato,
            meldingsreferanseId,
            ident,
            søknadId,
            behandlingId,
            opprettet,
        )
}
