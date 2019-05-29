/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/luaeFsIntyg'

// LUAE-FS = Läkarutlåtande för aktivitetsersättning vid förlängd skolgång, FK 7802

describe('LUAE-FS-intyg', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLuaeFsData').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skapaLuaeFsUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LUAE-FS-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar en maximalt ifylld LUAE-FS och skickar den till FK', function () {
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

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        intyg.sektionFunktionsnedsättning(this.intygsdata.funkNedsättning);
        intyg.sektionÖvrigt(this.intygsdata.övrigt);
        intyg.sektionKontakt(this.intygsdata.kontakt);
        intyg.signera();
        intyg.skickaTillFk();
    });
});
