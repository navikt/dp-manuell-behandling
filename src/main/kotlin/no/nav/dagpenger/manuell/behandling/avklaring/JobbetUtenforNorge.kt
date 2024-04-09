package no.nav.dagpenger.manuell.behandling.avklaring

internal val JobbetUtenforNorge = {
    Avklaring(
        "Arbeid utenfor Norge",
        Behov.JobbetUtenforNorge,
        Behandlingsvarsler.EØS_ARBEID,
    ) { hendelse -> mapOf("InnsendtSøknadsId" to mapOf("urn" to "urn:soknadid:${hendelse.søknadId}")) }
}
