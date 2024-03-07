package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovHendelse
import no.nav.dagpenger.manuell.behandling.mottak.booleanLøsningstolk

internal class JobbetUtenforNorge : Avklaring("Arbeid utenfor Norge") {
    private val behov = Behov.JobbetUtenforNorge

    override fun behandle(hendelse: ManuellBehandlingAvklaring) {
        hendelse.behov(
            behov,
            "Trenger informasjon om arbeid utenfor Norge",
            mapOf(
                "InnsendtSøknadsId" to hendelse.søknadId,
            ),
        )
    }

    override fun behandle(hendelse: LøstBehovHendelse) {
        if (hendelse.behov != behov) return
        if (vurdert()) return
        utfall = hendelse.utfall
        if (utfall == Utfall.Manuell) hendelse.varsel(Behandlingsvarsler.EØS_ARBEID)
    }

    companion object {
        internal val Løsningstolk = booleanLøsningstolk
    }
}
