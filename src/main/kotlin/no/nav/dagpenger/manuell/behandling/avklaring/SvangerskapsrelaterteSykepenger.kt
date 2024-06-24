package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.legacyBehov
import java.util.UUID

internal val SvangerskapsrelaterteSykepenger = { id: UUID ->
    Avklaring(
        id = id,
        begrunnelse = "Har hatt sykepenger som kan være svangerskapsrelatert",
        behov = Behov.SykepengerSiste36Måneder,
        varsel = Behandlingsvarsler.SVANGERSKAPSRELATERTE_SYKEPENGER,
    ) { hendelse ->
        hendelse.legacyBehov()
    }
}
