package no.nav.dagpenger.manuell.behandling.repository

import no.nav.dagpenger.manuell.behandling.avklaring.AvklaringBehandling
import no.nav.dagpenger.manuell.behandling.avklaring.Utfall
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import java.util.UUID

internal interface AvklaringRepository {
    fun lagre(
        avklaring: AvklaringBehandling,
        manuellBehandlingAvklaring: ManuellBehandlingAvklaring,
    )

    fun løsning(
        avklaringId: UUID,
        utfall: Utfall,
    )
}
