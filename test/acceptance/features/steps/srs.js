/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

/* globals pages, browser, protractor, logger, Promise, intyg */
'use strict';
let helpers = require('./helpers');
let Soap = require('soap');
let soapMessageBodies = require('./soap');
let fk7263utkast = pages.intyg.fk['7263'].utkast;
let srsdata = require('./srsdata.js');

module.exports = function() {
    let user = {};

    this.Given(/^spara användare till globaluser$/, () => {
        global.user = user;
        logger.info(`Användare ${global.user.forNamn} ${global.user.efterNamn} på enhet ${global.user.hsaId} sparad sparad till intygsobjekt`);
        return browser.sleep(500);
    });

    this.Given(/^att jag är djupintegrerat inloggad som läkare på vårdenhet "(med SRS|utan SRS)"$/,
        srsStatus => {
            user = srsdata.inloggningar[srsStatus];
            logger.info(`Loggar in som ${user.forNamn} ${user.efterNamn} på enhet ${user.enhetId}`);
            return pages.welcome.get()
                .then(() => pages.welcome.loginByJSON(JSON.stringify(user), true));
        }

    );

    this.Given(/^en patient som "(inte har givit samtycke|har givit samtycke)" till SRS$/,
        samtycke => setConsent(srsdata.patient, user, samtycke)
    );

    this.Given(/^att jag befinner mig på ett nyskapat Läkarintyg FK 7263$/, () =>
        createDraftUsingSOAP(user, srsdata.patient.id)
        .then(intygsId => {
            intyg.id = intygsId;
            browser.get(buildLinkToIntyg(intygsId, srsdata.patient, user.enhetId));
        })
        .then(() => browser.waitForAngular())
        .then(() => browser.sleep(2000)) // Behövs för att waitForAngular tydligen inte räcker
        .then(() => expect(element(by.id('wcHeader')).isPresent()).to.eventually.equal(true))
    );

    this.Then(/^ska en frågepanel för SRS "(inte)? ?visas"$/,
        panelStatus => expect(fk7263utkast.srs.panel().isDisplayed()).to.eventually.equal(panelStatus !== 'inte')
    );

    this.Then(/^ska en pil med texten "(Visa mindre|Visa mer)" visas$/,
        text => expect(findLabelContainingText(text).isPresent()).to.eventually.equal(true)
    );

    this.When(/^jag (?:fyller|fyllt) i diagnoskod som "(.*)"$/,
        srsStatus => fk7263utkast.angeDiagnosKod(srsdata.diagnoskoder[srsStatus])
        .then(() => {
            logger.info('Använder diagnoskod: ' + srsdata.diagnoskoder[srsStatus]);
            return browser.sleep(1000); // Angular behöver extra tid på sig här för att spara diagnoskoden
        })
    );

    this.Then(/^ska knappen för SRS vara i läge "(stängd|öppen|gömd)"$/,
        srsButtonStatus => expect(fk7263utkast.getSRSButtonStatus()).to.eventually.equal(srsButtonStatus)
    );

    this.When(/^jag klickar på knappen för SRS$/, () => fk7263utkast.srs.knapp().click());

    this.When(/^jag klickar på pilen( för att minimera)?$/, action => {
        fk7263utkast.srs.visamer().getAttribute('class')
            .then((collapsed) => collapsed.includes('collapsed') ? true : false)
            .then((isCollapsed) => isCollapsed ? fk7263utkast.srs.visamer().click() :
                (action !== undefined && action.trim() === 'för att minimera') ? fk7263utkast.srs.visamer().click() : undefined)
            .then(() => browser.sleep(500));
    });

    this.Then(/^ska frågepanelen för SRS vara "(minimerad|maximerad)"$/,
        status => expect(fk7263utkast.getSRSQuestionnaireStatus()).to.eventually.equal(status)
    );

    this.Then(/^ska jag få prediktion "([^"]*)"$/, predictionMsg =>
        expect(fk7263utkast.srs.prediktion().getText()).to.eventually.contain(predictionMsg)
    );

    this.Then(/^ska en fråga om samtycke visas$/, () => expect(
        findLabelContainingText('Patienten samtycker till att delta').isPresent()
    ).to.eventually.equal(true));

    this.When(/^jag anger att patienten (inte)? ?samtycker till SRS$/,
        samtycke => fk7263utkast.setSRSConsent(samtycke === 'inte' ? false : true)
    );

    this.Then(/^frågan om samtycke ska (?:inte )?vara förifylld med "(Ja|Nej)"$/, samtycke => (('nej' === samtycke.toLowerCase()) ?
            expect(fk7263utkast.srs.samtycke[samtycke.toLowerCase()]().isSelected()).to.eventually.equal(false) :
            expect(fk7263utkast.srs.samtycke[samtycke.toLowerCase()]().isSelected()).to.eventually.equal(true))
        .then(() => browser.sleep(500)));


    this.Then(/^ska åtgärdsförslag från SRS-tjänsten visas$/, () => expect(fk7263utkast.srs.atgarder().isDisplayed()).to.eventually.equal(true));

    this.When(/^jag fyller i ytterligare svar för SRS$/, () => clickAnswerRadioButtons());

    this.When(/^ska en ny sida öppnas och urlen innehålla "([^"]*)"$/, (type) => {
        browser.ignoreSynchronization = true;
        //Ignorera angular sync om det inte är en angular app. Och vänta på att sidan laddar.
        return helpers.hugeDelay().then(function() {
            return getWindowHandles();
        }).then(function(handles) {
            console.log(handles);
            if (handles.length < 2) {
                throw ('bara 1 flik är öppen');
            } else {
                return switchToWindow(handles[1], true).then(function() {
                    return testTypeOfUrl(handles[0], type).then((urlPart) => {
                        return expect(type).to.contain(urlPart);

                    });

                });
            }
        });
    });

    this.When(/^ska en ny sida öppnas och urlen innehålla diagnoskod som "([^"]*)"$/, (type) => {
        if (srsdata.diagnoskoder[type] !== undefined) {
            type = srsdata.diagnoskoder[type].toLowerCase();
        }

        browser.ignoreSynchronization = true;
        //Ignorera angular sync om det inte är en angular app. Och vänta på att sidan laddar.

        return helpers.hugeDelay().then(function() {
            return getWindowHandles();
        }).then(function(handles) {
            if (handles.length < 2) {
                throw ('bara 1 flik är öppen');
            } else {
                return switchToWindow(handles[1], true).then(function() {
                    return testTypeOfUrl(handles[0], type).then((urlPart) => {
                        return expect(type).to.contain(urlPart);

                    });
                });
            }
        });
    });

    this.When(/^ska en ny sida öppnas och urlen innehålla diagnoskod som "([^"]*)" med postfix "([^"]*)"$/, (type, postfix) => {
        if (srsdata.diagnoskoder[type] !== undefined) {
            type = srsdata.diagnoskoder[type].toLowerCase();
        }

        browser.ignoreSynchronization = true;
        //Ignorera angular sync om det inte är en angular app. Och vänta på att sidan laddar.
        return helpers.hugeDelay().then(function() {
            return getWindowHandles();
        }).then(function(handles) {
            if (handles.length < 2) {
                throw ('bara 1 flik är öppen');
            } else {
                return switchToWindow(handles[1], true).then(function() {
                    return testTypeOfUrl(handles[0], type, postfix).then((urlPart) => {
                        return expect(type).to.contain(urlPart);
                    });
                });
            }
        });
    });

    this.When(/^jag klickar på knappen "([^"]*)" vid (samtycke)$/, (knappText, type) => {
        if (isQuestionmarkBtn(knappText)) {
            clickQuestionmarkBtn(type).then(() => browser.sleep(500));
        } else {
            clickReadMoreBtn(type).then(() => browser.sleep(500));
        }
    });

    this.When(/^jag klickar på knappen "([^"]*)" vid (prediktionsmeddelandet)$/, (knappText, type) => {
        if (isQuestionmarkBtn(knappText)) {
            clickQuestionmarkBtn(type).then(() => browser.sleep(500));
        } else {
            clickReadMoreBtn(type).then(() => browser.sleep(500));
        }
    });

    this.When(/^jag klickar på knappen "([^"]*)" vid (åtgärder)$/, (knappText, type) => {
        clickReadMoreBtn(type).then(() => browser.sleep(500));
    });

    this.When(/^jag klickar på knappen "([^"]*)" vid (statistik)$/, (knappText, type) => {
        clickReadMoreBtn(type).then(() => browser.sleep(500));
    });

    this.When(/^jag trycker på knappen "Visa"$/, () => helpers.moveAndSendKeys(fk7263utkast.srs.visaKnapp(), protractor.Key.SPACE));

    this.Then(/^ska prediktion från SRS-tjänsten visas$/, () => expect(fk7263utkast.srs.prediktion().isDisplayed()).to.eventually.equal(true));

    this.When(/^jag trycker på fliken "(Statistik|Åtgärder)"$/,
        flikText => fk7263utkast.srs.flik(flikText).sendKeys(protractor.Key.ENTER)
    );

    this.Then(/^ska en statistikbild från SRS-tjänsten visas för en diagnoskod som "([^"]*)"$/, (srsStatus) => fk7263utkast.srs.statistik().isDisplayed()
        .then((isDisplayed) => isDisplayed ? fk7263utkast.srs.statistik().element(by.tagName('img')).getAttribute('src') : 'unknown url')
        .then((srcUrl) => expect(srcUrl.indexOf(srsdata.diagnoskoder[srsStatus]) > -1).to.equal(true))
    );

    this.Then(/^ska felmeddelandet "(.*)" visas$/,
        text => expect(findLabelContainingText(text).isDisplayed()).to.eventually.equal(true)
    );

    this.Then(/^ska OBS-åtgärder från "(.*)" visas$/,
        listNamn => {
            logger.debug('Förväntade OBS-åtgärder: ' + srsdata.atgarder[listNamn]);
            return expect(getAtgarderOBS()).to.eventually.have.same.members(srsdata.atgarder[listNamn]);
        }
    );

    this.Then(/^ska REK-åtgärder från "(.*)" visas$/,
        listNamn => {
            logger.debug('Förväntade REK-åtgärder: ' + srsdata.atgarder[listNamn]);
            return expect(getAtgarderREK()).to.eventually.have.same.members(srsdata.atgarder[listNamn]);
        }
    );

};

