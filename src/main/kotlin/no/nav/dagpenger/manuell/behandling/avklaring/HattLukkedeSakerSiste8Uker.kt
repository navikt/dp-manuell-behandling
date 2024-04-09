package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov

internal val HattLukkedeSakerSiste8Uker = {
    Avklaring(
        begrunnelse = "Hatt lukkede saker siste 8 uker",
        behov = Behov.HarHattLukketSiste8Uker,
        varsel = Behandlingsvarsler.LUKKEDE_SAKER_SISTE_8_UKER,
    ) { hendelse ->
        hendelse.legacyBehov()
    }
}
