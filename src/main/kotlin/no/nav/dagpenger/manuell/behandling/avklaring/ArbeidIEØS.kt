package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.avklaring.Behov.EØSArbeid
import java.util.UUID

internal val ArbeidIEØS = { id: UUID ->
    Avklaring(
        id = id,
        begrunnelse = "Arbeid i EØS",
        behov = EØSArbeid,
        varsel = Behandlingsvarsler.EØS_ARBEID,
    ) { hendelse -> mapOf("InnsendtSøknadsId" to mapOf("urn" to "urn:soknadid:${hendelse.søknadId}")) }
}
