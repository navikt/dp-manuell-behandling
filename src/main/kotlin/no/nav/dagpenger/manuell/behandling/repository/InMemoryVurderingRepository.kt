package no.nav.dagpenger.manuell.behandling.repository

import no.nav.dagpenger.manuell.behandling.avklaring.Avklaring
import no.nav.dagpenger.manuell.behandling.modell.ManuellBehandling
import java.util.UUID

internal interface VurderingRepository {
    fun opprett(
        fødselsnummer: String,
        søknadId: UUID,
    ): ManuellBehandling

    fun finn(
        ident: String,
        manuellBehandlingId: UUID,
    ): ManuellBehandling?

    fun lagre(avklaring: ManuellBehandling)
}

internal class InMemoryVurderingRepository(vararg vurderinger: Avklaring) : VurderingRepository {
    private val vurderinger = vurderinger.toList()
    private val avklaringer: MutableList<ManuellBehandling> = mutableListOf()

    override fun opprett(
        fødselsnummer: String,
        søknadId: UUID,
    ): ManuellBehandling {
        require(
            avklaringer.none { it.ident == fødselsnummer && it.søknadId == søknadId },
        ) { "Manuell behandling for søknadId=$søknadId og ident=$fødselsnummer finnes allerede" }
        return ManuellBehandling(UUID.randomUUID(), fødselsnummer, søknadId, vurderinger).also { avklaringer.add(it) }
    }

    override fun finn(
        ident: String,
        manuellBehandlingId: UUID,
    ) = avklaringer.find { it.ident == ident && it.manuellBehandlingId == manuellBehandlingId }

    override fun lagre(avklaring: ManuellBehandling) {
        // no-op
    }
}
