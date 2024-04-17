package no.nav.dagpenger.manuell.behandling.avklaring

internal val VirkningstidspunktFramITid = {
    EnkelAvklaring(
        begrunnelse = "Virkningstidspunkt er for langt fram i tid",
        varsel = Behandlingsvarsler.VIRKNINGSTIDSPUNKT_FRAM_I_TID,
    ) {
        it.behandlingsdato.isAfter(it.opprettet.toLocalDate().plusWeeks(2))
    }
}
