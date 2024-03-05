package no.nav.dagpenger.manuell.behandling.hendelse

import java.time.LocalDate
import java.util.UUID

internal class ManuellBehandlingAvklaring(
    val behandlingsdato: LocalDate,
    meldingsreferanseId: UUID,
    fødselsnummer: String,
    søknadId: UUID,
) : SøknadHendelse(meldingsreferanseId, fødselsnummer, søknadId)

internal fun ManuellBehandlingAvklaring.legacyBehov() =
    mapOf(
        "Virkningstidspunkt" to behandlingsdato.toString(),
        "søknad_uuid" to søknadId.toString(),
        "identer" to listOf(mapOf("type" to "fnr", "historisk" to false, "id" to ident())),
    )
