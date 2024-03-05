package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.aktivitetslogg.Aktivitet

enum class Behov : Aktivitet.Behov.Behovtype {
    EØSArbeid,
    JobbetUtenforNorge,
    HarHattLukketSiste8Uker,
    HarRapportertInntektNesteMåned,
    SykepengerSiste36Måneder,
    HarHattDagpengerSiste13Mnd, // Mulig gjenopptak
}
