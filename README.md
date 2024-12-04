# dp-manuell-behandling

## Beskrivelse

dp-manuell-behandling lytter på avklaringer (`NyAvklaring` hendelse på rapid) fra [dp-behandling](https://github.com/navikt/dp-behandling/tree/main/docs#avklaringer) og oversetter disse til informasjonsbehov. Lytter på løsninger for informasjonsbehov, tolker svaret og sender ut `AvklaringIKkeRelevant` avklaringer som ikke er relevante (basert på svaret på informasjonsbehovet). 


Se hvilke avklaringer den behandler er i [dokumentasjonen](avklaring-behov.approved.md)


## Komme i gang

Gradle brukes som byggverktøy og er bundlet inn.

`./gradlew build`

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* André Roaldseth, andre.roaldseth@nav.no
* Eller en annen måte for omverden å kontakte teamet på

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #poa-arbeid-dev.
