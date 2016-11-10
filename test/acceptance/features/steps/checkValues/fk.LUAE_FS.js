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

/* globals logger, pages, Promise, wcTestTools */

'use strict';

var luaefsPage = pages.intyg.luaeFS.intyg;
var testdataHelper = wcTestTools.helpers.testdata;


function checkBaseratPa(baseratPa) {
    var minUndersokningText = testdataHelper.dateToText((baseratPa.minUndersokningAvPatienten));
    var journaluppgifterText = testdataHelper.dateToText((baseratPa.journaluppgifter));
    var anhorigBeskrivningText = testdataHelper.dateToText((baseratPa.anhorigsBeskrivning));
    var annatText = testdataHelper.dateToText((baseratPa.annat));
    var annatBeskrivningText = testdataHelper.ejAngivetIfNull(baseratPa.annatBeskrivning);
    var personligKannedomText = testdataHelper.dateToText((baseratPa.personligKannedom));

    return Promise.all([
        expect(luaefsPage.baseratPa.minUndersokningAvPatienten.getText()).to.eventually.equal(minUndersokningText),
        expect(luaefsPage.baseratPa.journaluppgifter.getText()).to.eventually.equal(journaluppgifterText),
        expect(luaefsPage.baseratPa.anhorigsBeskrivning.getText()).to.eventually.equal(anhorigBeskrivningText),
        expect(luaefsPage.baseratPa.annat.getText()).to.eventually.equal(annatText),
        expect(luaefsPage.baseratPa.annatBeskrivning.getText()).to.eventually.equal(annatBeskrivningText),
        expect(luaefsPage.baseratPa.personligKannedom.getText()).to.eventually.equal(personligKannedomText)
    ]);
}

function checkAndraMedicinskaUtredningar(andraMedicinskaUtredningar) {
    if (andraMedicinskaUtredningar) {
        var promiseArr = [];
        for (var i = 0; i < andraMedicinskaUtredningar.length; i++) {
            var utredningEL = luaefsPage.andraMedicinskaUtredningar.getUtredning(i);
            var utredningDatum = testdataHelper.dateToText(andraMedicinskaUtredningar[i].datum);
            promiseArr.push(expect(utredningEL.typ.getText()).to.eventually.equal(andraMedicinskaUtredningar[i].underlag));
            promiseArr.push(expect(utredningEL.datum.getText()).to.eventually.equal(utredningDatum));
            promiseArr.push(expect(utredningEL.info.getText()).to.eventually.equal(andraMedicinskaUtredningar[i].infoOmUtredningen));
        }
        return Promise.all(promiseArr);
    } else if (!andraMedicinskaUtredningar) {
        return expect(luaefsPage.andraMedicinskaUtredningar.field.getText()).to.eventually.contain('Nej');
    }
}

function checkDiagnos(diagnos) {
    var diagnoser = diagnos.diagnoser;
    // var nyBedomning = testdataHelper.boolTillJaNej(diagnos.nyBedomning);
    var promiseArr = [];
    for (var i = 0; i < diagnoser.length; i++) {
        promiseArr.push(expect(luaefsPage.diagnoser.getDiagnos(i).kod.getText()).to.eventually.equal(diagnoser[i].kod));
    }

    return Promise.all(promiseArr);

}

function checkFunktionsnedsattning(nedsattning) {
    return Promise.all([
        expect(luaefsPage.funktionsnedsattning.debut.getText()).to.eventually.equal(nedsattning.debut),
        expect(luaefsPage.funktionsnedsattning.paverkan.getText()).to.eventually.equal(nedsattning.paverkan)
    ]);
}

function checkOvrigaUpplysningar(ovriga) {
    return expect(luaefsPage.ovrigaUpplysningar.getText()).to.eventually.equal(ovriga);
}

function checkKontaktMedFk(kontakt) {
    return expect(luaefsPage.kontaktFK.onskas.getText()).to.eventually.contain(testdataHelper.boolTillJaNej(kontakt));
}

module.exports = {
    checkValues: function(intyg) {
        logger.info('-- Kontrollerar Läkarutlåtande för sjukersättning --');
        logger.warn('intyg med typ: ' + intyg.typ + ' saknar vissa funktioner för kontroll av data');

        return Promise.all([
            //Baserat på
            checkBaseratPa(intyg.baseratPa)
            .then(function(value) {
                logger.info('OK - Baseras på');
            }, function(reason) {
                throw ('FEL, Baseras på: ' + reason);
            }),

            //Medicinska utredningar
            checkAndraMedicinskaUtredningar(intyg.andraMedicinskaUtredningar)
            .then(function(value) {
                logger.info('OK - Andra medicinska utredningar');
            }, function(reason) {
                throw ('FEL, Andra medicinska utredningar: ' + reason);
            }),

            //Diagnoser
            checkDiagnos(intyg.diagnos)
            .then(function(value) {
                logger.info('OK - Diagnos');
            }, function(reason) {
                throw ('FEL, Diagnos: ' + reason);
            }),

            //Funktionsnedsättning
            checkFunktionsnedsattning(intyg.funktionsnedsattning)
            .then(function(value) {
                logger.info('OK - Funktionsnedsättning');
            }, function(reason) {
                throw ('FEL, Funktionsnedsättning: ' + reason);
            }),

            //Övriga upplysningar
            checkOvrigaUpplysningar(intyg.ovrigt)
            .then(function(value) {
                logger.info('OK - Övriga upplysningar');
            }, function(reason) {
                throw ('FEL, Övriga upplysningar: ' + reason);
            }),

            //Kontakt med FK
            checkKontaktMedFk(intyg.kontaktMedFk)
            .then(function(value) {
                logger.info('OK - Kontakt med FK');
            }, function(reason) {
                throw ('FEL, Kontakt med FK: ' + reason);
            })

        ]);
    }
};
