/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*globals element, by, Promise, protractor, browser */
'use strict';

var FkBaseUtkast = require('../fk.base.utkast.page.js');

function sendKeysWithBackspaceFix(el, text) {
    return el.sendKeys(text)
        .then(function() {
            return el.sendKeys(protractor.Key.BACK_SPACE);
        })
        .then(function() {
            return el.sendKeys(text.substr(text.length - 1));
        });
}

function sendEnterToElement(el) {
    return function() {
        el.sendKeys(protractor.Key.ENTER);
    };
}

var intellektuellForm = element(by.id('form_funktionsnedsattningIntellektuell'));
var kommunikationForm = element(by.id('form_funktionsnedsattningKommunikation'));
var koncentrationForm = element(by.id('form_funktionsnedsattningKoncentration'));
var psykiskForm = element(by.id('form_funktionsnedsattningPsykisk'));
var horselTalForm = element(by.id('form_funktionsnedsattningSynHorselTal'));
var balansForm = element(by.id('form_funktionsnedsattningBalansKoordination'));
var annanForm = element(by.id('form_funktionsnedsattningAnnan'));

var avslutadForm = element(by.id('form_avslutadBehandling'));
var planeradForm = element(by.id('form_planeradBehandling'));
var pagaendeForm = element(by.id('form_pagaendeBehandling'));
var substansintagForm = element(by.id('form_substansintag'));

function getCheckbox(el) {
    return el.element(by.css('input'));
}

function getTextarea(el) {
    return el.element(by.css('textarea'));
}

function sendTextToForm(textEL, text) {
    return textEL.sendKeys(text)
        .then(function() {
            console.log('OK - Angav: ' + text);
        }, function(reason) {
            throw ('FEL - Angav: ' + text + ' ' + reason);
        });
}

