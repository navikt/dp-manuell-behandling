package no.nav.dagpenger.manuell.behandling.avklaring

import no.nav.dagpenger.aktivitetslogg.Varselkode

object Behandlingsvarsler {
    @Suppress("ClassName")
    data object EØS_ARBEID : Varselkode2("Personen har oppgitt arbeid fra EØS")

    @Suppress("ClassName")
    data object SVANGERSKAPSRELATERTE_SYKEPENGER : Varselkode2("Personen har sykepenger som kan være svangerskapsrelaterte")

    @Suppress("ClassName")
    data object INNTEKT_NESTE_KALENDERMÅNED : Varselkode2("Personen har inntekter som tilhører neste inntektsperiode")

    @Suppress("ClassName")
    data object MULIG_GJENOPPTAK : Varselkode2("Personen har åpne saker i Arena som kan være gjenopptak")

    @Suppress("ClassName")
    data object LUKKEDE_SAKER_SISTE_8_UKER : Varselkode2("Personen har lukkede saker i Arena siste 8 uker")

    @Suppress("ClassName")
    data object VIRKNINGSTIDSPUNKT_FRAM_I_TID : Varselkode2("Virkningstidspunktet ligger mer enn 2 uker fram i tid")

    // TODO: Midlertidlig bridge til vi får fikset aktivitetsloggen
    abstract class Varselkode2(override val varseltekst: String) : Varselkode() {
        override fun toString() = "${this::class.java.simpleName}: $varseltekst"
    }
}
