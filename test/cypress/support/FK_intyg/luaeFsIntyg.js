// Intyget är uppdelat på samma sätt som det är uppdelat när det fylls i genom WebCert

// Datumen är inte specifika för något testfall

// -------------------- 'Grund för medicinskt underlag' --------------------
export function sektionGrundFörMedicinsktUnderlag(medUnderlag) {
    const idagMinus5    = Cypress.moment().subtract(5,   'days').format('YYYY-MM-DD');
    const idagMinus6    = Cypress.moment().subtract(6,   'days').format('YYYY-MM-DD');
    const idagMinus14   = Cypress.moment().subtract(14,  'days').format('YYYY-MM-DD');
    const idagMinus15   = Cypress.moment().subtract(15,  'days').format('YYYY-MM-DD');
    const idagMinus2Mån = Cypress.moment().subtract(2, 'months').format('YYYY-MM-DD');

    const utlåtandeBaseratPå = medUnderlag.utlåtandetÄrBaseratPå;
    if (utlåtandeBaseratPå.minUndersökning) {
        // TODO: Lägger väldigt lång timeout vid första elementet i intyget eftersom
        // sidan ibland inte har hunnit ladda innan den får timeout.
        // Initial analys är att Jenkins är överbelastad då det verkar fungera bra när
        // man kör lokalt.
        cy.get('#checkbox_undersokningAvPatienten', {timeout: 60000}).check();
        cy.get("#datepicker_undersokningAvPatienten").clear().type(idagMinus5);
    }

    if (utlåtandeBaseratPå.journaluppgifter) {
        cy.get('#checkbox_journaluppgifter').check();
        cy.get("#datepicker_journaluppgifter").clear().type(idagMinus15);
    }

    if (utlåtandeBaseratPå.anhörigsBeskrivning) {
        cy.get('#checkbox_anhorigsBeskrivningAvPatienten').check();
        cy.get("#datepicker_anhorigsBeskrivningAvPatienten").clear().type(idagMinus6);
    }

    if (utlåtandeBaseratPå.annat) {
        cy.get('#checkbox_annatGrundForMU').check();
        cy.get("#datepicker_annatGrundForMU").clear().type(idagMinus14);

        // cy.type() tar bara in text eller nummer (så vi behöver inte verifiera värdet)
        cy.get("#annatGrundForMUBeskrivning").type(utlåtandeBaseratPå.annatText);
    }

    cy.get("#datepicker_kannedomOmPatient").clear().type(idagMinus14);

    const andraUtrEllerUnderlag = medUnderlag.andraUtredningarEllerUnderlag;
    if (andraUtrEllerUnderlag.ja) {
        cy.get("#underlagFinnsYes").click();

        // TODO: Ska nedanstående brytas ut till en funktion och anropas tre gånger istället?
        if (andraUtrEllerUnderlag.rad1) {
            const rad = andraUtrEllerUnderlag.rad1;
            cy.get("#underlag-0--typ").click();
            cy.get("#wcdropdown-underlag-0--typ")
            .contains(rad.underlagstyp)
            .then(option => {
                cy.wrap(option).contains(rad.underlagstyp); // Säkerställ att rätt alternativ valts
                option[0].click(); // jquery "click()", inte Cypress "click()"
            });

            cy.get("#datepicker_underlag\\[0\\]\\.datum").clear().type(idagMinus2Mån);
            cy.get("#underlag-0--hamtasFran").type(rad.underlagHämtasFrån);
        }

        if (andraUtrEllerUnderlag.rad2) {
            expect(andraUtrEllerUnderlag.rad1).to.exist;
            const rad = andraUtrEllerUnderlag.rad2;
            cy.get("#underlag-1--typ").click();
            cy.get("#wcdropdown-underlag-1--typ")
            .contains(rad.underlagstyp)
            .then(option => {
                cy.wrap(option).contains(rad.underlagstyp); // Säkerställ att rätt alternativ valts
                option[0].click(); // jquery "click()", inte Cypress "click()"
            });

            cy.get("#datepicker_underlag\\[1\\]\\.datum").clear().type(idagMinus2Mån);
            cy.get("#underlag-1--hamtasFran").type(rad.underlagHämtasFrån);
        }

        if (andraUtrEllerUnderlag.rad3) {
            expect(andraUtrEllerUnderlag.rad2).to.exist;
            const rad = andraUtrEllerUnderlag.rad3;
            cy.get("#underlag-2--typ").click();
            cy.get("#wcdropdown-underlag-2--typ")
            .contains(rad.underlagstyp)
            .then(option => {
                cy.wrap(option).contains(rad.underlagstyp); // Säkerställ att rätt alternativ valts
                option[0].click(); // jquery "click()", inte Cypress "click()"
            });

            cy.get("#datepicker_underlag\\[2\\]\\.datum").clear().type(idagMinus2Mån);
            cy.get("#underlag-2--hamtasFran").type(rad.underlagHämtasFrån);
        }
    } else {
        cy.get("#underlagFinnsNo").click();
    }
}

