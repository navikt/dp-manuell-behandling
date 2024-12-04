package no.nav.dagpenger.manuell.behandling

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.manuell.behandling.avklaring.AvklaringsbehovLøstMottak
import no.nav.dagpenger.manuell.behandling.avklaring.VurderAvklaringMottak
import no.nav.helse.rapids_rivers.RapidApplication
import java.util.UUID

fun main() {
    val env = System.getenv()

    RapidApplication
        .create(env)
        .apply {
            VurderAvklaringMottak(this)
            AvklaringsbehovLøstMottak(this)
        }.start()
}

fun JsonNode.asUUID(): UUID = this.asText().let { UUID.fromString(it) }
