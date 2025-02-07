package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.aktivitetslogg.aktivitet.Behov

enum class Behov : Behov.Behovtype {
    EØSArbeid,
    JobbetUtenforNorge,
    HarHattLukketSiste8Uker,
    HarRapportertInntektNesteMåned,
    SykepengerSiste36Måneder,
    HarHattDagpengerSiste13Mnd, // Mulig gjenopptak
    HarTilleggsopplysninger,
}
