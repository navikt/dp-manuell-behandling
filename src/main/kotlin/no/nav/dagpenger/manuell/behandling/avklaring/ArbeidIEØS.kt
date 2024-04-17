package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.avklaring.Behov.EØSArbeid

internal val ArbeidIEØS = {
    Avklaring(
        begrunnelse = "Arbeid i EØS",
        behov = EØSArbeid,
        varsel = Behandlingsvarsler.EØS_ARBEID,
    ) { hendelse -> mapOf("InnsendtSøknadsId" to mapOf("urn" to "urn:soknadid:${hendelse.søknadId}")) }
}
