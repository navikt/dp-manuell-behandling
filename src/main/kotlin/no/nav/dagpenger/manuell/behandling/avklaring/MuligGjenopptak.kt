package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov
import java.util.UUID

internal val MuligGjenopptak = { id: UUID ->
    Avklaring(
        id = id,
        begrunnelse = "Mulig gjenopptak",
        behov = Behov.HarHattDagpengerSiste13Mnd,
        varsel = Behandlingsvarsler.MULIG_GJENOPPTAK,
    ) { hendelse ->
        hendelse.legacyBehov()
    }
}
