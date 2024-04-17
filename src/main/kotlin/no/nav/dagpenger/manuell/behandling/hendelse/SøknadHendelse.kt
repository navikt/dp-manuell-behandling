package no.nav.dagpenger.manuell.behandling.hendelse

import java.time.LocalDateTime
import java.util.UUID

internal abstract class SøknadHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val søknadId: UUID,
    opprettet: LocalDateTime,
) : PersonHendelse(meldingsreferanseId, ident, opprettet) {
    abstract val behandlingId: UUID
}
