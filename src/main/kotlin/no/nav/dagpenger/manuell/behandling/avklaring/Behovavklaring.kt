package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovHendelse

internal class Behovavklaring(
    private val begrunnelse: String,
    varsel: Behandlingsvarsler.Varselkode2,
    private val behov: Behov,
    private val behovKontekst: (hendelse: ManuellBehandlingAvklaring) -> Map<String, Any>,
) : Avklaring(begrunnelse, varsel) {
    override fun behandle(hendelse: ManuellBehandlingAvklaring) =
        hendelse.behov(behov, "Trenger informasjon for å avklare $begrunnelse", behovKontekst(hendelse))

    fun behandle(hendelse: LøstBehovHendelse) {
        if (hendelse.behov != behov) return
        if (vurdert) return
        hendelse.kontekst(this)
        settUtfall(hendelse.behandlesManuelt, hendelse)
    }

    override fun toSpesifikkKontekst() = SpesifikkKontekst(this::class.simpleName ?: "Avklaring")
}
