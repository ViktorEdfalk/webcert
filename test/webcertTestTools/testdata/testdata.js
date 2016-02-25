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

'use strict';

var testdataHelper = require('./../helpers/testdataHelper.js');
var shuffle = testdataHelper.shuffle;

function findArrayElementsInArray(targetArray, compareArray) {
    // find all elements in targetArray matching any elements in compareArray
    var result = targetArray.filter(function(element) {
        return (compareArray.indexOf(element) >= 0);
    });

    return result;
}

function arrayContains(array, compareArray) {
    var found = findArrayElementsInArray(array, compareArray);
    return found.length > 0;
}

module.exports = {
    ICD10: ['A00', 'B00', 'C00', 'D00'],
    korkortstyper: ['AM', 'A1', 'A2', 'A', 'B', 'BE', 'Traktor', 'C1', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'Taxi'],
    identitetStyrktGenom: ['ID-kort', 'Företagskort eller tjänstekort', 'Svenskt körkort', 'Personlig kännedom', 'Försäkran enligt 18 kap. 4§', 'Pass'],
    diabetestyp: ['Typ 1', 'Typ 2'],
    diabetesbehandlingtyper: ['Endast kost', 'Tabletter', 'Insulin'],
    comment: 'Inget att rapportera',
    // FK7263

    // TS-Bas-attribut
    korkortstyperHogreBehorighet: ['C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'Taxi'],
    synDonder: ['Ja', 'Nej'],
    synNedsattBelysning: ['Ja', 'Nej'],
    synOgonsjukdom: ['Ja', 'Nej'],
    synDubbel: ['Ja', 'Nej'],
    synNystagmus: ['Ja', 'Nej'],
    synLinser: ['Ja', 'Nej'],
    horselYrsel: ['Ja', 'Nej'],
    horselSamtal: ['Ja', 'Nej'],
    rorOrgNedsattning: ['Ja', 'Nej'],
    rorOrgInUt: ['Ja', 'Nej'],
    hjartHjarna: ['Ja', 'Nej'],
    hjartSkada: ['Ja', 'Nej'],
    hjartRisk: ['Ja', 'Nej'],
    diabetes: ['Ja', 'Nej'],
    neurologiska: ['Ja', 'Nej'],
    epilepsi: ['Ja', 'Nej'],
    njursjukdom: ['Ja', 'Nej'],
    demens: ['Ja', 'Nej'],
    somnVakenhet: ['Ja', 'Nej'],
    alkoholMissbruk: ['Ja', 'Nej'],
    alkoholVard: ['Ja', 'Nej'],
    alkoholProvtagning: ['Ja', 'Nej'],
    alkoholLakemedel: ['Ja', 'Nej'],
    psykiskSjukdom: ['Ja', 'Nej'],
    adhdPsykisk: ['Ja', 'Nej'],
    adhdSyndrom: ['Ja', 'Nej'],
    sjukhusvard: ['Ja', 'Nej'],
    ovrigMedicin: ['Ja', 'Nej'],
    mediciner: ['Ipren', 'Alvedon', 'Bamyl', 'Snus'],
    funktionsnedsattningar: ['Problem...', 'Inget tal', 'Ingen koncentration', 'Total', 'Blind', 'Svajig i benen', 'Ingen'],

    fk: require('./fk.js'),

    getRandomKorkortstyper: function() {
        // Shuffla korkortstyper och returnera slumpad längd på array
        return shuffle(this.korkortstyper).slice(0, Math.floor(Math.random() * this.korkortstyper.length) + 1);
    },
    getRandomFloat: function() {
        return parseFloat(Math.round((Math.random() * (2.0 - 1.0) + 1.0) * 10) / 10).toFixed(1);
    },
    getRandomKorkortstyperHogre: function() {
        // Shuffla korkortstyper och returnera slumpad längd på array
        return shuffle(this.korkortstyperHogreBehorighet).slice(0, Math.floor(Math.random() * this.korkortstyperHogreBehorighet.length) + 1);
    },
    getRandomStyrka: function() {
        var styrkor = {
            houk: this.getRandomFloat(),
            homk: this.getRandomFloat(),
            vouk: this.getRandomFloat(),
            vomk: this.getRandomFloat(),
            buk: this.getRandomFloat(),
            bmk: this.getRandomFloat()
        };
        return styrkor;
    },

    getRandomIdentitetStyrktGenom: function() {
        return shuffle(this.identitetStyrktGenom)[0];
    },
    getRandomHypoglykemier: function(korkortstyper) {
        var hypoObj = {
            a: 'Nej',
            b: 'Nej'
        };

        //För vissa körkortstyper krävs det svar på f och g
        if (
            korkortstyper.indexOf('C1') > -1 ||
            korkortstyper.indexOf('C1E') > -1 ||
            korkortstyper.indexOf('C') > -1 ||
            korkortstyper.indexOf('CE') > -1 ||
            korkortstyper.indexOf('D1') > -1 ||
            korkortstyper.indexOf('D1E') > -1 ||
            korkortstyper.indexOf('D') > -1 ||
            korkortstyper.indexOf('DE') > -1
        ) {
            hypoObj.f = shuffle(['Ja', 'Nej'])[0];
            hypoObj.g = 'Nej';
        }
        return hypoObj;
    },
    getRandomBehandling: function() {
        var behandlingObj = {
            typer: shuffle(this.diabetesbehandlingtyper).slice(0, Math.floor(Math.random() * this.diabetesbehandlingtyper.length) + 1)
        };

        // Om Insulinbehanling så måste startår anges
        if (behandlingObj.typer.indexOf('Insulin') > -1) {
            behandlingObj.insulinYear = Math.floor((Math.random() * 20) + 1980);
        }

        return behandlingObj;
    },
    hasHogreKorkortsbehorigheter: function(korkortstyper) {
        var foundHogreBehorigheter = findArrayElementsInArray(korkortstyper, this.korkortstyperHogreBehorighet);
        return foundHogreBehorigheter.length > 0;
    },
    getRandomBedomning: function(korkortstyper) {
        //TODO: bör shuffla ja och nej,
        var lamplighet = shuffle(['Ja', 'Nej'])[0];

        if (!this.hasHogreKorkortsbehorigheter(korkortstyper)) {
            lamplighet = null;
        }

        var bedomningsObj = {
            stallningstagande: 'behorighet_bedomning',
            behorigheter: korkortstyper,
            lamplighet: lamplighet
        };

        //För vissa körkortstyper krävs det svar lämplighet
        if (
            korkortstyper.indexOf('C1') > -1 ||
            korkortstyper.indexOf('C1E') > -1 ||
            korkortstyper.indexOf('C') > -1 ||
            korkortstyper.indexOf('CE') > -1 ||
            korkortstyper.indexOf('D1') > -1 ||
            korkortstyper.indexOf('D1E') > -1 ||
            korkortstyper.indexOf('D') > -1 ||
            korkortstyper.indexOf('DE') > -1
        ) {
            bedomningsObj.lamplighet = shuffle(['Ja', 'Nej'])[0];
        }
        return bedomningsObj;

    },
    getRandomTsDiabetesIntyg: function(intygsID) {
        var randomKorkortstyper = this.getRandomKorkortstyper();

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        return {
            id: intygsID,
            typ: 'Transportstyrelsens läkarintyg, diabetes',
            korkortstyper: randomKorkortstyper,
            identitetStyrktGenom: this.getRandomIdentitetStyrktGenom(),
            allmant: {
                year: Math.floor((Math.random() * 20) + 1980),
                typ: shuffle(this.diabetestyp)[0],
                behandling: this.getRandomBehandling()
            },

            // TODO: Gör dessa slumpade likt ovanstående
            hypoglykemier: this.getRandomHypoglykemier(randomKorkortstyper),
            synintyg: {
                a: 'Ja'
            },
            bedomning: this.getRandomBedomning(randomKorkortstyper)
        };
    },
    getRandomHorselSamtal: function(korkortstyper) {
        var besvarasOm = ['D1', 'D1E', 'D', 'DE', 'Taxi'];
        if (arrayContains(korkortstyper, besvarasOm)) {
            return shuffle(this.horselSamtal)[0];
        } else {
            return false;
        }
    },
    getRandomInUtUrFordon: function(korkortstyper) {
        var besvarasOm = ['D1', 'D1E', 'D', 'DE', 'Taxi'];
        if (arrayContains(korkortstyper, besvarasOm)) {
            return shuffle(this.rorOrgInUt)[0];
        } else {
            return '';
        }
    },
    getRandomTsBasIntyg: function(intygsID) {
        var randomKorkortstyper = this.getRandomKorkortstyperHogre();

        if (!intygsID) {
            intygsID = testdataHelper.generateTestGuid();
        }

        return {
            id: intygsID,
            typ: 'Transportstyrelsens läkarintyg',
            korkortstyper: randomKorkortstyper,
            identitetStyrktGenom: this.getRandomIdentitetStyrktGenom(),
            allmant: {
                year: Math.floor((Math.random() * 20) + 1980),
                behandling: this.getRandomBehandling()
            },
            synintyg: {
                a: 'Ja'
            },
            bedomning: this.getRandomBedomning(randomKorkortstyper),
            synDonder: shuffle(this.synDonder)[0],
            synNedsattBelysning: shuffle(this.synNedsattBelysning)[0],
            synOgonsjukdom: shuffle(this.synOgonsjukdom)[0],
            synDubbel: shuffle(this.synDubbel)[0],
            synNystagmus: shuffle(this.synNystagmus)[0],
            horsel: {
                yrsel: shuffle(this.horselYrsel)[0],
                samtal: this.getRandomHorselSamtal(randomKorkortstyper)
            },
            linser: {
                vanster: shuffle(this.synLinser)[0],
                hoger: shuffle(this.synLinser)[0]
            },
            rorelseorganensFunktioner: {
                nedsattning: shuffle(this.rorOrgNedsattning)[0],
                inUtUrFordon: this.getRandomInUtUrFordon(randomKorkortstyper)
            },
            hjartHjarna: shuffle(this.hjartHjarna)[0],
            hjartSkada: shuffle(this.hjartSkada)[0],
            hjartRisk: shuffle(this.hjartRisk)[0],
            diabetes: {
                hasDiabetes: shuffle(this.diabetes)[0],
                typ: shuffle(this.diabetestyp)[0],
                behandlingsTyper: this.diabetesbehandlingtyper
            },
            neurologiska: shuffle(this.neurologiska)[0],
            epilepsi: shuffle(this.epilepsi)[0],
            njursjukdom: shuffle(this.njursjukdom)[0],
            demens: shuffle(this.demens)[0],
            somnVakenhet: shuffle(this.somnVakenhet)[0],
            alkoholMissbruk: shuffle(this.alkoholMissbruk)[0],
            alkoholVard: shuffle(this.alkoholVard)[0],
            alkoholProvtagning: shuffle(this.alkoholProvtagning)[0],
            alkoholLakemedel: shuffle(this.alkoholLakemedel)[0],
            psykiskSjukdom: shuffle(this.psykiskSjukdom)[0],
            adhdPsykisk: shuffle(this.adhdPsykisk)[0],
            adhdSyndrom: shuffle(this.adhdSyndrom)[0],
            sjukhusvard: shuffle(this.sjukhusvard)[0],
            ovrigMedicin: shuffle(this.ovrigMedicin)[0],
            kommentar: this.comment,
            styrkor: this.getRandomStyrka()
        };
    },

    randomTrueFalse: function() {
        return shuffle([true, false])[0];
    },

    getRandomLuseIntyg: function(intygsID) {
        return {
            intygId: intygsID,
            typ: 'Läkarutlåtande för sjukersättning',
            diagnos: {
                kod: shuffle(this.ICD10)[0],
                bakgrund: 'En slumpmässig bakgrund'
            },
            annanUnderlag: this.randomTrueFalse(),
            sjukdomsForlopp: 'Sjukdomsförlopp kommentar',
            nyDiagnosBedom: this.randomTrueFalse(),
            funktionsnedsattning: {
                //funktionsnedsattningar
                intellektuell: shuffle(this.funktionsnedsattningar)[0],
                kommunikation: shuffle(this.funktionsnedsattningar)[0],
                koncentration: shuffle(this.funktionsnedsattningar)[0],
                psykisk: shuffle(this.funktionsnedsattningar)[0],
                synHorselTal: shuffle(this.funktionsnedsattningar)[0],
                balansKoordination: shuffle(this.funktionsnedsattningar)[0],
                annan: shuffle(this.funktionsnedsattningar)[0]
            },
            aktivitetsbegransning: 'Total',
            avslutadBehandling: shuffle(this.mediciner)[0],
            pagaendeBehandling: shuffle(this.mediciner)[0],
            planeradBehandling: shuffle(this.mediciner)[0],
            substansintag: shuffle(this.mediciner)[0],
            medicinskaForutsattningarForArbete: 'Inte speciellt',
            aktivitetsFormaga: 'Liten',
            ovrigt: 'Inget',
            kontaktMedFkNo: this.randomTrueFalse(),
            tillaggsfragor0svar: 'Answer',
            tillaggsfragor1svar: 'Question'
        };
    }
};