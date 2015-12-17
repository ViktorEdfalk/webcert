/**
 * Created by bennysce on 09/06/15.
 */
/*globals helpers,pages*/
'use strict';
var specHelper = wcTestTools.helpers.spec;
var testdataHelper = wcTestTools.helpers.testdata;
var FkIntygPage = wcTestTools.pages.intygpages.fkIntyg;
var FkUtkastPage = wcTestTools.pages.intygpages.fk7263Utkast;

describe('Generate fk intyg', function() {

    var intygId = specHelper.generateTestGuid();

    describe('prepare test with intyg', function() {
        it('should generate fk max intyg', function() {
            browser.ignoreSynchronization = false;
            testdataHelper.createIntygFromTemplate('fkMax', intygId).then(function(response) {
                var intyg = JSON.parse(response.request.body);
                expect(intyg.id).not.toBeNull();
            }, function(error) {
                console.log('Error calling createIntyg');
            });
        });
    });

    describe('Login through the welcome page', function() {
        it('with default user', function() {
            specHelper.login();
        });
    });

    var utkastId = null;

    describe('copy fk intyg to new utkast', function() {
        it('should view fk intyg', function() {
            FkIntygPage.get(intygId);
        });

        it('should copy intyg and view resulting utkast', function() {
            FkIntygPage.copyBtn().click();
            FkIntygPage.copyDialogConfirmBtn().click();
            expect(FkUtkastPage.isAt()).toBeTruthy();
        });

        it('fill missing text capacityForWorkForecastText', function() {

            // Save id so it can be removed in cleanup stage.
            browser.getCurrentUrl().then(function(url) {
                utkastId = url.split('/').pop();
            });

            browser.ignoreSynchronization = true;
            FkUtkastPage.getCapacityForWorkForecastText().sendKeys('Litet förtydligande');
            expect(FkUtkastPage.getCapacityForWorkForecastText().getAttribute('value')).toContain('Litet förtydligande');
        });

        it('should sign copy', function() {
            browser.ignoreSynchronization = false;
            FkUtkastPage.whenSigneraButtonIsEnabled().then(function() {
                FkUtkastPage.signeraButtonClick();
                expect(FkIntygPage.isAt()).toBeTruthy();
            });
        });
    });

    describe('remove test intyg', function() {
        it('should clean up all utkast after the test', function() {
            testdataHelper.deleteIntyg(intygId);
            testdataHelper.deleteUtkast(utkastId); // in case the test breaks before it is signed..
            testdataHelper.deleteIntyg(utkastId);
        });
    });
});
