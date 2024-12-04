package no.nav.dagpenger.manuell.behandling.mottak

import com.spun.util.persistence.Loader
import no.nav.dagpenger.manuell.behandling.avklaring.avklaringerTilBehovRegister
import org.approvaltests.Approvals
import org.approvaltests.core.Options
import org.approvaltests.namer.NamerWrapper
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class BehovdokumentasjonTest {
    @Test
    fun `skriv dokumentasjon`() {
        val markdown =
            """
            ># Dokumentasjon på behov for avklaringer
            >
            >Dette er opplysninger som blir innhentet som en del av sjekk på avklaringer. De publiseres som behov på rapiden.
            >
            >|Avklaring|Behov|
            >|---|---|
            ${
                avklaringerTilBehovRegister.toSortedMap().entries.joinToString("\n") { (kode, behov) ->
                    ">|$kode | ${behov.name}|"
                }
            }
            """.trimMargin(">")

        skriv(markdown)
    }

    private companion object {
        val path = "${Paths.get("").toAbsolutePath()}"
        val options = Options().forFile().withExtension(".md")
    }

    private fun skriv(behov: String) {
        Approvals.namerCreater = Loader { NamerWrapper({ "avklaring-behov" }, { path }) }
        Approvals.verify(behov, options)
    }
}
