package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.avklaring.Behov.EØSArbeid

internal val ArbeidIEØS = {
    Behovavklaring(
        begrunnelse = "Arbeid i EØS",
        varsel = Behandlingsvarsler.EØS_ARBEID,
        behov = EØSArbeid,
    ) { hendelse -> mapOf("InnsendtSøknadsId" to mapOf("urn" to "urn:soknadid:${hendelse.søknadId}")) }
}
