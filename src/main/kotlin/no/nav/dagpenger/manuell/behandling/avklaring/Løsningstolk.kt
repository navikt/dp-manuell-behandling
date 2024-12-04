package no.nav.dagpenger.manuell.behandling.avklaring

import com.fasterxml.jackson.databind.JsonNode

fun interface Løsningstolk {
    fun tolk(løsning: JsonNode): Utfall
}

enum class Utfall {
    Manuell,
    Automatisk,
    IkkeVurdert,
}

// En standard tolk som funker for det meste
val booleanLøsningstolk =
    Løsningstolk { løsning ->
        when (løsning.asBoolean()) {
            true -> Utfall.Manuell
            false -> Utfall.Automatisk
        }
    }
