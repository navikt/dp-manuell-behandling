package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov

internal val SvangerskapsrelaterteSykepenger =
    Avklaring(
        begrunnelse = "Har hatt sykepenger som kan være svangerskapsrelatert",
        behov = Behov.SykepengerSiste36Måneder,
        varsel = Behandlingsvarsler.SVANGERSKAPSRELATERTE_SYKEPENGER,
    ) { hendelse ->
        hendelse.legacyBehov()
    }