// -------------------- 'Diagnos' --------------------
export function sektionDiagnos(diagnos) {
    if (diagnos.rad1) {
        cy.get('#diagnoseCode-0').parent().within(($diagnoskodrad) => {
            cy.get('#diagnoseCode-0').type(diagnos.rad1.kod);
            cy.wrap($diagnoskodrad).contains(diagnos.rad1.text).click();
        });
        cy.get('#diagnoseDescription-0').invoke('val').should('contain', diagnos.rad1.text);
    }

    if (diagnos.rad2) {
        expect(diagnos.rad1).to.exist;
        cy.get('#diagnoseCode-1').parent().within(($diagnoskodrad) => {
            cy.get('#diagnoseCode-1').type(diagnos.rad2.kod);
            cy.wrap($diagnoskodrad).contains(diagnos.rad2.text).click();
        });
        cy.get('#diagnoseDescription-1').invoke('val').should('contain', diagnos.rad2.text);
    }

    if (diagnos.rad3) {
        expect(diagnos.rad2).to.exist;
        cy.get('#diagnoseCode-2').parent().within(($diagnoskodrad) => {
            cy.get('#diagnoseCode-2').type(diagnos.rad3.kod);
            cy.wrap($diagnoskodrad).contains(diagnos.rad3.text).click();
        });
        cy.get('#diagnoseDescription-2').invoke('val').should('contain', diagnos.rad3.text);
    }
}

// -------------------- 'Funktionsnedsättningar' --------------------
export function sektionFunktionsnedsättning(funkNedsättning) {
    if (funkNedsättning.debut) {
        cy.get("#funktionsnedsattningDebut").type(funkNedsättning.debut);
    }

    if (funkNedsättning.påverkan) {
        cy.get("#funktionsnedsattningPaverkan").type(funkNedsättning.påverkan);
    }
}

// -------------------- 'Övrigt' --------------------
export function sektionÖvrigt(övrigt) {
    cy.get("#ovrigt").type(övrigt.text);
}

// -------------------- 'Kontakt' --------------------
export function sektionKontakt(kontakt) {
    if (kontakt.ja) {
        cy.get("#kontaktMedFk").check();

        if (kontakt.text) {
            cy.get("#anledningTillKontakt").type(kontakt.text);
        }
    }
}

// -------------------- 'Signera intyget' --------------------
export function signera() {
    // TODO: Utan wait så tappas ofta slutet på texten bort i sista textboxen.
    // Antagligen hinner WebCert inte auto-spara innan man trycker på "signera".
    // Wait är dock ett anti-pattern så finns något annat sätt så är det att föredra.
    cy.wait(1000);

    cy.contains("Klart att signera");
    cy.contains("Obligatoriska uppgifter saknas").should('not.exist');
    cy.contains("Utkastet sparas").should('not.exist');

    // cy.click() fungerar inte alltid. Det finns ärenden rapporterade
    // (stängd pga inaktivitet):
    // https://github.com/cypress-io/cypress/issues/2551
    // https://www.cypress.io/blog/2019/01/22/when-can-the-test-click/ :
    // "If a tree falls in the forest and no one has attached a “fall” event listener, did it really fall?"

    const click = $el => { return $el.click() }

    // Parent() p.g.a. att ett element täcker knappen
    cy.get('#signera-utkast-button').parent().should('be.visible')

    cy.get('#signera-utkast-button')
    .pipe(click, {timeout: 60000}) // ToDo: Lång timeout (problem endast på Jenkins, överlastad slav?)
    .should($el => {
        expect($el.parent()).to.not.be.visible;
    })
}

// -------------------- 'Skicka intyget' --------------------
export function skickaTillFk() {
    cy.get("#sendBtn", { timeout: 60000 }).click();

    // Modal som dyker upp och frågar om man verkligen vill skicka
    cy.get("#button1send-dialog").click();
    cy.contains("Intyget är skickat till Försäkringskassan");
}

// -------------------- 'Skriv ut intyget' --------------------
export function skrivUt(typAvUtskrift, intygsId){
    switch(typAvUtskrift) {
        case "utkast":
        case "fullständigt":
            cy.request({
                method: 'GET',
                url: 'moduleapi/intyg/luae_fs/' + intygsId + "/pdf",
            });
            cy.log('Skriver ut ett ' + typAvUtskrift + ' intyg (via cy.request, ej grafiskt)');
            break;
        default:
            cy.log('Ingen korrekt typ av utskrift vald');
    }
}

// ------------------'Förnya intyg'---------------------------
export function fornya() {
    cy.get('#fornyaBtn').click();
}

// ------------------'Radera utkast'--------------------------
export function raderaUtkast() {
    cy.get('#ta-bort-utkast').click();
    cy.get('#confirm-draft-delete-button').click();
}

// ------------------'Makulera intyg'-------------------------
export function makuleraIntyg(arg) {
    cy.get('#makuleraBtn').click();
    if (arg === "Annat allvarligt fel") {
        cy.get('#reason-ANNAT_ALLVARLIGT_FEL').check();
        cy.get('#clarification-ANNAT_ALLVARLIGT_FEL').type('Testanledning');
        cy.get('#button1makulera-dialog').click();
    } else {
        cy.get('#reason-FEL_PATIENT').check();
        cy.get('#button1makulera-dialog').click();
    }
}