function clickAnswerRadioButtons() {
    return fk7263utkast.srs.fragor()
        .all(by.css('input[type=radio]'))
        .each(el => el.click()).catch(() => logger.debug('Ignoring unclickable radio button.')); // Av någon anledning kastas ett fel trots att alla element går att klicka på
}

function setConsent(patient, user, consent) {
    /**
     * Injicerar ett skript i browsern som skickar "SetConsent" till webcert backend.
     * Används för att försätta en patient i känt state inför test.
     **/
    const patientId = patient.id.slice(0, 8) + '-' + patient.id.slice(8 + 0);
    const link = buildLinkToSetConsent(patientId, user.enhetId);
    const payload = consent === 'har givit samtycke' ? 'true' : 'false';
    logger.debug(`URL: ${link} PAYLOAD: ${payload}`);
    return browser.executeAsyncScript(function(url, samtycke, body) {
            var callback = arguments[arguments.length - 1];
            var xhr = new XMLHttpRequest();
            xhr.open('PUT', url, true);
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4) {
                    callback(xhr.responseText);
                }
            };
            xhr.send(body);
        }, link, consent, payload)
        .then(response => expect(response).to.equal('"OK"'));
}

function buildLinkToSetConsent(patientId, enhetId) {
    let uri = uriTemplate `api/srs/consent/${patientId}/${enhetId}`;
    return process.env.WEBCERT_URL + uri;
}

