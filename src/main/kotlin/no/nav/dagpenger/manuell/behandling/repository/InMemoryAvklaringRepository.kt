package no.nav.dagpenger.manuell.behandling.repository

import no.nav.dagpenger.manuell.behandling.avklaring.Avklaring
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

internal class InMemoryAvklaringRepository(
    val rapidsConnection: RapidsConnection,
) : AvklaringRepository {
    internal val avklaringer = mutableMapOf<UUID, Avklaring>()

    override fun lagre(
        avklaring: Avklaring,
        manuellBehandlingAvklaring: ManuellBehandlingAvklaring,
    ) {
        avklaringer[avklaring.id] = avklaring
        rapidsConnection.publish(avklaring.lagInformasjonsbehov(manuellBehandlingAvklaring))
    }
}
