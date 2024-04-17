package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.hendelse.PersonHendelse

internal typealias AvklaringFactory = () -> Avklaring

internal abstract class Avklaring(
    private val begrunnelse: String,
    val varsel: Behandlingsvarsler.Varselkode2,
) : Aktivitetskontekst {
    private var utfall: Utfall = Utfall.IkkeVurdert

    abstract fun behandle(hendelse: ManuellBehandlingAvklaring)

    protected fun settUtfall(
        behandlesManuelt: Boolean,
        hendelse: PersonHendelse,
    ) {
        utfall =
            if (behandlesManuelt) {
                hendelse.varsel(varsel)
                Utfall.Manuell
            } else {
                hendelse.info("Fant ikke grunnlag til å kreve manuell behandling på grunn av $begrunnelse")
                Utfall.Automatisk
            }
    }

    val vurdert get() = utfall != Utfall.IkkeVurdert

    val behandlesManuelt get() = utfall != Utfall.Manuell

    override fun toSpesifikkKontekst() = SpesifikkKontekst(this::class.simpleName ?: "Avklaring")

    protected enum class Utfall {
        Manuell,
        Automatisk,
        IkkeVurdert,
    }
}
