package no.nav.dagpenger.manuell.behandling

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean
import io.kotest.property.exhaustive.collection
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.manuell.behandling.avklaring.Behov
import no.nav.dagpenger.manuell.behandling.avklaring.Utfall
import no.nav.dagpenger.manuell.behandling.mottak.InformasjonsbehovLøstMottak
import no.nav.dagpenger.manuell.behandling.mottak.VurderAvklaringMottak
import no.nav.dagpenger.manuell.behandling.repository.InMemoryAvklaringRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.util.UUID

internal class VurderAvklaringMottakTest {
    private val testRapid = TestRapid()

    private val avklaringRepository =
        InMemoryAvklaringRepository().also {
            VurderAvklaringMottak(testRapid, it)
            InformasjonsbehovLøstMottak(testRapid, it)
        }

    @Test
    fun `kan motta ny avklaring `() {
        val koder =
            listOf(
                Pair("SvangerskapsrelaterteSykepenger", Behov.SykepengerSiste36Måneder),
                Pair("EØSArbeid", Behov.EØSArbeid),
                Pair("HattLukkedeSakerSiste8Uker", Behov.HarHattLukketSiste8Uker),
                Pair("MuligGjenopptak", Behov.HarHattDagpengerSiste13Mnd),
                Pair("InntektNesteKalendermåned", Behov.HarRapportertInntektNesteMåned),
                Pair("JobbetUtenforNorge", Behov.JobbetUtenforNorge),
            )
        runBlocking {
            checkAll(Exhaustive.collection(koder), Exhaustive.boolean()) { avklaringskode, utfall ->
                val avklaringId = UUID.randomUUID()
                val søknadId = UUID.randomUUID()
                val behandlingId = UUID.randomUUID()
                testRapid.sendTestMessage(nyAvklaring(avklaringId, avklaringskode = avklaringskode.first, søknadId, behandlingId))

                with(testRapid.inspektør) {
                    size shouldBe 1
                    this.message(0).also {
                        it["@event_name"].asText() shouldBe "behov"
                        it["avklaringId"].asText() shouldBe avklaringId.toString()
                        it["søknadId"].asText() shouldBe søknadId.toString()
                        it["behandlingId"].asText() shouldBe behandlingId.toString()
                    }
                }

                testRapid.sendTestMessage(informasjonsbehovLøst(avklaringId, avklaringskode.second.name, utfall))

                val forventetUtfall = if (utfall) Utfall.Manuell else Utfall.Automatisk

                when (forventetUtfall) {
                    Utfall.Manuell -> testRapid.inspektør.size shouldBe 1
                    Utfall.Automatisk -> {
                        with(testRapid.inspektør) {
                            size shouldBe 2
                            this.message(1).also {
                                it["@event_name"].asText() shouldBe "AvklaringIkkeRelevant"
                                it["avklaringId"].asText() shouldBe avklaringId.toString()
                                it["ident"].shouldNotBeNull()
                                it["kode"].shouldNotBeNull()
                                it["behandlingId"].shouldNotBeNull()
                            }
                        }
                    }

                    Utfall.IkkeVurdert -> TODO()
                }
                testRapid.reset()
            }
        }
    }

    @Language("JSON")
    private fun informasjonsbehovLøst(
        uuid: UUID,
        avklaringskode: String,
        utfall: Boolean,
    ) = """
        {
            "@event_name": "behov",
            "@behovId": "353a1d0f-f82b-4ead-88d0-3340e51f24a7",
            "@behov": [
            "$avklaringskode"
            ],
            "Virkningstidspunkt": "2024-06-24",
            "søknad_uuid": "4afce924-6cb4-4ab4-a92b-fe91e24f31bf",
            "identer": [
            {
                "type": "folkeregisterident",
                "historisk": false,
                "id": "11109233444"
            }
            ],
            "@løsning": {
            "$avklaringskode": $utfall
        },
            "avklaringId": "$uuid",
            "@id": "737172d8-2207-4458-af43-5dab8a13d192",
            "@opprettet": "2024-06-24T12:21:56.62289",
            "system_read_count": 0,
            "system_participating_services": [
            {
                "id": "737172d8-2207-4458-af43-5dab8a13d192",
                "time": "2024-06-24T12:21:56.622890"
            }
            ]
        }
        """.trimIndent()

    private fun nyAvklaring(
        uuid: UUID,
        avklaringskode: String,
        søknadId: UUID,
        behandlingId: UUID,
    ) = // language=JSON
        """
        {
            "@event_name": "NyAvklaring",
            "ident": "11109233444",
            "avklaringId": "$uuid",
            "kode": "$avklaringskode",
            "behandlingId": "$behandlingId",
            "gjelderDato": "2024-06-24",
            "søknadId": "$søknadId",
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
