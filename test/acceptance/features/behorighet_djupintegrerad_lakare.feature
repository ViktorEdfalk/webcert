# language: sv
@behorighet @djupintegration @lakare
# PRIVILEGE_NAVIGERING
Egenskap: Behörigheter för en djupintegrerad läkare

Bakgrund: Logga in och gå in på en patient
	Givet att jag är inloggad som djupintegrerad läkare
	Och jag går in på en patient

Scenario: Kan makulera sjukintyg
	När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"

@fornya
Scenario: Kan förnya och signera ett intyg
	När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Så ska det finnas en knapp för att förnya intyget

Scenario: Det går att förnya signerade och mottagna intyg från intygslistan
	Så ska Förnya-knappen visas för alla signerade eller mottagna "Läkarintyg FK 7263"-intyg

Scenario: Besvara kompletteringsfråga
   När jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   Och Försäkringskassan ställer en "Kontakt" fråga om intyget
   Och jag svarar på frågan
   Så kan jag se mitt svar under hanterade frågor

Scenario: Svara med nytt intyg
   När jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
   Och Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget

   När jag går in på intygsutkastet via djupintegrationslänk
   Och jag väljer att svara med ett nytt intyg
   Så ska jag se kompletteringsfrågan på utkast-sidan

   När jag signerar intyget
   Och jag skickar intyget till Försäkringskassan
   Och ska intygets status vara "Intyget är signerat"
