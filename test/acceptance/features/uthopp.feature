# language: sv

@uthopp @uthoppsläge
Egenskap: Uthoppsläge Fk7263

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som uthoppsläkare

@fråga-uthopp @mailnotifikation @waitingForFix
Scenario: Mail när fråga på intyg inkommer
När jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten
Och jag skickar intyget direkt till Försäkringskassan
Och Försäkringskassan ställer en "Kontakt" fråga om intyget 
Så ska jag få ett mejl med ämnet "Försäkringskassan har ställt en fråga angående ett intyg"

@fråga-till-fk
Scenario: Skicka fråga till Försäkringskassan
	När jag skickar ett "Läkarintyg FK 7263" intyg till Intygstjänsten
	Och jag skickar intyget direkt till Försäkringskassan
	Och jag går in på intyget via uthoppslänk
	Och jag skickar en fråga med ämnet "Arbetstidsförläggning" till Försäkringskassan
	Så ska ett info-meddelande visa "Frågan är skickad till Försäkringskassan"
	Och ska jag se min fråga under ohanterade frågor