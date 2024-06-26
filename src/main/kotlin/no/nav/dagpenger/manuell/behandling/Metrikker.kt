package no.nav.dagpenger.manuell.behandling

import io.prometheus.client.Counter

object Metrikker {
    val avklaringTeller =
        Counter
            .build("dp_behandling_avklaring_utfall", "Utfall for avklaringer")
            .labelNames("kode", "utfall")
            .register()
}
