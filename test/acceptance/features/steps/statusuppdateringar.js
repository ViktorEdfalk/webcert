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

/* global pages, protractor, logger, ursprungligtIntyg */

'use strict';
var fk7263Utkast = pages.intyg.fk['7263'].utkast;
var db = require('./dbActions');
var tsBasintygtPage = pages.intyg.ts.bas.intyg;

var handelseRegex = /(HAN\d{1,2})/g;

function assertEvents(intygsId, handelsekod, numEvents, cb) {

    // Select table and extension based on if 'händelse' contains the pattern HAN{1-2digit} or NOT
    var table = selectTable(handelsekod);
    var extensionType = selectExtension(handelsekod);

    setTimeout(function() {
        var databaseTable = table;
        var query = 'SELECT COUNT(*) AS Counter FROM ' + databaseTable + ' WHERE ' +
            databaseTable + '.handelseKod = "' + handelsekod + '" AND ' +
            databaseTable + '.' + extensionType + ' = "' + intygsId + '" ;';

        assertNumberOfEvents(query, numEvents, cb);
    }, 5000);
}

function selectTable(handelsekod) {
    var res = handelsekod.match(handelseRegex);
    if (res) {
        if (res.length > 1) {
            logger.error('ERROR: More than one "händelse" found.');
            return;
        }
        console.log('Database: webcert_requests.requests');
        return 'webcert_requests.requests';
    } else {
        console.log('Database: webcert_requests.statusupdates_2');
        return 'webcert_requests.statusupdates_2';
    }
}

function selectExtension(handelsekod) {
    var res = handelsekod.match(handelseRegex);
    if (res) {
        if (res.length > 1) {
            logger.error('ERROR: More than one "händelse" found. Cannot select determine extension');
            return;
        }
        console.log('Extension: utlatandeExtension');
        return 'utlatandeExtension';
    } else {
        console.log('Extension: intygsExtension');
        return 'intygsExtension';
    }
}

function assertNumberOfEvents(query, numEvents, cb) {
    // logger.debug('Assert number of events. Query: ' + query);
    var conn = db.makeConnection();
    conn.connect();
    conn.query(query,
        function(err, rows, fields) {
            conn.end();
            if (err) {
                cb(err);
            } else if (rows[0].Counter !== numEvents) {
                cb('FEL, Antal händelser i db: ' + rows[0].Counter + ' (' + numEvents + ')');
            } else {
                logger.info('OK - Antal händelser i db ' + rows[0].Counter + '(' + numEvents + ')');
                cb();
            }
        });
}

module.exports = function() {

    this.Then(/^ska intygsutkastets status vara "([^"]*)"$/, function(statustext, callback) {
        expect(tsBasintygtPage.intygStatus.getText()).to.eventually.contain(statustext).and.notify(callback);
    });

    this.Given(/^ska statusuppdatering "([^"]*)" skickas till vårdsystemet\. Totalt: "([^"]*)"$/, function(handelsekod, antal, callback) {
        assertEvents(global.intyg.id, handelsekod, parseInt(antal, 10), callback);
    });

    this.Given(/^ska (\d+) statusuppdatering "([^"]*)" skickas för det ursprungliga intyget$/, function(antal, handelsekod, callback) {
        assertEvents(ursprungligtIntyg.id, handelsekod, parseInt(antal, 10), callback);
    });

    // this.Given(/^är intygets status "([^"]*)"$/, function(arg1, callback) {
    //     assertDraftWithStatus(global.person.id, global.intyg.id, arg1, callback);
    // });

    // this.Given(/^är innehåller databasfältet "([^"]*)" värdet "([^"]*)"$/, function(arg1, arg2, callback) {
    //     assertDatabaseContents(global.intyg.id, arg1, arg2, callback);
    // });

    this.Given(/^jag raderar intyget$/, function(callback) {
        fk7263Utkast.radera.knapp.sendKeys(protractor.Key.SPACE).then(function() {
            fk7263Utkast.radera.bekrafta.sendKeys(protractor.Key.SPACE).then(callback);
        });
    });
};
