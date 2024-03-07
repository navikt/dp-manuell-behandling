package no.nav.dagpenger.manuell.behandling

import io.mockk.mockk
import no.nav.dagpenger.manuell.behandling.avklaring.ArbeidIEØS
import no.nav.dagpenger.manuell.behandling.avklaring.Behov
import no.nav.dagpenger.manuell.behandling.avklaring.HattLukkedeSakerSiste8Uker
import no.nav.dagpenger.manuell.behandling.modell.ManuellBehandlingObserverKafka
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovMottak
import no.nav.dagpenger.manuell.behandling.mottak.ManuellBehandlingService
import no.nav.dagpenger.manuell.behandling.repository.InMemoryVurderingRepository
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class MediatorTest {
    private val rapid = TestRapid()
    private val repository =
        InMemoryVurderingRepository(
            ArbeidIEØS,
            HattLukkedeSakerSiste8Uker,
        )
    private val mediator =
        Mediator(
            repository = repository,
            aktivitetsloggMediator = mockk(relaxed = true),
            behovMediator = BehovMediator(rapid),
            observatører = listOf(ManuellBehandlingObserverKafka(rapid)),
        ).apply {
            ManuellBehandlingService(rapid, this)
            LøstBehovMottak(rapid, this)
        }
    private val ident = "12345678910"
    private val søknadId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

    @Test
    fun `e2e`() {
        rapid.sendTestMessage(avklaringsBehov.toJson())

        assertEquals(1, rapid.inspektør.size)
        with(rapid.inspektør.message(0)) {
            assertEquals(
                listOf(
                    Behov.EØSArbeid.name,
                    Behov.HarHattLukketSiste8Uker.name,
                ),
                this["@behov"].map { it.asText() },
            )
        }
        val manuellBehandlingId = rapid.inspektør.field(0, "manuellBehandlingId").asUUID()

        rapid.sendTestMessage(eøsLøsning(manuellBehandlingId).toJson())
        rapid.sendTestMessage(eøsLøsning(manuellBehandlingId).toJson())
        rapid.sendTestMessage(eøsLøsning(manuellBehandlingId).toJson())
        assertEquals(1, rapid.inspektør.size, "Lager ikke behov på nytt")

        rapid.sendTestMessage(lukkedeSakerLøsning(manuellBehandlingId).toJson())

        assertEquals(2, rapid.inspektør.size)
        with(rapid.inspektør.message(1)) {
            assertTrue(this["@løsning"]["AvklaringManuellBehandling"].asBoolean())
        }
    }

    private val avklaringsBehov =
        JsonMessage.newNeed(
            listOf("AvklaringManuellBehandling"),
            mapOf(
                "ident" to ident,
                "søknadId" to søknadId.toString(),
            ),
        )

    private fun eøsLøsning(manuellBehandlingId: UUID) =
        JsonMessage.newNeed(
            listOf(Behov.EØSArbeid.name, Behov.HarHattLukketSiste8Uker.name),
            mapOf(
                "ident" to ident,
                "søknadId" to søknadId.toString(),
                "manuellBehandlingId" to manuellBehandlingId.toString(),
                "@løsning" to
                    mapOf(
                        Behov.EØSArbeid.name to false,
                    ),
            ),
        )

    private fun lukkedeSakerLøsning(manuellBehandlingId: UUID) =
        JsonMessage.newNeed(
            listOf(Behov.EØSArbeid.name, Behov.HarHattLukketSiste8Uker.name),
            mapOf(
                "ident" to ident,
                "søknadId" to søknadId.toString(),
                "manuellBehandlingId" to manuellBehandlingId.toString(),
                "@løsning" to
                    mapOf(
                        Behov.HarHattLukketSiste8Uker.name to true,
                    ),
            ),
        )
}
