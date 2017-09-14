# language: sv
@behorighet @djupintegration @lakare
# PRIVILEGE_NAVIGERING
Egenskap: Behörigheter för en djupintegrerad läkare

Bakgrund: Logga in och gå in på en patient
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	Och jag går in på en patient

@makulera @lisjp
Scenario: Kan makulera sjukintyg
	När jag går in på ett "Läkarintyg för sjukpenning" med status "Signerat"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"

@fornya @lisjp @skriv-ut
Scenario: Kan förnya och signera ett läkarintyg
	När jag går in på ett "Läkarintyg för sjukpenning" med status "Signerat"
   Så ska det finnas en knapp för att förnya intyget
   
   Och ska det finnas en knapp för att skriva ut intyget

@skriv-ut @signera @ts
Scenario: Signera ett ts-intyg
   När att vårdsystemet skapat ett intygsutkast för slumpat TS-intyg
   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag fyller i alla nödvändiga fält för intyget
   Så ska det finnas en knapp för att skriva ut utkastet

   När jag signerar intyget
   Så ska det finnas en knapp för att skriva ut intyget


@komplettera @fk7263 @waitingForFix
Scenario: Besvara kompletteringsfråga
   ###### TODO ändra till att testa med lisjp intyg ##########################
   #När jag går in på ett "Läkarintyg för sjukpenning" med status "Skickat"           -
   
   När jag går in på ett "Läkarintyg FK 7263" med status "Skickat"
   
   #Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
   #Och jag går in på utkastet
   #Och jag fyller i alla nödvändiga fält för intyget
   #Och jag signerar intyget
   #Och jag skickar intyget till Försäkringskassan 
   ###########################################################################
   
   Och Försäkringskassan ställer en "Kontakt" fråga om intyget
   Och jag svarar på frågan
   Så kan jag se mitt svar under hanterade frågor

Scenario: Svara med nytt intyg
   ###### TODO ändra till att testa med lisjp intyg ##########################
   #När jag går in på ett "Läkarintyg för sjukpenning" med status "Skickat"           -
   
   När jag går in på ett "Läkarintyg FK 7263" med status "Skickat"
   
   #Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
   #Och jag går in på utkastet
   #Och jag fyller i alla nödvändiga fält för intyget
   #Och jag signerar intyget
   #Och jag skickar intyget till Försäkringskassan 
   ###########################################################################
   
   Och Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget

   När jag går in på intygsutkastet via djupintegrationslänk
   Och jag väljer att svara med ett nytt intyg
   Så ska jag se kompletteringsfrågan på utkast-sidan

   När jag signerar och skickar kompletteringen
   Och ska intygets status vara "Intyget är signerat"

  
