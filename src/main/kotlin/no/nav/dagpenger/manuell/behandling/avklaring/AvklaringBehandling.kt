package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.manuell.behandling.hendelse.ManuellBehandlingAvklaring
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import java.util.UUID

internal data class AvklaringBehandling(
    val avklaring: Avklaring,
    val kode: String,
    val behandlingId: UUID,
    val ident: String,
    val context: MessageContext,
) {
    fun lagInformasjonsbehov(manuellBehandlingAvklaring: ManuellBehandlingAvklaring) =
        context.publish(
            JsonMessage
                .newNeed(
                    behov = listOf(avklaring.behov.name),
                    avklaring.behovKontekst(manuellBehandlingAvklaring) + mapOf("avklaringId" to avklaring.id.toString()),
                ).toJson(),
        )

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
    }
}
