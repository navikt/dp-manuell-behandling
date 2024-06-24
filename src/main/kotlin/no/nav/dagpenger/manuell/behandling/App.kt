package no.nav.dagpenger.manuell.behandling

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.manuell.behandling.avklaring.ArbeidIEØS
import no.nav.dagpenger.manuell.behandling.avklaring.HattLukkedeSakerSiste8Uker
import no.nav.dagpenger.manuell.behandling.avklaring.InntektNesteKalendermåned
import no.nav.dagpenger.manuell.behandling.avklaring.JobbetUtenforNorge
import no.nav.dagpenger.manuell.behandling.avklaring.MuligGjenopptak
import no.nav.dagpenger.manuell.behandling.avklaring.SvangerskapsrelaterteSykepenger
import no.nav.dagpenger.manuell.behandling.modell.ManuellBehandlingObserverKafka
import no.nav.dagpenger.manuell.behandling.mottak.InformasjonsbehovLøstMottak
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovMottak
import no.nav.dagpenger.manuell.behandling.mottak.ManuellBehandlingService
import no.nav.dagpenger.manuell.behandling.mottak.VurderAvklaringMottak
import no.nav.dagpenger.manuell.behandling.repository.InMemoryAvklaringRepository
import no.nav.dagpenger.manuell.behandling.repository.InMemoryVurderingRepository
import no.nav.helse.rapids_rivers.RapidApplication
import java.util.UUID

fun main() {
    val env = System.getenv()

    RapidApplication
        .create(env)
        .apply {
            val mediator =
                Mediator(
                    InMemoryVurderingRepository(
                        ArbeidIEØS,
                        HattLukkedeSakerSiste8Uker,
                        InntektNesteKalendermåned,
                        JobbetUtenforNorge,
                        MuligGjenopptak,
                        SvangerskapsrelaterteSykepenger,
                    ),
                    AktivitetsloggMediator(this),
                    BehovMediator(this, unleash),
                    listOf(ManuellBehandlingObserverKafka(this)),
                )
            LøstBehovMottak(this, mediator)
            ManuellBehandlingService(this, mediator)

            val repository = InMemoryAvklaringRepository(this)
            VurderAvklaringMottak(this, repository)
            InformasjonsbehovLøstMottak(this, repository)
        }.start()
}

fun JsonNode.asUUID(): UUID = this.asText().let { UUID.fromString(it) }
