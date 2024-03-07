package no.nav.dagpenger.manuell.behandling.hendelse

import java.util.UUID

internal abstract class ManuellBehandlingHendelse(
    val manuellBehandlingId: UUID,
    meldingsreferanseId: UUID,
    ident: String,
) : PersonHendelse(meldingsreferanseId, ident)
