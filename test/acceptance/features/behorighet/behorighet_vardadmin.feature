# language: sv
@behorighet @vårdadmin 
Egenskap: Behörigheter för en vårdadministratör

Bakgrund: Logga in och gå in på en patient
	Givet att jag är inloggad som vårdadministratör
	Så ska det finnas en länk med texten "Frågor & svar"
	Och går in på Sök/skriv intyg
	Och jag går in på en patient
	
@fornya @lisjp
Scenario: Kan förnya Läkarintyg för sjukpenning
   När jag går in på ett "Läkarintyg för sjukpenning" med status "Signerat"
   Så ska det finnas en knapp för att förnya intyget

@fornya @ts
Scenario: Kan inte förnya Transportstyrelsens läkarintyg 
   När jag går in på ett "Transportstyrelsens läkarintyg" med status "Signerat"
   Så ska det inte finnas en knapp för att förnya intyget

@diabetes @fornya @ts
Scenario: Kan förnya Transportstyrelsens läkarintyg, diabetes intyg
   När jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Signerat"
   Så ska det inte finnas en knapp för att förnya intyget

@lisjp
Scenario: Kan inte signera Läkarintyg för sjukpenning
	Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg för sjukpenning"
    Och jag går in på utkastet
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig
	
@skriv-ut @ts @bas
Scenario: Kan inte signera Transportstyrelsens läkarintyg
	När jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig

	Och ska det finnas en knapp för att skriva ut utkastet

@diabetes @skriv-ut
Scenario: Kan inte signera Transportstyrelsens läkarintyg, diabetes
	När jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Så ska signera-knappen inte vara synlig

	Och ska det finnas en knapp för att skriva ut utkastet



