package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovHendelse

internal abstract class Avklaring(val begrunnelse: String) {
    var utfall: Utfall = Utfall.IkkeVurdert
        protected set

    abstract fun behandle(hendelse: ManuellBehandlingAvklaring)

    abstract fun behandle(hendelse: LøstBehovHendelse)

    fun vurdert() = utfall != Utfall.IkkeVurdert
}

internal enum class Utfall {
    Manuell,
    Automatisk,
    IkkeVurdert,
}
