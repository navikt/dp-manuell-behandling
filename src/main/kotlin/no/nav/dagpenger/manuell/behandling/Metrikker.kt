package no.nav.dagpenger.manuell.behandling

import io.prometheus.client.Counter
import io.prometheus.client.Histogram

object Metrikker {
    val avklaringTeller: Counter =
        Counter
            .build("dp_behandling_avklaring_utfall", "Utfall for avklaringer")
            .labelNames("kode", "utfall")
            .register()

    val avklaringTidBrukt: Histogram =
        Histogram
            .build("dp_behandling_avklaring_duration", "Tid brukt for å avklare")
            .labelNames("kode")
            .register()
}
