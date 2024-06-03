package no.nav.dagpenger.manuell.behandling

import com.natpryce.konfig.Key
import com.natpryce.konfig.stringType
import io.getunleash.DefaultUnleash
import io.getunleash.FakeUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import no.nav.dagpenger.manuell.Configuration.properties
import java.net.InetAddress

internal val unleash: Unleash =
    if (properties.getOrNull(Key("UNLEASH_SERVER_API_URL", stringType)) == null) {
        FakeUnleash()
    } else {
        DefaultUnleash(
            UnleashConfig.builder()
                .appName(properties[Key("NAIS_APP_NAME", stringType)])
                .instanceId(runCatching { InetAddress.getLocalHost().hostName }.getOrElse { "ukjent" })
                .unleashAPI(properties[Key("UNLEASH_SERVER_API_URL", stringType)] + "/api/")
                .apiKey(properties[Key("UNLEASH_SERVER_API_TOKEN", stringType)])
                .environment(
                    when (System.getenv("NAIS_CLUSTER_NAME").orEmpty()) {
                        "prod-gcp" -> "production"
                        else -> "development"
                    },
                ).build(),
        )
    }
