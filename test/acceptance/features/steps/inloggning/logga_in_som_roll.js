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

/* globals browser */

'use strict';
var loginHelper = require('./login.helpers.js');
var logInAsUserRole = loginHelper.logInAsUserRole;
var logInAsUser = loginHelper.logInAsUser;

module.exports = function() {

    this.Given(/^att jag är inloggad som tandläkare$/, function() {
        var userObj = {
            fornamn: 'Louise',
            efternamn: 'Ericsson',
            hsaId: 'TSTNMT2321000156-103B',
            enhetId: 'TSTNMT2321000156-1039'
        };
        return logInAsUserRole(userObj, 'Tandläkare');
    });
    this.Given(/^att jag är inloggad som läkare utan adress till enheten$/, function() {
        var userObj = {
            fornamn: 'Per',
            efternamn: 'Nilsson',
            hsaId: 'TST2321000156-102C',
            enhetId: 'SE2321000156-1004',
            lakare: 'true',
            origin: 'NORMAL'
        };
        return logInAsUserRole(userObj, 'Läkare');
    });


    this.Given(/^att jag är inloggad som vårdadministratör$/, function() {
        var userObj = {
            fornamn: 'Susanne',
            efternamn: 'Johansson Karlsson',
            hsaId: 'TSTNMT2321000156-105J',
            enhetId: 'TSTNMT2321000156-105F'
        };
        return logInAsUserRole(userObj, 'Vårdadministratör');
    });

    this.Given(/^att jag är inloggad som uthoppad vårdadministratör$/, function() {
        var userObj = {
            fornamn: 'Susanne',
            efternamn: 'Johansson Karlsson',
            hsaId: 'TSTNMT2321000156-105J',
            enhetId: 'TSTNMT2321000156-105F',
            origin: 'UTHOPP'
        };
        return logInAsUserRole(userObj, 'Vårdadministratör');
    });
    this.Given(/^att jag är inloggad som läkare( som inte accepterat kakor)?$/, function(inteAccepteratKakor) {
        var userObj = {
            fornamn: 'Erik',
            efternamn: 'Nilsson',
            hsaId: 'TSTNMT2321000156-105H',
            enhetId: 'TSTNMT2321000156-105F'
        };
        return logInAsUserRole(userObj, 'Läkare', inteAccepteratKakor);
    });

    this.Given(/^att jag är inloggad som djupintegrerad läkare$/, function() {

        var userObj = {
            fornamn: 'Erik',
            efternamn: 'Nilsson',
            hsaId: 'TSTNMT2321000156-105H',
            enhetId: 'TSTNMT2321000156-102R',
            origin: 'DJUPINTEGRATION'
        };
        return logInAsUserRole(userObj, 'Läkare');
    });

    this.Given(/^att jag är inloggad som djupintegrerad läkare på vårdenhet "([^"]*)"$/, function(enhetHsa) {
        var userObj = {
            fornamn: 'Åsa',
            efternamn: 'Svensson',
            hsaId: 'TSTNMT2321000156-100L',
            enhetId: enhetHsa,
            forskrivarKod: '2481632',
            origin: 'DJUPINTEGRATION'
        };
        return logInAsUserRole(userObj, 'Läkare');
    });

    this.Given(/^att jag är inloggad som uthoppsläkare$/, function() {
        var userObj = {
            fornamn: 'Erik',
            efternamn: 'Nilsson',
            hsaId: 'TSTNMT2321000156-105H',
            enhetId: 'TSTNMT2321000156-108F',
            origin: 'UTHOPP'
        };
        return logInAsUserRole(userObj, 'Läkare');
    });

    this.Given(/^ska jag ha rollen "([^"]*)"$/, function(roll, callback) {
        checkUserRole().then(function(value) {
            var re = /\[\"(.*)\"\]/;
            value = value.replace(re, '$1');
            expect(value).to.equal(roll);
            callback();
        });
    });

    this.Given(/^jag ska ha origin "([^"]*)"/, function(origin) {
        return expect(checkUserOrigin()).to.eventually.be.equal(origin);
    });
    this.Given(/^jag loggar in med felaktig uppgift om telefonuppgift i HSAkatalogen$/, function() {
        var userObj = {
            fornamn: 'Johan',
            efternamn: 'Johansson',
            hsaId: 'TSTNMT2321000156-107V',
            enhetId: 'TSTNMT2321000156-107Q'
        };
        return logInAsUser(userObj);
    });
    this.Given(/^jag loggar in med felaktig uppgift om befattning i HSAkatalogen$/, function() {
        var userObj = {
            fornamn: 'Susanne Gustafsson',
            efternamn: 'Ericsson',
            hsaId: 'TSTNMT2321000156-107W',
            enhetId: 'TSTNMT2321000156-107P'
        };
        return logInAsUser(userObj);
    });

    this.Given(/^jag loggar in med felaktig uppgift om adress i HSAkatalogen$/, function() {
        var userObj = {
            fornamn: 'Karin',
            efternamn: 'Persson',
            hsaId: 'TSTNMT2321000156-107T',
            enhetId: 'TSTNMT2321000156-107P'
        };
        return logInAsUser(userObj);
    });
    this.Given(/^ska jag vara inloggad som 'Läkare'$/, function() {
        var wcHeader = element(by.id('wcHeader'));
        return expect(wcHeader.getText()).to.eventually.contain('Läkare');
    });
    this.Given(/^jag loggar in som läkare med medarbetaruppdrag som administatör$/, function() {
        var userObj = {
            fornamn: 'Jenny',
            efternamn: 'Larsson',
            hsaId: 'TSTNMT2321000156-1084',
            enhetId: 'TSTNMT2321000156-107P',
            lakare: true,
            origin: 'DJUPINTEGRATION'
        };
        console.log('Loggar in som admin....');
        return logInAsUserRole(userObj, 'Läkare');

    });



};

function checkUserRole() {
    return performUserCheck('role');
}

function checkUserOrigin() {
    return performUserCheck('origin');
}

function performUserCheck(userconfig) {
    browser.ignoreSynchronization = true;
    browser.get('testability/user/' + userconfig + '/');
    var attribute = element(by.css('pre')).getText();
    browser.navigate().back();
    browser.sleep(1000);
    browser.ignoreSynchronization = false;
    return attribute;
}
