package no.nav.dagpenger.manuell.behandling

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.manuell.behandling.mottak.InformasjonsbehovLøstMottak
import no.nav.dagpenger.manuell.behandling.mottak.VurderAvklaringMottak
import no.nav.dagpenger.manuell.behandling.repository.InMemoryAvklaringRepository
import no.nav.helse.rapids_rivers.RapidApplication
import java.util.UUID

fun main() {
    val env = System.getenv()

    RapidApplication
        .create(env)
        .apply {
            val repository = InMemoryAvklaringRepository()
            VurderAvklaringMottak(this, repository)
            InformasjonsbehovLøstMottak(this, repository)
        }.start()
}

fun JsonNode.asUUID(): UUID = this.asText().let { UUID.fromString(it) }
