package no.nav.dagpenger.manuell.behandling

import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.dagpenger.manuell.behandling.hendelse.SøknadHendelse
import no.nav.dagpenger.manuell.behandling.modell.ManuellBehandling
import no.nav.dagpenger.manuell.behandling.modell.ManuellBehandlingObserver
import no.nav.dagpenger.manuell.behandling.mottak.LøstBehovHendelse
import no.nav.dagpenger.manuell.behandling.repository.VurderingRepository
import no.nav.helse.rapids_rivers.withMDC

internal class Mediator(
    private val repository: VurderingRepository,
    private val aktivitetsloggMediator: AktivitetsloggMediator,
    private val behovMediator: BehovMediator,
    private val observatører: List<ManuellBehandlingObserver> = emptyList(),
) : VurderingRepository by repository {
    fun håndter(hendelse: ManuellBehandlingAvklaring) {
        opprettOgBehandle(hendelse) { manuellBehandling ->
            manuellBehandling.behandle(hendelse)
        }
    }

    fun håndter(hendelse: LøstBehovHendelse) {
        hentOgBehandle(hendelse) { manuellBehandling ->
            manuellBehandling.behandle(hendelse)
        }
    }

    private fun hentOgBehandle(
        hendelse: SøknadHendelse,
        håndter: (ManuellBehandling) -> Unit,
    ) {
        if (finn(hendelse.ident(), hendelse.søknadId) == null) {
            hendelse.info("Fant ikke behandling for hendelse")
            return
        }
        opprettOgBehandle(hendelse, håndter)
    }

    private fun opprettOgBehandle(
        hendelse: SøknadHendelse,
        håndter: (ManuellBehandling) -> Unit,
    ) = try {
        val manuellBehandling = finnEllerOpprett(hendelse.ident(), hendelse.søknadId)
        observatører.forEach { manuellBehandling.leggTilObservatør(it) }
        håndter(manuellBehandling)
        lagre(manuellBehandling)
        ferdigstill(hendelse)
    } catch (err: Aktivitetslogg.AktivitetException) {
        logger.error("alvorlig feil i aktivitetslogg (se sikkerlogg for detaljer)")

        withMDC(err.kontekst()) {
            sikkerlogg.error("alvorlig feil i aktivitetslogg: ${err.message}", err)
        }
        throw err
    } catch (e: Exception) {
        errorHandler(e, e.message ?: "Ukjent feil")
        throw e
    }

    private fun ferdigstill(hendelse: SøknadHendelse) {
        if (!hendelse.harAktiviteter()) return
        if (hendelse.harFunksjonelleFeilEllerVerre()) {
            logger.info("aktivitetslogg inneholder feil (se sikkerlogg)")
            sikkerlogg.error("aktivitetslogg inneholder feil:\n${hendelse.toLogString()}")
        } else {
            sikkerlogg.info("aktivitetslogg inneholder meldinger:\n${hendelse.toLogString()}")
        }
        sikkerlogg.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
        behovMediator.håndter(hendelse)
        aktivitetsloggMediator.håndter(hendelse)
    }

    private fun errorHandler(
        err: Exception,
        message: String,
        context: Map<String, String> = emptyMap(),
    ) {
        logger.error("alvorlig feil: ${err.message} (se sikkerlogg for melding)", err)
        withMDC(context) { sikkerlogg.error("alvorlig feil: ${err.message}\n\t$message", err) }
    }

    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerlogg = KotlinLogging.logger("tjenestekall.Mediator")
    }
}
