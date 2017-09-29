#language: sv

@SRS
Egenskap: Webcert visa information från SRS i form av prediktioner på sjukskrivningslängd, statistikbilder och åtgärdsförslag

Bakgrund: 
    Givet att jag är djupintegrerat inloggad som läkare på vårdenhet "med SRS"
    

@SRS-US-W01 @allmänt
@SRS-US-W02 @åtgärder
@SRS-US-W03 @statistik
@SRS-US-W04 @prediktion
Scenario: När samtycke är givet och ytterligare frågor besvarade ska information från SRS visas.
    Givet en patient som "inte har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Och jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    När jag anger att patienten samtycker till SRS
    Och jag fyller i ytterligare svar för SRS
    Och jag trycker på knappen "Visa"
    Så ska prediktion från SRS-tjänsten visas
    Och ska åtgärdsförslag från SRS-tjänsten visas
    När jag trycker på fliken "Statistik"
    Så ska en statistikbild från SRS-tjänsten visas


@SRS-US-W01 @allmänt
Scenario: SRS-knappen ska bara visas när diagnos som har stöd för SRS är ifylld
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Så ska knappen för SRS vara i läge "stängd"
    När jag fyller i diagnoskod som "inte finns i SRS"
    Så ska knappen för SRS vara i läge "gömd"

@SRS-US-W01 @allmänt
Scenario: Samtycken som patienter har givit ska lagras
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    Och jag fyllt i diagnoskod som "finns i SRS"
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    Och frågan om samtycke ska vara förifylld med "Ja"

@SRS-US-W01 @allmänt
Scenario: Patient som inte givit samtycke ska ha samtyckesfrågan förifyllt som "nej"
    Givet en patient som "inte har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    Och jag fyllt i diagnoskod som "finns i SRS"
    När jag klickar på knappen för SRS
    Så ska en fråga om samtycke visas
    Och frågan om samtycke ska vara förifylld med "Nej"

@SRS-US-W01 @allmänt
Scenario: Användaren ska kunna visa och dölja UI-komponenter som hör till SRS
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "finns i SRS"
    Så ska knappen för SRS vara i läge "stängd"
    När jag klickar på knappen för SRS
    Och ska en frågepanel för SRS "visas"
    Och ska en pil med texten "Visa mindre" visas
    När jag klickar på pilen
    Så ska frågepanelen för SRS vara "minimerad"
    Och ska en pil med texten "Visa mer" visas
    När jag klickar på pilen
    Så ska frågepanelen för SRS vara "maximerad"

@SRS-US-W01 @allmänt
@notReady
Scenario: Prediktion ska kunna visa förhöjd risk
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "har förhöjd risk"
    Och jag klickar på knappen för SRS
    Och jag trycker på knappen "Visa"
    Så ska meddelandet "Förhöjd risk" visas


@SRS-US-W02 @åtgärder
Scenario: Användaren ska kunna ta del av åtgärdsförslag från SRS
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "har åtgärder"
    Och jag klickar på knappen för SRS
    Så ska REK-åtgärder från "åtgärdslista 1" visas
    Och ska OBS-åtgärder från "åtgärdslista 2" visas


@SRS-US-W02 @åtgärder
Scenario: När åtgärdsförslag inte kan ges ska korrekt felmeddelande visas
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "saknar åtgärder"
    Och jag klickar på knappen för SRS
    Så ska felmeddelandet "finns ingen SRS-information för detta fält" visas

@SRS-US-W03 @statistik
Scenario: När statistikbild för en viss diagnoskod saknas ska användaren informeras.
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "saknar statistik"
    Och jag klickar på knappen för SRS
    Och jag trycker på fliken "Statistik"
    Så ska felmeddelandet "finns ingen SRS-information för detta fält" visas

@SRS-US-W04 @prediktion
@notReady
Scenario: När prediktion inte kan ges ska korrekt felmeddelande visas
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "saknar prediktion"
    Och jag klickar på knappen för SRS
    Och jag trycker på knappen "Visa"
    Så ska felmeddelandet "finns ingen SRS-information för detta fält" visas

@SRS-US-W04 @prediktion
@notReady
Scenario: Prediktion ska kunna visa ingen förhöjd risk
    Givet en patient som "har givit samtycke" till SRS
    Och att jag befinner mig på ett nyskapat Läkarintyg FK 7263
    När jag fyller i diagnoskod som "för ingen förhöjd risk"
    Och jag klickar på knappen för SRS
    Och jag trycker på knappen "Visa"
    Så ska meddelandet "Ingen förhöjd risk" visas

