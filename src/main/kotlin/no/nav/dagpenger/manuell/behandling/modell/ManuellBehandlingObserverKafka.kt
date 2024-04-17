package no.nav.dagpenger.manuell.behandling.modell

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ManuellBehandlingObserverKafka(private val rapidsConnection: RapidsConnection) :
    ManuellBehandlingObserver {
    override fun vurderingAvklart(manuellBehandlingAvklart: ManuellBehandlingObserver.ManuellBehandlingAvklart) {
        val event =
            JsonMessage.newNeed(
                listOf("AvklaringManuellBehandling"),
                mapOf(
                    "ident" to manuellBehandlingAvklart.ident,
                    "søknadId" to manuellBehandlingAvklart.søknadId,
                    "behandlingId" to manuellBehandlingAvklart.behandlingId,
                    "vurderinger" to
                        manuellBehandlingAvklart.vurderinger.map {
                            mapOf(
                                "type" to it.behov.name,
                                "begrunnelse" to it.varsel.varseltekst,
                                "utfall" to it.utfall.name,
                            )
                        },
                    "@løsning" to
                        mapOf(
                            "AvklaringManuellBehandling" to manuellBehandlingAvklart.behandlesManuelt,
                        ),
                ),
            )

        withLoggingContext(
            "behandlingId" to manuellBehandlingAvklart.behandlingId.toString(),
        ) {
            rapidsConnection.publish(manuellBehandlingAvklart.ident, event.toJson())
            logger.info { "Publiser løsning for AvklaringManuellBehandling, behandlesManuelt=${manuellBehandlingAvklart.behandlesManuelt}" }
        }
    }

    private companion object {
        val logger = KotlinLogging.logger {}
    }
}
