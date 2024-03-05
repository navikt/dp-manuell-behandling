package no.nav.dagpenger.manuell.behandling.modell

import no.nav.dagpenger.manuell.behandling.avklaring.Avklaring
import java.util.UUID

internal interface ManuellBehandlingObserver {
    fun vurderingAvklart(event: ManuellBehandlingAvklart)

    data class ManuellBehandlingAvklart(
        val behandlesManuelt: Boolean,
        val ident: String,
        val søknadId: UUID,
        val vurderinger: List<Avklaring> = emptyList(),
    )
}
