package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov
import java.util.UUID

internal val HattLukkedeSakerSiste8Uker = { id: UUID ->
    Avklaring(
        id = id,
        begrunnelse = "Hatt lukkede saker siste 8 uker",
        behov = Behov.HarHattLukketSiste8Uker,
        varsel = Behandlingsvarsler.LUKKEDE_SAKER_SISTE_8_UKER,
    ) { hendelse ->
        hendelse.legacyBehov()
    }
}
