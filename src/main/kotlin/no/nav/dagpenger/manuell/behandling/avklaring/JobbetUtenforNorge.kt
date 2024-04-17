package no.nav.dagpenger.manuell.behandling.avklaring

internal val JobbetUtenforNorge = {
    Behovavklaring(
        "Arbeid utenfor Norge",
        Behandlingsvarsler.EØS_ARBEID,
        Behov.JobbetUtenforNorge,
    ) { hendelse -> mapOf("InnsendtSøknadsId" to mapOf("urn" to "urn:soknadid:${hendelse.søknadId}")) }
}
