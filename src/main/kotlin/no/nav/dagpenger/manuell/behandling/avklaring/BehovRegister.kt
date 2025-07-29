package no.nav.dagpenger.manuell.behandling.avklaring

val avklaringerTilBehovRegister =
    mapOf(
        "EØSArbeid" to Behov.EØSArbeid,
        "HattLukkedeSakerSiste8Uker" to Behov.HarHattLukketSiste8Uker,
        "MuligGjenopptak" to Behov.HarHattDagpengerSiste13Mnd,
        "InntektNesteKalendermåned" to Behov.HarRapportertInntektNesteMåned,
        "JobbetUtenforNorge" to Behov.JobbetUtenforNorge,
        "HarTilleggsopplysninger" to Behov.HarTilleggsopplysninger,
    )
