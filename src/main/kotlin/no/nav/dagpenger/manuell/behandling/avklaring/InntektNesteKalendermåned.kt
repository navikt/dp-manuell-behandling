package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov

internal val InntektNesteKalendermåned = {
    Behovavklaring(
        "Har innrapport inntekt for neste måned",
        Behandlingsvarsler.INNTEKT_NESTE_KALENDERMÅNED,
        Behov.HarRapportertInntektNesteMåned,
    ) { hendelse ->
        hendelse.legacyBehov()
    }
}
