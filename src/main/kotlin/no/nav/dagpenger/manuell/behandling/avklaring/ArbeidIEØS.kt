package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.avklaring.Behov.EØSArbeid
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovHendelse
import no.nav.dagpenger.manuell.behandling.mottak.booleanLøsningstolk

internal class ArbeidIEØS : Avklaring("Arbeid i EØS") {
    private val behov = EØSArbeid

    override fun behandle(hendelse: ManuellBehandlingAvklaring) {
        hendelse.behov(
            behov,
            "Trenger informasjon om arbeid i EØS",
            mapOf(
                "InnsendtSøknadsId" to hendelse.søknadId,
            ),
        )
    }

    override fun behandle(hendelse: LøstBehovHendelse) {
        if (hendelse.behov != behov) return
        if (vurdert()) return
        utfall = hendelse.utfall
        hendelse.varsel(Behandlingsvarsler.EØS_ARBEID)
    }

    companion object {
        internal val Løsningstolk = booleanLøsningstolk
    }
}