function getAtgarderREK() {
    return fk7263utkast.srs.atgarderRek().getText().then(t => {
        const atgarder = t.replace('Läs mer', '') // Ta bort "Läs mer" som finns på slutet
            .replace(/\n/g, '') // Ta bort alla radbrytningar
            .split('• ')
            .slice(1); // Första elementet blir alltid tomt
        logger.info('Hittade REK-åtgärder: ' + atgarder);
        return Promise.resolve(atgarder);
    });
}

function getAtgarderOBS() {
    return fk7263utkast.srs.atgarderObs().getText().then(t => {
        const atgarder = t.replace(/\\n/g, '') // Ta bort alla radbrytningar
            .replace('Tänk på att; ', '')
            .split('. ');
        logger.info('Hittade OBS-åtgärder: ' + atgarder);
        return Promise.resolve(atgarder);
    });
}

function findLabelContainingText(text) {
    return fk7263utkast.srs.panel()
        .all(by.tagName('div'))
        .filter(ele => ele.getText().then(t => t.includes(text))).first();
}

function createDraftUsingSOAP(user, patientId) {
    let path = '/services/create-draft-certificate/v1.0/?wsdl';
    let body = soapMessageBodies.CreateDraftCertificate(
        user.hsaId,
        `${user.forNamn} ${user.efterNamn}`,
        user.enhetId,
        'Enhetsnamn',
        patientId
    );

    let url = helpers.stripTrailingSlash(process.env.WEBCERT_URL) + path;
    logger.debug('debug', url);
    logger.debug('debug', body);

    return new Promise((resolve, reject) =>
        Soap.createClient(url, (err, client) => {
            client.CreateDraftCertificate(body, (err, response, responseBody) => {
                if (isNotOk(response)) {
                    reject(`Felaktigt SOAP-svar: ${responseBody}`);
                } else if (err) {
                    reject(err);
                } else {
                    resolve(response['utlatande-id'].attributes.extension);
                }
            });
        })
    ).catch(err => logger.error(err));
}

