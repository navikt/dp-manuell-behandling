package no.nav.dagpenger.manuell.behandling.repository

import no.nav.dagpenger.manuell.behandling.avklaring.Avklaring
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring

internal interface AvklaringRepository {
    fun lagre(
        avklaring: Avklaring,
        manuellBehandlingAvklaring: ManuellBehandlingAvklaring,
    )
}
