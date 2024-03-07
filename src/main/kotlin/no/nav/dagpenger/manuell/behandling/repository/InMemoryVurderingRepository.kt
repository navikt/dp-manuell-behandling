package no.nav.dagpenger.manuell.behandling.repository

import no.nav.dagpenger.manuell.behandling.avklaring.Avklaring
import no.nav.dagpenger.manuell.behandling.modell.ManuellBehandling
import java.util.UUID

internal interface VurderingRepository {
    fun finnEllerOpprett(
        fødselsnummer: String,
        søknadId: UUID,
    ): ManuellBehandling

    fun lagre(avklaring: ManuellBehandling)

    fun finn(
        ident: String,
        søknadId: UUID,
    ): ManuellBehandling?
}

internal class InMemoryVurderingRepository(vararg vurderinger: Avklaring) : VurderingRepository {
    private val vurderinger = vurderinger.toList()
    private val avklaringer: MutableList<ManuellBehandling> = mutableListOf()

    override fun finnEllerOpprett(
        fødselsnummer: String,
        søknadId: UUID,
    ) = finn(fødselsnummer, søknadId)
        ?: ManuellBehandling(UUID.randomUUID(), fødselsnummer, søknadId, vurderinger).also { avklaringer.add(it) }

    override fun lagre(avklaring: ManuellBehandling) {
        // no-op
    }

    override fun finn(
        ident: String,
        søknadId: UUID,
    ) = avklaringer.find { it.ident == ident && it.søknadId == søknadId }
}
