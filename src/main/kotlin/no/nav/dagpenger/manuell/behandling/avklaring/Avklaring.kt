package no.nav.dagpenger.manuell.behandling.avklaring

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.manuell.behandling.hendelse.LøstBehovHendelse
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import java.util.UUID

internal typealias AvklaringFactory = (UUID) -> Avklaring

internal class Avklaring(
    internal val id: UUID = UUID.randomUUID(),
    private val begrunnelse: String,
    internal val behov: Behov,
    internal val varsel: Behandlingsvarsler.Varselkode2,
    internal val behovKontekst: (hendelse: ManuellBehandlingAvklaring) -> Map<String, Any>,
) : Aktivitetskontekst {
    var utfall: Utfall = Utfall.IkkeVurdert

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

    fun lagInformasjonsbehov(manuellBehandlingAvklaring: ManuellBehandlingAvklaring): String =
        JsonMessage
            .newNeed(
                behov = listOf(behov.name),
                behovKontekst(manuellBehandlingAvklaring) + mapOf("avklaringId" to id.toString()),
            ).toJson()
}

internal enum class Utfall {
    Manuell,
    Automatisk,
    IkkeVurdert,
}
