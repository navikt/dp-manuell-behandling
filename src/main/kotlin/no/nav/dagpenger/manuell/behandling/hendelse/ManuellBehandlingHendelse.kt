package no.nav.dagpenger.manuell.behandling.hendelse

import java.time.LocalDateTime
import java.util.UUID

internal abstract class ManuellBehandlingHendelse(
    val manuellBehandlingId: UUID,
    opprettet: LocalDateTime,
    meldingsreferanseId: UUID,
    ident: String,
) : PersonHendelse(meldingsreferanseId, ident, opprettet)
