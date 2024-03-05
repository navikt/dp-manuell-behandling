package no.nav.dagpenger.manuell.behandling.modell

import mu.KotlinLogging
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
                    "@løsning" to
                        mapOf(
                            "AvklaringManuellBehandling" to manuellBehandlingAvklart.behandlesManuelt,
                        ),
                ),
            )
        rapidsConnection.publish(manuellBehandlingAvklart.ident, event.toJson())
        logger.info { "Publiser løsning for AvklaringManuellBehandling" }
    }

    private companion object {
        val logger = KotlinLogging.logger {}
    }
}
