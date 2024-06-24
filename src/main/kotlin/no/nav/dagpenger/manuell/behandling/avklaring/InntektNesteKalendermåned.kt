package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov
import java.util.UUID

internal val InntektNesteKalendermåned = { id: UUID ->
    Avklaring(
        id = id,
        begrunnelse = "Har innrapport inntekt for neste måned",
        behov = Behov.HarRapportertInntektNesteMåned,
        varsel = Behandlingsvarsler.INNTEKT_NESTE_KALENDERMÅNED,
    ) { hendelse ->
        hendelse.legacyBehov()
    }
}
