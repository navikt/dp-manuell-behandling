package no.nav.dagpenger.manuell.behandling.avklaring

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import java.util.UUID

internal data class AvklaringBehandling(
    val avklaring: Avklaring,
    val kode: String,
    val behandlingId: UUID,
    val ident: String,
    val søknadId: UUID,
    val context: MessageContext,
) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    fun lagInformasjonsbehov(manuellBehandlingAvklaring: ManuellBehandlingAvklaring) {
        val behov =
            JsonMessage
                .newNeed(
                    behov = listOf(avklaring.behov.name),
                    avklaring.behovKontekst(manuellBehandlingAvklaring) +
                        mapOf(
                            "avklaringId" to avklaring.id.toString(),
                            "søknadId" to søknadId.toString(),
                            "behandlingId" to behandlingId.toString(),
                            "ident" to ident,
                        ),
                )
        behov.interestedIn("@behovId")
        withLoggingContext(
            "behovId" to behov["@behovId"].asText(),
            "avklaringId" to avklaring.id.toString(),
            "behandlingId" to behandlingId.toString(),
        ) {
            logger.info { "Publisere behov for ${avklaring.behov.name} for avklaring $kode" }
            context.publish(
                behov.toJson(),
            )
        }
    }

    fun publiserIkkeRelevant() {
        context.publish(
            JsonMessage
                .newMessage(
                    "AvklaringIkkeRelevant",
                    mutableMapOf(
                        "avklaringId" to avklaring.id.toString(),
                        "kode" to kode,
                        "behandlingId" to behandlingId.toString(),
                        "ident" to ident,
                    ),
                ).toJson(),
        )
        withLoggingContext(
            "avklaringId" to avklaring.id.toString(),
            "behandlingId" to behandlingId.toString(),
        ) {
            logger.info { "Publisert AvklaringIkkeRelevant for avklaring $kode" }
        }
    }
}
