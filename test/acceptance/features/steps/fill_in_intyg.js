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

/*global
testdata, intyg, browser, pages, logg, JSON*/
'use strict';

var tsdUtkastPage = pages.intyg.ts.diabetes.utkast;
var tsBasUtkastPage = pages.intyg.ts.bas.utkast;
var fkUtkastPage = pages.intyg.fk['7263'].utkast;

module.exports = function () {

  this.Given(/^jag fyller i alla nödvändiga fält för intyget$/, function (callback) {
    if (!global.intyg.typ) {
      callback('Intyg.typ odefinierad.');
    }

    if (intyg.typ === 'Transportstyrelsens läkarintyg') {
      global.intyg = testdata.getRandomTsBasIntyg(intyg.id);

      tsBasUtkastPage.fillInKorkortstyper(global.intyg.korkortstyper, 'intygetAvserForm');
      // browser.ignoreSynchronization = true;
      // Intyget avser
      // Identiteten är styrkt genom
      tsBasUtkastPage.fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom);
      // Synfunktioner
      browser.ignoreSynchronization = true;
      tsBasUtkastPage.fillInSynfunktioner(global.intyg);
      tsBasUtkastPage.fillInHorselOchBalanssinne(global.intyg);
      tsBasUtkastPage.fillInRorelseorganensFunktioner(global.intyg);
      tsBasUtkastPage.fillInHjartOchKarlsjukdomar(global.intyg);
      tsBasUtkastPage.fillInDiabetes(global.intyg);
      tsBasUtkastPage.fillInHorselOchBalanssinne(global.intyg);
      tsBasUtkastPage.fillInNeurologiskaSjukdomar(global.intyg);
      tsBasUtkastPage.fillInEpilepsi(global.intyg);
      tsBasUtkastPage.fillInNjursjukdomar(global.intyg);
      tsBasUtkastPage.fillInDemens(global.intyg);
      tsBasUtkastPage.fillInSomnOchVakenhet(global.intyg);
      tsBasUtkastPage.fillInAlkoholNarkotikaLakemedel(global.intyg);
      tsBasUtkastPage.fillInPsykiska(global.intyg);
      tsBasUtkastPage.fillInAdhd(global.intyg);
      tsBasUtkastPage.fillInSjukhusvard(global.intyg);

      tsBasUtkastPage.fillInOvrigMedicinering(global.intyg);

      browser.ignoreSynchronization = false;
      tsBasUtkastPage.fillInBedomning(intyg.bedomning);
      callback();
    } else if (intyg.typ === 'Transportstyrelsens läkarintyg, diabetes') {
      global.intyg = testdata.getRandomTsDiabetesIntyg(intyg.id);

      //Ange körkortstyper
      tsdUtkastPage.fillInKorkortstyper(intyg.korkortstyper).then(function () {
        logg('OK - fillInKorkortstyper :' + intyg.korkortstyper.toString());
      }, function (reason) {
        callback('FEL, fillInKorkortstyper,' + reason);
      });

      //Ange Identitet styrkt genom
      tsdUtkastPage.fillInIdentitetStyrktGenom(intyg.identitetStyrktGenom).then(function () {
        logg('OK - fillInIdentitetStyrktGenom :' + intyg.identitetStyrktGenom.toString());
      }, function (reason) {
        callback('FEL, fillInIdentitetStyrktGenom,' + reason);
      });

      browser.ignoreSynchronization = true;

      //Ange allmänt
      tsdUtkastPage.fillInAllmant(intyg.allmant).then(function () {
        logg('OK - fillInAllmant :' + JSON.stringify(intyg.allmant));
      }, function (reason) {
        callback('FEL, fillInAllmant,' + reason);
      });

      //Ange hypoglykemier
      tsdUtkastPage.fillInHypoglykemier(intyg.hypoglykemier).then(function () {
        logg('OK - fillInHypoglykemier :' + JSON.stringify(intyg.hypoglykemier));
      }, function (reason) {
        callback('FEL, fillInHypoglykemier,' + reason);
      });

      tsdUtkastPage.fillInSynintyg(intyg.synintyg).then(function () {
        logg('OK - fillInSynintyg :' + JSON.stringify(intyg.synintyg));
      }, function (reason) {
        callback('FEL, fillInSynintyg,' + reason);
      });

      browser.ignoreSynchronization = false;

      tsdUtkastPage.fillInBedomning(intyg.bedomning).then(function () {
        logg('OK - fillInBedomning :' + JSON.stringify(intyg.bedomning));
      }, function (reason) {
        callback('FEL, fillInBedomning,' + reason);
      }).then(callback);

    } else if (intyg.typ === 'Läkarintyg FK 7263') {
      global.intyg = testdata.fk.sjukintyg.getRandom(intyg.id);

      //Ange smittskydd
      fkUtkastPage.angeSmittskydd(intyg.smittskydd).then(function () {
        logg('OK - angeSmittskydd :' + intyg.smittskydd);
      }, function (reason) {
        callback('FEL, angeSmittskydd,' + reason);
      });

      browser.ignoreSynchronization = true;

      //Ange baseras på
      fkUtkastPage.angeIntygetBaserasPa(intyg.baserasPa).then(function () {
        logg('OK - angeIntygetBaserasPa :' + JSON.stringify(intyg.baserasPa));
      }, function (reason) {
        callback('FEL, angeIntygetBaserasPa,' + reason);
      });

      //Ange funktionsnedsättning
      fkUtkastPage.angeFunktionsnedsattning(intyg.funktionsnedsattning).then(function () {
        logg('OK - angeFunktionsnedsattning :' + JSON.stringify(intyg.funktionsnedsattning));
      }, function (reason) {
        callback('FEL, angeFunktionsnedsattning,' + reason);
      });

      //Ange diagnoser
      fkUtkastPage.angeDiagnoser(intyg.diagnos).then(function () {
        logg('OK - angeDiagnoser :' + JSON.stringify(intyg.diagnos));
      }, function (reason) {
        callback('FEL, angeDiagnoser,' + reason);
      });

      //Ange aktuellt sjukdomsförlopp
      fkUtkastPage.angeAktuelltSjukdomsForlopp(intyg.aktuelltSjukdomsforlopp).then(function () {
        logg('OK - angeAktuelltSjukdomsForlopp :' + JSON.stringify(intyg.aktuelltSjukdomsforlopp));
      }, function (reason) {
        callback('FEL, angeAktuelltSjukdomsForlopp,' + reason);
      });

      //Ange aktivitetsbegränsning
      fkUtkastPage.angeAktivitetsBegransning(intyg.aktivitetsBegransning).then(function () {
        logg('OK - angeAktivitetsBegransning :' + JSON.stringify(intyg.aktivitetsBegransning));
      }, function (reason) {
        callback('FEL, angeAktivitetsBegransning,' + reason);
      });

      fkUtkastPage.angeArbete(intyg.arbete).then(function () {
        logg('OK - angeArbete :' + JSON.stringify(intyg.arbete));
      }, function (reason) {
        callback('FEL, angeArbete,' + reason);
      });
      fkUtkastPage.angeArbetsformaga(intyg.arbetsformaga).then(function () {
        logg('OK - angeArbetsformaga :' + JSON.stringify(intyg.arbetsformaga));
      }, function (reason) {
        callback('FEL, angeArbetsformaga,' + reason);
      });
      fkUtkastPage.angeArbetsformagaFMB(intyg.arbetsformagaFMB).then(function () {
        logg('OK - angeArbetsformagaFMB :' + JSON.stringify(intyg.arbetsformagaFMB));
      }, function (reason) {
        callback('FEL, angeArbetsformagaFMB,' + reason);
      });

      browser.ignoreSynchronization = false;

      fkUtkastPage.angePrognos(intyg.prognos).then(function () {
        logg('OK - angePrognos :' + JSON.stringify(intyg.prognos));
      }, function (reason) {
        callback('FEL, angePrognos,' + reason);
      });
      fkUtkastPage.angeKontaktOnskasMedFK(intyg.kontaktOnskasMedFK).then(function () {
        logg('OK - angeKontaktOnskasMedFK :' + JSON.stringify(intyg.kontaktOnskasMedFK));
      }, function (reason) {
        callback('FEL, angeKontaktOnskasMedFK,' + reason);
      });
      fkUtkastPage.angeRekommendationer(intyg.rekommendationer).then(function () {
          logg('OK - angeRekommendationer :' + JSON.stringify(intyg.rekommendationer));
        }, function (reason) {
          callback('FEL, angeRekommendationer,' + reason);
        })
        .then(callback());

    }

  });

};
