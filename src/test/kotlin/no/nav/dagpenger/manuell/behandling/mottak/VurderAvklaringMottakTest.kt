package no.nav.dagpenger.manuell.behandling.mottak

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean
import io.kotest.property.exhaustive.collection
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.manuell.behandling.Metrikker.avklaringTeller
import no.nav.dagpenger.manuell.behandling.avklaring.AvklaringsbehovLøstMottak
import no.nav.dagpenger.manuell.behandling.avklaring.Behov
import no.nav.dagpenger.manuell.behandling.avklaring.Utfall
import no.nav.dagpenger.manuell.behandling.avklaring.VurderAvklaringMottak
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.fail

internal class VurderAvklaringMottakTest {
    private val testRapid = TestRapid()

    init {
        VurderAvklaringMottak(testRapid)
        AvklaringsbehovLøstMottak(testRapid)
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
                val ident = "12345678910"
                testRapid.sendTestMessage(
                    nyAvklaring(
                        avklaringId,
                        avklaringskode = avklaringskode.first,
                        søknadId,
                        behandlingId,
                        ident,
                    ),
                )

                with(testRapid.inspektør) {
                    size shouldBe 1
                    val behov = this.message(0)
                    behov["@event_name"].asText() shouldBe "behov"
                    behov["avklaringId"].asText() shouldBe avklaringId.toString()
                    behov["søknadId"].asText() shouldBe søknadId.toString()
                    behov["behandlingId"].asText() shouldBe behandlingId.toString()
                    behov["ident"].asText() shouldBe ident
                    behov["Virkningstidspunkt"].shouldNotBeNull()
                    behov["@avklaringsbehov"].asBoolean() shouldBe true
                    val løsning =
                        behov.toString().let { JsonMessage(it, MessageProblems(it), SimpleMeterRegistry()) }.also {
                            it["@løsning"] = mapOf(avklaringskode.second.name to utfall)
                        }
                    testRapid.sendTestMessage(løsning.toJson())
                }

                val forventetUtfall = if (utfall) Utfall.Manuell else Utfall.Automatisk

                val antallMeldinger = if (avklaringskode.first == "EØSArbeid") 2 else 1
                when (forventetUtfall) {
                    Utfall.Manuell -> testRapid.inspektør.size shouldBe antallMeldinger
                    Utfall.Automatisk -> {
                        with(testRapid.inspektør) {
                            size shouldBe 2
                            this.key(1) shouldBe ident
                            this.message(1).also {
                                it["@event_name"].asText() shouldBe "AvklaringIkkeRelevant"
                                it["avklaringId"].asText() shouldBe avklaringId.toString()
                                it["ident"].shouldNotBeNull()
                                it["kode"].shouldNotBeNull()
                                it["behandlingId"].shouldNotBeNull()
                            }
                        }
                    }

                    Utfall.IkkeVurdert -> fail("Ikke implementert for $forventetUtfall")
                }
                testRapid.reset()
                avklaringTeller.labelValues(avklaringskode.first, forventetUtfall.toString()).get() shouldBe 1.0
            }
        }
        // avklaringer * 2 utfall
        avklaringTeller.collect().dataPoints.size shouldBe koder.size * 2
    }

    private fun nyAvklaring(
        uuid: UUID,
        avklaringskode: String,
        søknadId: UUID,
        behandlingId: UUID,
        ident: String,
    ) = // language=JSON
        """
        {
            "@event_name": "NyAvklaring",
            "ident": "$ident",
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
