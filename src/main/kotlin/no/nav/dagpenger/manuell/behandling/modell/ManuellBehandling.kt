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
    internal val ident: String,
    internal val søknadId: UUID,
    private val avklaringer: List<Avklaring>,
) : Aktivitetskontekst {
    private val observatører = mutableSetOf<ManuellBehandlingObserver>()

    fun behandle(hendelse: ManuellBehandlingAvklaring) {
        hendelse.kontekst(this)
        avklaringer.forEach { it.behandle(hendelse) }
    }

    fun behandle(hendelse: LøstBehovHendelse) {
        hendelse.kontekst(this)
        avklaringer.forEach { it.behandle(hendelse) }
        if (ferdigVurdert()) {
            emitVurderingAvklart()
        }
    }

    fun ferdigVurdert() = avklaringer.all { it.vurdert() }

    fun behandlesManuelt() = avklaringer.any { it.utfall == Utfall.Manuell }

    fun leggTilObservatør(observatør: ManuellBehandlingObserver) {
        observatører.add(observatør)
    }

    private fun emitVurderingAvklart() {
        observatører.forEach {
            it.vurderingAvklart(ManuellBehandlingAvklart(behandlesManuelt(), ident, søknadId))
        }
    }

    override fun toSpesifikkKontekst() = SpesifikkKontekst(this.javaClass.simpleName, mapOf("ident" to ident, "søknadId" to "$søknadId"))
}
