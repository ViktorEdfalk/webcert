/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../../support/AF_intyg/af00251Intyg'
// import * as pdl from '../../../support/pdl_helpers'

// // LISJP = Läkarintyg för sjukpenning, FK 7804

// var pdlEventArray = [];

// function Af00251PdlEvent(env, actType, actArgs, actLevel, assignment, vgId_mod, vgNamn_mod, veId_mod, veNamn_mod) {
//     return pdl.pdlEvent(env, actType, actArgs, actLevel, env.vårdpersonal.hsaId, assignment, env.vårdpersonal.titel, vgId_mod, vgNamn_mod, veId_mod, 
//         veNamn_mod, env.vårdtagare.personnummerKompakt, env.vårdenhet.vårdgivareId, env.vårdenhet.vårdgivareNamn, env.vårdenhet.id, env.vårdenhet.namn)   
// };

describe('AF00251-intyg', function () {
    
    before(function() {
        cy.fixture('AF_intyg/minAF00251Data').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
    })

    beforeEach(function() {
        // pdlEventArray = [];
        cy.skapaAF00251Utkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("AF00251-utkast med id " + utkastId + " skapat och används i testfallet");
            // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar en minimalt ifylld AF00251', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        cy.visit(önskadUrl);

        // Om vi dirigeras till sidan som säger att 'Intygsutkastet är raderat'
        // så försöker vi igen eftersom det antagligen gick för snabbt.
        cy.get('body').then(($body) => {
            if ($body.text().includes('Intygsutkastet är raderat och kan därför inte längre visas.')) {
                cy.log("Kom till 'Intygetsutkastet är raderat', antagligen gick det för snabbt. Provar igen.");
                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet); // Vi behöver logga in igen
                cy.visit(önskadUrl);
            }
        });
        cy.url().should('include', this.utkastId);
        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionGrundMedicinsktUnderlag(this.intygsdata.medicinsktUnderlag);
        intyg.sektionArbetsmarknadspolitisktProgram(this.intygsdata.arbetsmarknadspolitiskt);
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        cy.contains("Utkastet är sparat").should('exist');

        // Lite special logga ut/logga in -variant för att sedan öppna intyget på nytt med en ny session
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(önskadUrl);
        cy.url().should('include', this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i resten av intyget
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionSjukdomensKonsekvenser(this.intygsdata.konsekvenser);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Signerar intyget och populerar pdl-arrayen med förväntade logposter "Signera" och "Läsa"
        intyg.signera();
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.SIGNERA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Skickar intyget till Arbetsförmeddlingen. Populerar pdl-arrayen med förväntad loggpost "Utskrift" med argument att det är skickat till AF
        intyg.skickaTillAf();
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.AF, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Introducerar en wait då skrivUt går så fort att man riskerar att få samma timestamp som för "skicka"
        cy.wait(1500);

        // Skriver ut intyget samt populerar pdl-arrayen med förväntad logpost "Utskrift"
        intyg.skrivUt("fullständigt", this.utkastId, "af00251");
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.UTSKRIFT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        cy.log("Testar SJF");

        // Lite special logga ut/logga in -variant för att sedan öppna intyget på nytt med en ny session och SJF (Sammanhållen journalföring)
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet_2);

        const sjfUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet_2.id + "&sjf=true";
        cy.visit(sjfUrl);

        // Om vi inte väntar på (valfritt) elementet nedan i intyget
        // så kommer "utskrift" att inträffa före "läsa"
        cy.contains("Arbetsmarknadspolitiskt program");

        cy.url().should('include', this.utkastId);
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.LÄSASJF, this.utkastId, this.vårdenhet_2.uppdragsnamn, this.vårdenhet_2.vårdgivareId, this.vårdenhet_2.vårdgivareNamn, this.vårdenhet_2.id, this.vårdenhet_2.namn));
        cy.log(this.utkastId + this.vårdenhet_2.vårdgivareId + this.vårdenhet_2.vårdgivareNamn + this.vårdenhet_2.id + this.vårdenhet_2.namn);

        // Skriver ut intyget samt populerar pdl-arrayen med förväntad logpost "Utskrift"
        // intyg.skrivUt("fullständigt", this.utkastId, "af00251");
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.UTSKRIFTSJF, 
        //     this.utkastId, this.vårdenhet_2.uppdragsnamn, this.vårdenhet_2.vårdgivareId, this.vårdenhet_2.vårdgivareNamn, this.vårdenhet_2.id, this.vårdenhet_2.namn));

        cy.log("Testar återigen utan SJF");

        // Lite special logga ut/logga in -variant för att sedan öppna intyget på nytt med en ny session
        cy.clearCookies();
        cy.visit('/logout');
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        cy.visit(önskadUrl);
        cy.contains("Arbetsmarknadspolitiskt program"); // Vänta på att intyget ska laddas färdigt
        // pdlEventArray.push(Af00251PdlEvent(this, "Läsa", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: Bug?! Varför blir det 2 "Läsa" på rad?
        // pdlEventArray.push(Af00251PdlEvent(this, "Läsa", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Förnya intyget -> utkast skapas. Populerar pdl-arrayen med förväntade logposter "Skriva" och "Läsa" samt nytt intygsID.
        cy.url().should('include', this.utkastId);
        intyg.ersatta();
        cy.contains("Arbetsmarknadspolitiskt program"); // Vänta på att intyget ska laddas färdigt
        cy.get('.intygs-id').invoke('text').then((text1) => {
            var intygsID_2 = text1.replace(/\s/g, '');
            intygsID_2 = intygsID_2.substring(intygsID_2.length-36, intygsID_2.length);
            cy.log('IntygsID fönyande utkast: ' + intygsID_2);
            cy.wrap(intygsID_2).as('intygsID_2');
        });
        // cy.get('@intygsID_2').then((intygID_2)=> {
        //     pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, intygID_2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        //     pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.LÄSA, undefined, intygID_2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        // });

        // Skriva ut utkast. Populerar PDL-arrayen med förväntad logpost "Utskrift" 
        cy.get('@intygsID_2').then((intygID_2)=> {
            intyg.skrivUt("fullständigt", intygID_2, "af00251");
            // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.UTSKRIFTUTKAST, intygID_2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        });

        // Raderar intygsutkast
        intyg.raderaUtkast();
        // cy.get('@intygsID_2').then((intygID_2)=> {
        //     pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.RADERA, undefined, intygID_2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        // });

        // Redirect till originalintyget genererar en ny "Läsa"
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Makulerar ursprungsintyget
        // intyg.makuleraIntyg('Annat allvarligt fel');
        intyg.makuleraIntyg();
        // pdlEventArray.push(Af00251PdlEvent(this, pdl.enumHandelse.MAKULERA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // cy.verifieraPdlLoggar(pdlEventArray);
    });
});
