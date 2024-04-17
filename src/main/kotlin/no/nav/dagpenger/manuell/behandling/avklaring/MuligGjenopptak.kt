package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov

internal val MuligGjenopptak = {
    Behovavklaring(
        begrunnelse = "Mulig gjenopptak",
        behov = Behov.HarHattDagpengerSiste13Mnd,
        varsel = Behandlingsvarsler.MULIG_GJENOPPTAK,
    ) { hendelse ->
        hendelse.legacyBehov()
    }
}
