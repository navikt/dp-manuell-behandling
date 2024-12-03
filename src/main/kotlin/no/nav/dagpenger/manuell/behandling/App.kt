package no.nav.dagpenger.manuell.behandling

import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID
import no.nav.dagpenger.manuell.behandling.mottak.AvklaringsbehovLøstMottak
import no.nav.dagpenger.manuell.behandling.mottak.NyAvklaringMottak
import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()

    RapidApplication
        .create(env)
        .apply {
            // Tilstandsløs håndtering av avklaring
            NyAvklaringMottak(this)
            AvklaringsbehovLøstMottak(this)
        }.start()
}

fun JsonNode.asUUID(): UUID = this.asText().let { UUID.fromString(it) }
