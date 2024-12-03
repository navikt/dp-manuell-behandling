package no.nav.dagpenger.manuell.behandling.hendelse

import no.nav.dagpenger.manuell.behandling.avklaring.Behov
import no.nav.dagpenger.manuell.behandling.mottak.Utfall
import java.util.UUID

internal class LøstBehovHendelse(
    manuellVurderingId: UUID,
    val behov: Behov,
    val utfall: Utfall,
    meldingsreferanseId: UUID,
    ident: String,
) : ManuellBehandlingHendelse(manuellVurderingId, meldingsreferanseId, ident)
