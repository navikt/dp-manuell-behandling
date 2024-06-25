package no.nav.dagpenger.manuell.behandling.repository

import mu.KotlinLogging
import no.nav.dagpenger.manuell.behandling.avklaring.AvklaringBehandling
import no.nav.dagpenger.manuell.behandling.avklaring.Utfall
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

internal class InMemoryAvklaringRepository(
    private val rapidsConnection: RapidsConnection,
) : AvklaringRepository {
    private val avklaringer = mutableMapOf<UUID, AvklaringBehandling>()

    override fun lagre(
        avklaring: AvklaringBehandling,
        manuellBehandlingAvklaring: ManuellBehandlingAvklaring,
    ) {
        avklaringer[avklaring.avklaring.id] = avklaring
        rapidsConnection.publish(avklaring.ident, avklaring.avklaring.lagInformasjonsbehov(manuellBehandlingAvklaring))
    }

    override fun løsning(
        avklaringId: UUID,
        utfall: Utfall,
    ) {
        val avklaring =
            avklaringer.getOrElse(avklaringId) {
                logger.warn("Fant ikke avklaring med id=$avklaringId. Venter på ${avklaringer.size} avklaringer")
                return
            }
        avklaring.avklaring.utfall = utfall

        if (utfall == Utfall.Automatisk) {
            rapidsConnection.publish(
                avklaring.ident,
                JsonMessage
                    .newMessage(
                        "AvklaringIkkeRelevant",
                        mutableMapOf(
                            "avklaringId" to avklaringId.toString(),
                            "kode" to avklaring.kode,
                            "behandlingId" to avklaring.behandlingId.toString(),
                            "ident" to avklaring.ident,
                        ),
                    ).toJson(),
            )
        }

        avklaringer.remove(avklaringId)
    }

    private companion object {
        private val logger = KotlinLogging.logger { }
    }
}
