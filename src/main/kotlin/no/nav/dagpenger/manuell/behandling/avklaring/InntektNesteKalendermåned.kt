package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovHendelse
import no.nav.dagpenger.manuell.behandling.mottak.booleanLøsningstolk

internal class InntektNesteKalendermåned : Avklaring("Har innrapport inntekt for neste måned") {
    private val behov = Behov.HarRapportertInntektNesteMåned

    override fun behandle(hendelse: ManuellBehandlingAvklaring) {
        hendelse.behov(
            behov,
            "Trenger informasjon om inntekter i neste kalendermåned",
            hendelse.legacyBehov(),
        )
    }

    override fun behandle(hendelse: LøstBehovHendelse) {
        if (hendelse.behov != behov) return
        if (vurdert()) return
        utfall = hendelse.utfall
        if (utfall == Utfall.Manuell) hendelse.varsel(Behandlingsvarsler.INNTEKT_NESTE_KALENDERMÅNED)
    }

    companion object {
        internal val Løsningstolk = booleanLøsningstolk
    }
}
