package no.nav.dagpenger.manuell.behandling

import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.core.metrics.Histogram

object Metrikker {
    val avklaringTeller: Counter =
        Counter
            .builder()
            .name("dp_behandling_avklaring_utfall")
            .help("Utfall for avklaringer")
            .labelNames("kode", "utfall")
            .register()

    val avklaringTidBrukt: Histogram =
        Histogram
            .builder()
            .name("dp_behandling_avklaring_duration")
            .help("Tid brukt for å avklare")
            .labelNames("kode")
            .register()
}
