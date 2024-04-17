package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov

internal val HattLukkedeSakerSiste8Uker = {
    Behovavklaring(
        begrunnelse = "Hatt lukkede saker siste 8 uker",
        varsel = Behandlingsvarsler.LUKKEDE_SAKER_SISTE_8_UKER,
        behov = Behov.HarHattLukketSiste8Uker,
    ) { hendelse ->
        hendelse.legacyBehov()
    }
}
