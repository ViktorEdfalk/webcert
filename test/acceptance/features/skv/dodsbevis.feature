# language: sv

@db @dodsbevis @skatteverket @skv @notReady
Egenskap: Dödsbevis

Bakgrund: Jag befinner mig på webcerts förstasida
		  Givet att jag är inloggad som läkare

@signera
Scenario: Kan signera dödsbevisintyg 
          När jag går in på en patient
		  Och jag går in på att skapa ett "Dödsbevis" intyg
		  Och jag fyller i alla nödvändiga fält för intyget
		  Och jag signerar intyget
		  Och jag ska se den data jag angett för intyget
		  
@sekretessmarkering
Scenario: Ska inte kunna utfärda DOI på patienter med sekretessmarkering
	När jag går in på en patient med sekretessmarkering
	Så ska jag inte kunna skapa ett "Dödsorsaksintyg" intyg