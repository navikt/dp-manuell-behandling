package no.nav.dagpenger.manuell.behandling

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.manuell.behandling.hendelse.PersonHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

class BehovMediator(private val rapidsConnection: RapidsConnection) {
    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerlogg = KotlinLogging.logger("tjenestekall.BehovMediator")
    }

    internal fun håndter(hendelse: PersonHendelse) {
        hendelse.kontekster().forEach { if (!it.harFunksjonelleFeilEllerVerre()) håndter(hendelse, it.behov()) }
    }

    private fun håndter(
        hendelse: PersonHendelse,
        behov: List<Aktivitet.Behov>,
    ) {
        behov
            .groupBy { it.kontekst() }
            .grupperBehovTilDetaljer()
            .forEach { (kontekst, behovMap) ->
                mutableMapOf<String, Any>()
                    .apply {
                        putAll(kontekst)
                        putAll(behovMap)
                        // Flat ut alle kontekster rett på root i behovet. Dette er for å være kompatibel med gamle behovløsere
                        behovMap.values.forEach { putAll(it as Map<String, Any>) }
                    }
                    .let { JsonMessage.newNeed(behovMap.keys, it) }
                    .also {
                        withLoggingContext("behovId" to it.id) {
                            sikkerlogg.info { "sender behov for ${behovMap.keys}:\n${it.toJson()}}" }
                            logger.info { "sender behov for ${behovMap.keys}" }
                            rapidsConnection.publish(hendelse.ident(), it.toJson())
                        }
                    }
            }
    }

    private fun Map<Map<String, String>, List<Aktivitet.Behov>>.grupperBehovTilDetaljer() =
        mapValues { (kontekst, behovliste) ->
            behovliste
                .groupBy({ it.type.name }, { it.detaljer() })
                .ikkeTillatUnikeDetaljerPåSammeBehov(kontekst, behovliste)
        }

    private fun <K : Any> Map<K, List<Map<String, Any?>>>.ikkeTillatUnikeDetaljerPåSammeBehov(
        kontekst: Map<String, String>,
        behovliste: List<Aktivitet.Behov>,
    ) = mapValues { (_, detaljerList) ->
        // tillater duplikate detaljer-maps, så lenge de er like
        detaljerList
            .distinct()
            .also { detaljer ->
                require(detaljer.size == 1) {
                    sikkerlogg.error(
                        "Forsøkte å sende duplikate behov på kontekst " +
                            kontekst.entries.joinToString { "${it.key}=${it.value}" },
                    )
                    "Kan ikke produsere samme behov på samme kontekst med ulike detaljer. " +
                        "Forsøkte å be om ${behovliste.joinToString { it.type.name }}"
                }
            }
            .single()
    }
}
