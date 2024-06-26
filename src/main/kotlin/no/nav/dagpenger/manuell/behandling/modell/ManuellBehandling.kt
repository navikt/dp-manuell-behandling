package no.nav.dagpenger.manuell.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.manuell.behandling.avklaring.Avklaring
import no.nav.dagpenger.manuell.behandling.avklaring.Utfall
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.modell.ManuellBehandlingObserver.ManuellBehandlingAvklart
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovHendelse
import java.util.UUID

internal class ManuellBehandling(
    val manuellBehandlingId: UUID,
    internal val ident: String,
    internal val søknadId: UUID,
    internal val behandlingId: UUID,
    private val avklaringer: List<Avklaring>,
) : Aktivitetskontekst {
    private val observatører = mutableSetOf<ManuellBehandlingObserver>()
    private var tilstand: Tilstand = IkkeVurdert()

    fun behandle(hendelse: ManuellBehandlingAvklaring) {
        hendelse.kontekst(this)
        tilstand.behandle(hendelse)
    }

    fun behandle(hendelse: LøstBehovHendelse) {
        hendelse.kontekst(this)
        tilstand.behandle(hendelse)
    }

    private val ferdigVurdert get() = avklaringer.all { it.vurdert() }

    private val behandlesManuelt get() = avklaringer.any { it.utfall == Utfall.Manuell }

    fun leggTilObservatør(observatør: ManuellBehandlingObserver) {
        observatører.add(observatør)
    }

    private sealed class Tilstand : Aktivitetskontekst {
        open fun behandle(hendelse: ManuellBehandlingAvklaring): Unit = throw IllegalStateException("Kan ikke behandle hendelse")

        open fun behandle(hendelse: LøstBehovHendelse): Unit = throw IllegalStateException("Kan ikke behandle hendelse")

        override fun toSpesifikkKontekst() = SpesifikkKontekst(this.javaClass.simpleName, emptyMap())
    }

    private inner class IkkeVurdert : Tilstand() {
        override fun behandle(hendelse: ManuellBehandlingAvklaring) {
            hendelse.kontekst(this)
            hendelse.info("Starter vurdering av manuell behandling")
            avklaringer.forEach { it.behandle(hendelse) }
            tilstand(VurderingPågår())
        }
    }

    private inner class VurderingPågår : Tilstand() {
        override fun behandle(hendelse: LøstBehovHendelse) {
            hendelse.kontekst(this)
            hendelse.info("Behandler løst behov for ${hendelse.behov}")
            avklaringer.forEach { it.behandle(hendelse) }
            hendelse.info("Sjekker om ${avklaringer.size} avklaringer er ferdig vurdert")
            if (ferdigVurdert) {
                hendelse.info("Vurdering av manuell behandling er ferdig, skalBehandlesManuelt=$behandlesManuelt")
                emitVurderingAvklart()
                tilstand(VurderingAvklart())
            } else {
                hendelse.info("Venter på ${avklaringer.filter { !it.vurdert() }.map { it.behov.name }} avklaringer til")
            }
        }
    }

    private inner class VurderingAvklart : Tilstand() {
        override fun behandle(hendelse: ManuellBehandlingAvklaring) {
            hendelse.info("Avklaring om manuell behandling finnes allerede, publiserer løsning på nytt")
            emitVurderingAvklart()
        }

        override fun behandle(hendelse: LøstBehovHendelse) {
            hendelse.info("Vurderingen er allerede avklart, kaster løsning")
        }
    }

    private fun tilstand(tilstand: Tilstand) {
        this.tilstand = tilstand
    }

    private fun emitVurderingAvklart() {
        observatører.forEach {
            it.vurderingAvklart(ManuellBehandlingAvklart(behandlesManuelt, ident, søknadId, behandlingId, avklaringer))
        }
    }

    override fun toSpesifikkKontekst() =
        SpesifikkKontekst(
            this.javaClass.simpleName,
            mapOf(
                "manuellBehandlingId" to manuellBehandlingId.toString(),
                "ident" to ident,
                "søknadId" to "$søknadId",
                "behandlingId" to "$behandlingId",
            ),
        )
}
