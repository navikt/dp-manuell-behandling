package no.nav.dagpenger.manuell

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.overriding

object Configuration {
    val properties =
        ConfigurationProperties.systemProperties() overriding EnvironmentVariables()

    val config: Map<String, String> =
        properties.list().reversed().fold(emptyMap()) { map, pair ->
            map + pair.second
        }
}
