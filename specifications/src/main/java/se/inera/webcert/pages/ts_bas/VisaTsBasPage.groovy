package se.inera.webcert.pages.ts_bas

import se.inera.certificate.page.AbstractPage

class VisaTsBasPage extends AbstractPage {

    static at = { js.doneLoading && $("#viewCertAndQA").isDisplayed() }

    static content = {

        intygSaknas { $("#cert-load-error") }
        intygLaddat { $('#intyg-vy-laddad') }

        copyButton { $("#copyBtn") }
        makuleraButton { $("#makuleraBtn") }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
        makuleraDialogKopieraKnapp { $("#button1makulera-dialog") }
        makuleraConfirmationOkButton { $("#confirmationOkButton") }
        skickaDialogCheck { $("#patientSamtycke") }
        skickaDialogSkickaKnapp { $("#button1send-dialog") }

        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }
        certificateIsSentToRecipientMessage(required: false) { $("#certificate-is-sent-to-recipient-message-text") }
        certificateIsRevokedMessage(required: false) { $("#certificate-is-revoked-message-text") }

        patientpostadress { $("#patientpostadress") }
        patientpostnummer { $("#patientpostnummer") }
        patientpostort { $("#patientpostort") }
        intygAvser { $("#intygAvser") }
        identitet { $("#identitet ") }
        synfaltsdefekter { $("#synfaltsdefekter") }
        nattblindhet { $("#nattblindhet ") }
        diplopi { $("#diplopi") }
        nystagmus { $("#nystagmus") }
        hogerOgautanKorrektion { $("#hogerOgautanKorrektion") }
        hogerOgamedKorrektion { $("#hogerOgamedKorrektion") }
        hogerOgakontaktlins { $("#hogerOgakontaktlins") }
        vansterOgautanKorrektion { $("#vansterOgautanKorrektion") }
        vansterOgamedKorrektion { $("#vansterOgamedKorrektion") }
        vansterOgakontaktlins { $("#vansterOgakontaktlins") }
        binokulartutanKorrektion { $("#binokulartutanKorrektion") }
        binokulartmedKorrektion { $("#binokulartmedKorrektion") }
        korrektionsglasensStyrka { $("#korrektionsglasensStyrka") }
        horselBalansbalansrubbningar { $("#horselBalansbalansrubbningar") }
        horselBalanssvartUppfattaSamtal4Meter { $("#horselBalanssvartUppfattaSamtal4Meter") }
        funktionsnedsattning { $("#funktionsnedsattning ") }
        funktionsnedsattningbeskrivning { $("#funktionsnedsattningbeskrivning") }
        funktionsnedsattningotillrackligRorelseformaga { $("#funktionsnedsattningotillrackligRorelseformaga") }
        hjartKarlSjukdom { $("#hjartKarlSjukdom") }
        hjarnskadaEfterTrauma { $("#hjarnskadaEfterTrauma") }
        riskfaktorerStroke { $("#riskfaktorerStroke") }
        beskrivningRiskfaktorer { $("#beskrivningRiskfaktorer") }
        harDiabetes { $("#harDiabetes") }
        diabetesTyp { $("#diabetesTyp") }
        kost { $("#kost") }
        tabletter { $("#tabletter") }
        insulin { $("#insulin") }
        neurologiskSjukdom { $("#neurologiskSjukdom") }
        medvetandestorning { $("#medvetandestorning") }
        medvetandestorningbeskrivning { $("#medvetandestorningbeskrivning") }
        nedsattNjurfunktion { $("#nedsattNjurfunktion") }
        sviktandeKognitivFunktion { $("#sviktandeKognitivFunktion") }
        teckenSomnstorningar { $("#teckenSomnstorningar") }
        teckenMissbruk { $("#teckenMissbruk") }
        foremalForVardinsats { $("#foremalForVardinsats") }
        provtagningBehovs { $("#provtagningBehovs") }
        lakarordineratLakemedelsbruk { $("#lakarordineratLakemedelsbruk") }
        lakemedelOchDos { $("#lakemedelOchDos") }
        psykiskSjukdom { $("#psykiskSjukdom") }
        psykiskUtvecklingsstorning { $("#psykiskUtvecklingsstorning") }
        harSyndrom { $("#harSyndrom") }
        stadigvarandeMedicinering { $("#stadigvarandeMedicinering") }
        medicineringbeskrivning { $("#medicineringbeskrivning") }
        kommentar { $("#kommentar") }
        kommentarEjAngivet { $("#kommentarEjAngivet") }
        bedomning { $("#bedomning") }
        bedomningKanInteTaStallning { $("#bedomningKanInteTaStallning") }
        lakareSpecialKompetens { $("#lakareSpecialKompetens") }
        lakareSpecialKompetensEjAngivet { $("#lakareSpecialKompetensEjAngivet") }
        signeringsdatum { $("#signeringsdatum") }
        vardperson_namn { $("#vardperson_namn") }
        vardperson_enhetsnamn { $("#vardperson_enhetsnamn") }
        vardenhet_postadress { $("#vardenhet_postadress") }
        vardenhet_postnummer { $("#vardenhet_postnummer") }
        vardenhet_postort { $("#vardenhet_postort") }
        vardenhet_telefonnummer { $("#vardenhet_telefonnummer") }
    }

    def copy() {
        $("#copyBtn").click()
        waitFor {
            doneLoading()
        }
        kopieraDialogKopieraKnapp.click()
    }

    def makulera() {
        $("#makuleraBtn").click()
        waitFor {
            doneLoading()
        }
        makuleraDialogKopieraKnapp.click()
    }

    def send() {
        $("#sendBtn").click()
        waitFor {
            doneLoading()
        }
        skickaDialogCheck.click()
        waitFor {
            doneLoading()
        }
        skickaDialogSkickaKnapp.click()
    }
}
