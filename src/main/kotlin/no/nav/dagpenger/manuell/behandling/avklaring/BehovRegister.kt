package no.nav.dagpenger.manuell.behandling.avklaring

val avklaringerTilBehovRegister =
    mapOf(
        "SvangerskapsrelaterteSykepenger" to Behov.SykepengerSiste36Måneder,
        "EØSArbeid" to Behov.EØSArbeid,
        "HattLukkedeSakerSiste8Uker" to Behov.HarHattLukketSiste8Uker,
        "MuligGjenopptak" to Behov.HarHattDagpengerSiste13Mnd,
        "InntektNesteKalendermåned" to Behov.HarRapportertInntektNesteMåned,
        "JobbetUtenforNorge" to Behov.JobbetUtenforNorge,
    )
