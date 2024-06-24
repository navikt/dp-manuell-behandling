package no.nav.dagpenger.manuell.behandling.avklaring

import java.util.UUID

internal val JobbetUtenforNorge = { id: UUID ->
    Avklaring(
        id = id,
        begrunnelse = "Arbeid utenfor Norge",
        behov = Behov.JobbetUtenforNorge,
        varsel = Behandlingsvarsler.EØS_ARBEID,
    ) { hendelse -> mapOf("InnsendtSøknadsId" to mapOf("urn" to "urn:soknadid:${hendelse.søknadId}")) }
}
