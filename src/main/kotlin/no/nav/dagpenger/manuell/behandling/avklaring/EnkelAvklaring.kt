package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring

internal class EnkelAvklaring(
    begrunnelse: String,
    varsel: Behandlingsvarsler.Varselkode2,
    private val block: (ManuellBehandlingAvklaring) -> Boolean,
) : Avklaring(begrunnelse, varsel) {
    override fun behandle(hendelse: ManuellBehandlingAvklaring) {
        settUtfall(block(hendelse), hendelse)
    }
}
