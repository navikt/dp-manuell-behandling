package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovHendelse

internal typealias AvklaringFactory = () -> Avklaring

internal class Avklaring(
    private val begrunnelse: String,
    private val behov: Behov,
    private val varsel: Behandlingsvarsler.Varselkode2,
    private val behovKontekst: (hendelse: ManuellBehandlingAvklaring) -> Map<String, Any>,
) : Aktivitetskontekst {
    var utfall: Utfall = Utfall.IkkeVurdert
        private set

    fun behandle(hendelse: ManuellBehandlingAvklaring) =
        hendelse.behov(behov, "Trenger informasjon for å avklare $begrunnelse", behovKontekst(hendelse))

    fun behandle(hendelse: LøstBehovHendelse) {
        if (hendelse.behov != behov) return
        if (vurdert()) return
        hendelse.kontekst(this)
        utfall = hendelse.utfall

        when (utfall) {
            Utfall.Manuell -> hendelse.varsel(varsel)
            Utfall.Automatisk -> hendelse.info("Fant ikke grunnlag til å kreve manuell behandling på grunn av $begrunnelse")
            Utfall.IkkeVurdert -> TODO()
        }
    }

    fun vurdert() = utfall != Utfall.IkkeVurdert

    override fun toSpesifikkKontekst() = SpesifikkKontekst(this::class.simpleName ?: "Avklaring")
}

internal enum class Utfall {
    Manuell,
    Automatisk,
    IkkeVurdert,
}
