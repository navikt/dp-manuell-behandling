package no.nav.dagpenger.manuell.behandling.hendelse

import java.util.UUID

internal abstract class SøknadHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val søknadId: UUID,
) : PersonHendelse(meldingsreferanseId, ident) {
    abstract val behandlingId: UUID
}