function isNotOk(response) {
    return !response || !response.result || response.result.resultCode !== 'OK' || !response['utlatande-id'] || !response['utlatande-id'].attributes;
}

function buildLinkToIntyg(intygsId, patient, enhetsId) {
    let uri = uriTemplate `visa/intyg/${intygsId}?fornamn=${patient.fornamn}&efternamn=${patient.efternamn}&postadress=${patient.adress.postadress}&postnummer=${patient.adress.postnummer}&postort=${patient.adress.postort}&enhet=${enhetsId}`;
    logger.info('IntygsURL: ' + process.env.WEBCERT_URL + uri);
    return process.env.WEBCERT_URL + uri;
}

function uriTemplate(strings, ...keys) {
    // Applicerar encodeURIComponent på varje variabel i templatet
    return strings.map((s, i) => [s, encodeURIComponent(keys[i])])
        .slice(0, -1)
        .reduce((sum, str) => sum += str[0] + str[1], '');
}

function getWindowHandles() {
    return browser.getAllWindowHandles();
}

function switchToWindow(handle, returnable) {
    if (returnable) {
        return browser.switchTo().window(handle);
    } else {
        browser.switchTo().window(handle);
    }
}

function testTypeOfUrl(handle, typeOfUrl, postfix) {
    return browser.getCurrentUrl().then(function(url) {
        return filterUrl(decodeURIComponent(url), typeOfUrl, postfix, handle);
    });
}

function filterUrl(url, typeOfUrl, postfix, handle) {
    logger.info(url);
    switchToWindow(handle, false);
    return url.split('/')
        .filter(urlPart => (urlPart === (typeOfUrl + (postfix !== undefined) ? postfix : '')))
        .toString();
}

function clickQuestionmarkBtn(type) {
    let index = ('samtycke' === type) ? 0 : 1;
    return helpers.moveAndSendKeys(element.all(by.css('.glyphicon-question-sign')).get(index), protractor.Key.SPACE);
}

function clickReadMoreBtn(type) {
    switch (type) {
        case srsdata.position.SAMTYCKE:
            return element(by.css('[ng-click="readMoreConsent()"]')).click();
        case srsdata.position.PREDIKTIONSMEDDELANDET:
            return element(by.css('[ng-click="readMoreRisk()"]')).click(); //TODO
        case srsdata.position.ATGARDER:
            return browser.actions().mouseMove(element(by.id('atgarderRek'))).perform()
                .then(() => helpers.mediumDelay())
                .then(() => {
                    return helpers.moveAndSendKeys(element(by.id('atgarderRek')).element(by.buttonText('Läs mer')), protractor.Key.SPACE);
                });
        case srsdata.position.STATISTIK:
            return browser.actions().mouseMove(element(by.id('statstics'))).perform()
                .then(() => helpers.mediumDelay())
                .then(() => {
                    return helpers.moveAndSendKeys(element(by.id('statstics')).element(by.buttonText('Läs mer')), protractor.Key.SPACE);
                });
        default:
            return Promise.reject();

    }

}

function isQuestionmarkBtn(knappText) {
    return ('?' === knappText) ? true : false;
}
