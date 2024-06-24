package no.nav.dagpenger.manuell.behandling

import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.manuell.behandling.repository.InMemoryAvklaringRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

internal class VurderAvklaringMottakTest {
    private val testRapid = TestRapid()

    val avklaringRepository =
        InMemoryAvklaringRepository(testRapid).also {
            VurderAvklaringMottak(testRapid, it)
        }

    @Test
    fun `kan motta ny avklaring `() {
        val koder =
            listOf(
                "SvangerskapsrelaterteSykepenger",
                "ArbeidIEØS",
                "HattLukkedeSakerSiste8Uker",
                "MuligGjenopptak",
                "InntektNesteKalendermåned",
                "JobbetUtenforNorge",
            )
        runBlocking {
            checkAll(Arb.element(koder)) { avklaringskode ->
                testRapid.sendTestMessage(nyAvklaring(avklaringskode))
                avklaringRepository.avklaringer.size shouldBe 1

                testRapid.inspektør.size shouldBe 1

                testRapid.reset()
            }
        }
    }

    private fun nyAvklaring(avklaringskode: String) =
        // language=JSON
        """
        {
          "@event_name": "NyAvklaring",
          "ident": "11109233444",
          "avklaringId": "01904942-3102-7da3-bd16-084acd959e1d",
          "kode": "$avklaringskode",
          "behandlingId": "01904942-2ef5-7a8c-975f-ae60bd98ea66",
          "gjelderDato": "2024-06-24",
          "søknadId": "4afce924-6cb4-4ab4-a92b-fe91e24f31bf",
          "søknad_uuid": "4afce924-6cb4-4ab4-a92b-fe91e24f31bf",
          "@id": "74a01063-4b78-4758-89d0-52d6b358b2e2",
          "@opprettet": "2024-06-24T09:59:53.107584",
          "system_read_count": 0,
          "system_participating_services": [
            {
              "id": "74a01063-4b78-4758-89d0-52d6b358b2e2",
              "time": "2024-06-24T09:59:53.107584"
            }
          ]
        }
        """.trimIndent()
}