var BaseSmiUtkast = FkBaseUtkast._extend({
    init: function init() {
        init._super.call(this);


        this.at = element(by.css('.edit-form'));

        this.togglerelatedIntygList = element(by.id('toggleShowRelatedIntyg'));
        this.relatedIntygList = {
            isDisplayed: function() {
                return element(by.id('intygRelations')).isDisplayed();
            },
            rows: function() {
                return element.all(by.css('#intygRelations tr'));
            },
            row: function(index) {
                var rowTds = element.all(by.css('#intygRelations tr:nth-child(' + index + ') td'));
                return {
                    visa: {
                        getText: function() {
                            return rowTds.get(0).getText();
                        },
                        click: function() {
                            rowTds.get(0).element(by.css('button')).sendKeys(protractor.Key.SPACE);
                        }
                    },
                    relation: rowTds.get(1),
                    status: rowTds.get(2),
                    datum: rowTds.get(3)
                };
            }
        };

        this.diagnoseCode = element(by.id('diagnoseCode-0'));
        this.aktivitetsbegransning = element(by.id('aktivitetsbegransning'));
        this.ovrigt = element(by.id('ovrigt'));
        this.tillaggsfragor0svar = this.getTillaggsfraga(0);
        this.tillaggsfragor1svar = this.getTillaggsfraga(1);

        this.funktionsnedsattning = {
            intellektuell: {
                checkbox: getCheckbox(intellektuellForm),
                text: getTextarea(intellektuellForm)

            },
            kommunikation: {
                checkbox: getCheckbox(kommunikationForm),
                text: getTextarea(kommunikationForm)
            },

            koncentration: {
                checkbox: getCheckbox(koncentrationForm),
                text: getTextarea(koncentrationForm)
            },

            annanPsykisk: {
                checkbox: getCheckbox(psykiskForm),
                text: getTextarea(psykiskForm)
            },

            synHorselTal: {
                checkbox: getCheckbox(horselTalForm),
                text: getTextarea(horselTalForm)

            },

            balansKoordination: {
                checkbox: getCheckbox(balansForm),
                text: getTextarea(balansForm)
            },

            annanKroppslig: {
                checkbox: getCheckbox(annanForm),
                text: getTextarea(annanForm)
            }
        };

        this.medicinskBehandling = {
            avslutad: {
                checkbox: getCheckbox(avslutadForm),
                text: getTextarea(avslutadForm)
            },
            pagaende: {
                checkbox: getCheckbox(pagaendeForm),
                text: getTextarea(pagaendeForm)
            },
            planerad: {
                checkbox: getCheckbox(planeradForm),
                text: getTextarea(planeradForm)
            },
            substansintag: {
                checkbox: getCheckbox(substansintagForm),
                text: getTextarea(substansintagForm)
            }
        };

        this.andraMedicinskaUtredningar = {
            finns: {
                JA: element(by.id('underlagFinnsYes')),
                NEJ: element(by.id('underlagFinnsNo'))
            },
            underlagRow: function(index) {
                return {
                    underlag: element(by.id('underlag-' + index + '-typ')),
                    datum: element(by.id('underlag-' + index + '-datum')),
                    information: element(by.id('underlag-' + index + '-hamtasFran'))
                };
            }
        };

        this.sjukdomsforlopp = element(by.id('sjukdomsforlopp'));
        this.diagnos = {
            diagnosRow: function(index) {
                return {
                    kod: element(by.id('diagnoseCode-' + index))
                };

            },
            narOchVarStalldesDiagnoser: element(by.id('diagnosgrund')),
            skalTillNyBedomning: {
                JA: element(by.id('nyBedomningDiagnosgrundYes')),
                NEJ: element(by.id('nyBedomningDiagnosgrundNo'))
            },
            diagnosForNyBedomning: element(by.id('diagnosForNyBedomning'))

        };

        this.ovrigt = element(by.id('ovrigt'));
        this.kontaktMedFK = element(by.id('form_kontaktMedFk')).element(by.css('input'));
        this.anledningTillKontakt = element(by.id('anledningTillKontakt'));
        //enhetsadress lika för alla SMI-intyg
        this.enhetensAdress = {
            postAdress: element(by.id('grundData.skapadAv.vardenhet.postadress')),
            postNummer: element(by.id('grundData.skapadAv.vardenhet.postnummer')),
            postOrt: element(by.id('grundData.skapadAv.vardenhet.postort')),
            enhetsTelefon: element(by.id('grundData.skapadAv.vardenhet.telefonnummer'))
        };
    },
    angeBaseratPa: function(baseratPa) {
        var promiseArr = [];
        if (baseratPa.minUndersokningAvPatienten) {
            promiseArr.push(sendKeysWithBackspaceFix(this.baseratPa.minUndersokningAvPatienten.datum, baseratPa.minUndersokningAvPatienten));
        }
        if (baseratPa.journaluppgifter) {
            promiseArr.push(sendKeysWithBackspaceFix(this.baseratPa.journaluppgifter.datum, baseratPa.journaluppgifter));

        }
        if (baseratPa.anhorigsBeskrivning) {
            promiseArr.push(sendKeysWithBackspaceFix(this.baseratPa.anhorigBeskrivning.datum, baseratPa.anhorigsBeskrivning));

        }

        if (baseratPa.annat) {
            var annatEl = this.baseratPa.annat;
            promiseArr.push(
                sendKeysWithBackspaceFix(annatEl.datum, baseratPa.annat)
                .then(function() {
                    return annatEl.beskrivning.sendKeys(baseratPa.annatBeskrivning);
                })
            );
        }

        if (baseratPa.personligKannedom) {
            promiseArr.push(sendKeysWithBackspaceFix(this.baseratPa.kannedomOmPatient.datum, baseratPa.personligKannedom));
        }

        return Promise.all(promiseArr);

    },
    angeFunktionsnedsattning: function(nedsattning) {
        var fn = this.funktionsnedsattning;
        return Promise.all([
            sendTextToForm(fn.intellektuell.text, nedsattning.intellektuell),
            sendTextToForm(fn.kommunikation.text, nedsattning.kommunikation),
            sendTextToForm(fn.koncentration.text, nedsattning.koncentration),
            sendTextToForm(fn.annanPsykisk.text, nedsattning.psykisk),
            sendTextToForm(fn.synHorselTal.text, nedsattning.synHorselTal),
            sendTextToForm(fn.balansKoordination.text, nedsattning.balansKoordination),
            sendTextToForm(fn.annanKroppslig.text, nedsattning.annan)
        ]);
    },

    angeAndraMedicinskaUtredningar: function(utredningar) {
        var utredningarElement = this.andraMedicinskaUtredningar;

        var fillIn = function fillInUtr(val, index) {
            var row = utredningarElement.underlagRow(index);

            return Promise.all([
                sendKeysWithBackspaceFix(row.datum, val.datum),
                row.underlag.click(),
                row.underlag.element(by.cssContainingText('div', val.underlag)).click(),
                row.information.sendKeys(val.infoOmUtredningen)
            ]);
        };

        if (utredningar) {
            return utredningarElement.finns.JA.sendKeys(protractor.Key.SPACE)
                .then(function() {
                    browser.sleep(2000);
                    var actions = utredningar.map(fillIn);
                    return actions;
                });
        } else {
            return utredningarElement.finns.NEJ.sendKeys(protractor.Key.SPACE);
        }
    },
    angeSjukdomsforlopp: function(forlopp) {
        return this.sjukdomsforlopp.sendKeys(forlopp);
    },
    angeDiagnosKoder: function(diagnoser) {
        var promiseArr = [];
        for (var i = 0; i < diagnoser.length; i++) {
            if (i !== 0) {
                promiseArr.push(this.diagnos.laggTillDiagnosKnapp.sendKeys(protractor.Key.SPACE));
            }
            var row = this.diagnos.diagnosRow(i);
            promiseArr.push(row.kod.sendKeys(diagnoser[i].kod).then(sendEnterToElement(row.kod)));

        }
        return Promise.all(promiseArr);

    },
    angeDiagnos: function(diagnosObj) {
        var diagnoser = diagnosObj.diagnoser;
        var promiseArr = [];


        //Ange diagnoser
        promiseArr.push(this.angeDiagnosKoder(diagnoser));

        //Ange när och var diagnoser ställts
        promiseArr.push(this.diagnos.narOchVarStalldesDiagnoser.sendKeys(diagnosObj.narOchVarStalldesDiagnoserna));

        //Ange Finns skäl till ny bedömning
        var nyBedomning = this.diagnos.skalTillNyBedomning.NEJ;
        if (diagnosObj.nyBedomning) {
            nyBedomning = this.diagnos.skalTillNyBedomning.JA;
        }
        var diagnosForNyBedomning = this.diagnos.diagnosForNyBedomning;
        promiseArr.push(nyBedomning.sendKeys(protractor.Key.SPACE).then(function() {
            if (diagnosObj.nyBedomning) {
                //Ange diagnosForNyBedomning
                return browser.sleep(1000).then(function() { //fix för nåt med animering
                    return diagnosForNyBedomning.sendKeys(diagnosObj.diagnosForNyBedomning);
                });
            } else {
                return Promise.resolve();
            }
        }));

        return Promise.all(promiseArr);
    },
    angeOvrigaUpplysningar: function(ovrigt) {
        return this.ovrigt.sendKeys(ovrigt);
    },
    angeKontaktMedFK: function(kontakt) {
        if (kontakt) {
            return this.kontaktMedFK.sendKeys(protractor.Key.SPACE);
        } else {
            return Promise.resolve();
        }
    },
    angeTillaggsfragor: function(svarArr) {
        if (!svarArr) {
            return Promise.resolve();
        } else {
            var promiseArr = [];
            for (var i = 0; i < svarArr.length; i++) {
                promiseArr.push(this.getTillaggsfraga(i).sendKeys(svarArr[i].svar));
            }
            return Promise.all(promiseArr);
        }

    },
    angeMedicinskBehandling: function(behandling) {
        var mb = this.medicinskBehandling;
        return Promise.all([
            sendTextToForm(mb.avslutad.text, behandling.avslutad),
            sendTextToForm(mb.pagaende.text, behandling.pagaende),
            sendTextToForm(mb.planerad.text, behandling.planerad),
            sendTextToForm(mb.substansintag.text, behandling.substansintag)
        ]);
    },
    getTillaggsfraga: function(i) {
        return element(by.id('tillaggsfragor[' + i + '].svar'));
    },
    getTillaggsfragaText: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + ' label')).getText();
    },
    getTillaggsfragaSvar: function(i) {
        return element(by.css('#form_tillaggsfragor_' + i + ' textarea')).getAttribute('value');
    }
});

module.exports = BaseSmiUtkast;
