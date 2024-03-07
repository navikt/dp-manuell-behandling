package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov

internal val InntektNesteKalendermåned =
    Avklaring(
        "Har innrapport inntekt for neste måned",
        Behov.HarRapportertInntektNesteMåned,
        Behandlingsvarsler.INNTEKT_NESTE_KALENDERMÅNED,
    ) { hendelse ->
        hendelse.legacyBehov()
    }
