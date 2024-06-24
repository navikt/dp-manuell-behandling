package no.nav.dagpenger.manuell.behandling

import mu.withLoggingContext
import no.nav.dagpenger.manuell.behandling.avklaring.ArbeidIEØS
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
    val avklaringRepository: AvklaringRepository,
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
                    "ArbeidIEØS" -> ArbeidIEØS(avklaringId)
                    "HattLukkedeSakerSiste8Uker" -> HattLukkedeSakerSiste8Uker(avklaringId)
                    "MuligGjenopptak" -> MuligGjenopptak(avklaringId)
                    "InntektNesteKalendermåned" -> InntektNesteKalendermåned(avklaringId)
                    "JobbetUtenforNorge" -> JobbetUtenforNorge(avklaringId)
                    else -> {
                        throw IllegalArgumentException("Ukjent avklaringkode $avklaringKode")
                    }
                }
            avklaringRepository.lagre(avklaring, manuellBehandlingAvklaring)
        }
    }
}
