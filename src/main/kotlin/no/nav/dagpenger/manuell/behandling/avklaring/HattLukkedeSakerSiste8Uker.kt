package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovHendelse
import no.nav.dagpenger.manuell.behandling.mottak.booleanLøsningstolk

internal class HattLukkedeSakerSiste8Uker : Avklaring("Hatt lukkede saker siste 8 uker") {
    private val behov = Behov.HarHattLukketSiste8Uker

    override fun behandle(hendelse: ManuellBehandlingAvklaring) {
        hendelse.behov(
            behov,
            "Trenger informasjon om lukkede saker i Arena",
            hendelse.legacyBehov(),
        )
    }

    override fun behandle(hendelse: LøstBehovHendelse) {
        if (hendelse.behov != behov) return
        if (vurdert()) return
        utfall = hendelse.utfall
        if (utfall == Utfall.Manuell) hendelse.varsel(Behandlingsvarsler.LUKKEDE_SAKER_SISTE_8_UKER)
    }

    companion object {
        internal val Løsningstolk = booleanLøsningstolk
    }
}
