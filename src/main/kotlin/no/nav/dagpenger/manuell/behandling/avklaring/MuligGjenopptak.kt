package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovHendelse
import no.nav.dagpenger.manuell.behandling.mottak.booleanLøsningstolk

internal class MuligGjenopptak : Avklaring("Mulig gjenopptak") {
    private val behov = Behov.HarHattDagpengerSiste13Mnd

    override fun behandle(hendelse: ManuellBehandlingAvklaring) {
        hendelse.behov(
            behov,
            "Trenger informasjon om tidligere dagpenger",
            hendelse.legacyBehov(),
        )
    }

    override fun behandle(hendelse: LøstBehovHendelse) {
        if (hendelse.behov != behov) return
        if (vurdert()) return
        utfall = hendelse.utfall
        if (utfall == Utfall.Manuell) hendelse.varsel(Behandlingsvarsler.MULIG_GJENOPPTAK)
    }

    companion object {
        internal val Løsningstolk = booleanLøsningstolk
    }
}
