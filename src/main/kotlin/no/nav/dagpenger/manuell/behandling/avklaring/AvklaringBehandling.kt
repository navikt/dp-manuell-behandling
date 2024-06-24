package no.nav.dagpenger.manuell.behandling.avklaring

import java.util.UUID

internal data class AvklaringBehandling(
    val avklaring: Avklaring,
    val kode: String,
    val behandlingId: UUID,
    val ident: String,
)
