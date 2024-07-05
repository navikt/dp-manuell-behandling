package no.nav.dagpenger.manuell.behandling.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.manuell.behandling.asUUID
import no.nav.dagpenger.manuell.behandling.avklaring.ArbeidIEØS
import no.nav.dagpenger.manuell.behandling.avklaring.AvklaringBehandling
import no.nav.dagpenger.manuell.behandling.avklaring.HattLukkedeSakerSiste8Uker
import no.nav.dagpenger.manuell.behandling.avklaring.InntektNesteKalendermåned
import no.nav.dagpenger.manuell.behandling.avklaring.JobbetUtenforNorge
import no.nav.dagpenger.manuell.behandling.avklaring.MuligGjenopptak
import no.nav.dagpenger.manuell.behandling.avklaring.SvangerskapsrelaterteSykepenger
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.repository.AvklaringRepository
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime

internal class VurderAvklaringMottak(
    rapidsconnection: RapidsConnection,
    private val avklaringRepository: AvklaringRepository,
) : River.PacketListener {
    init {
        River(rapidsconnection)
            .apply {
                validate { it.demandValue("@event_name", "NyAvklaring") }
                validate { it.requireKey("@id", "@opprettet") }
                validate { it.requireKey("avklaringId", "kode") }
                validate { it.requireKey("ident", "behandlingId", "søknadId") }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val avklaringKode = packet["kode"].asText()
        val avklaringId = packet["avklaringId"].asUUID()

        withLoggingContext(
            "avklaringId" to avklaringId.toString(),
            "kode" to avklaringKode,
            "behandlingId" to packet["behandlingId"].asText(),
            "søknadId" to packet["søknadId"].asText(),
        ) {
            val manuellBehandlingAvklaring =
                ManuellBehandlingAvklaring(
                    packet["@opprettet"].asLocalDateTime().toLocalDate(),
                    packet["@id"].asUUID(),
                    packet["ident"].asText(),
                    packet["søknadId"].asUUID(),
                    packet["behandlingId"].asUUID(),
                )
            // Do something
            val avklaring =
                when (avklaringKode) {
                    "SvangerskapsrelaterteSykepenger" -> SvangerskapsrelaterteSykepenger(avklaringId)
                    "EØSArbeid" -> ArbeidIEØS(avklaringId)
                    "HattLukkedeSakerSiste8Uker" -> HattLukkedeSakerSiste8Uker(avklaringId)
                    "MuligGjenopptak" -> MuligGjenopptak(avklaringId)
                    "InntektNesteKalendermåned" -> InntektNesteKalendermåned(avklaringId)
                    "JobbetUtenforNorge" -> JobbetUtenforNorge(avklaringId)
                    else -> return // En avklaring som må håndteres av noen andre. En saksbehandler for eksempel
                }
            val avklaringBehanding =
                AvklaringBehandling(
                    avklaring,
                    kode = avklaringKode,
                    behandlingId = packet["behandlingId"].asUUID(),
                    ident = packet["ident"].asText(),
                    søknadId = packet["søknadId"].asUUID(),
                    context = context,
                )

            avklaringRepository.lagre(avklaringBehanding, manuellBehandlingAvklaring)
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger { }
    }
}
