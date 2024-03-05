package no.nav.dagpenger.manuell.behandling.hendelse

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.AktivitetsloggHendelse
import no.nav.dagpenger.aktivitetslogg.IAktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import java.util.UUID

internal abstract class PersonHendelse(
    private val meldingsreferanseId: UUID,
    private val ident: String,
    private val aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : IAktivitetslogg by aktivitetslogg, AktivitetsloggHendelse {
    override fun ident() = ident

    override fun meldingsreferanseId() = meldingsreferanseId

    override fun toSpesifikkKontekst() = SpesifikkKontekst(this.javaClass.simpleName, mapOf("ident" to ident) + kontekstMap())

    fun toLogString(): String = aktivitetslogg.toString()

    open fun kontekstMap(): Map<String, String> = emptyMap()
}
