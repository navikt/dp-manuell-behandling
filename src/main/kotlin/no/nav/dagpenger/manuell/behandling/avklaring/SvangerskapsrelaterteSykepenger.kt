package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovHendelse
import no.nav.dagpenger.manuell.behandling.mottak.booleanLøsningstolk

internal class SvangerskapsrelaterteSykepenger : Avklaring("Har hatt sykepenger som kan være svangerskapsrelatert") {
    private val behov = Behov.SykepengerSiste36Måneder

    override fun behandle(hendelse: ManuellBehandlingAvklaring) {
        hendelse.behov(
            behov,
            "Trenger informasjon om sykepenger",
            hendelse.legacyBehov(),
        )
    }

    override fun behandle(hendelse: LøstBehovHendelse) {
        if (hendelse.behov != behov) return
        if (vurdert()) return
        utfall = hendelse.utfall
        if (utfall == Utfall.Manuell) hendelse.varsel(Behandlingsvarsler.SVANGERSKAPSRELATERTE_SYKEPENGER)
    }

    companion object {
        internal val Løsningstolk = booleanLøsningstolk
    }
}
