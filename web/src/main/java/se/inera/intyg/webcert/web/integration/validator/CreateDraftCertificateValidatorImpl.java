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
package se.inera.intyg.webcert.web.integration.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.WebcertFeature;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Enhet;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.HosPersonal;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Patient;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.TypAvUtlatande;

@Component
public class CreateDraftCertificateValidatorImpl extends BaseCreateDraftCertificateValidator implements CreateDraftCertificateValidator {

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.integration.validator.CreateDraftCertificateValidator#validate(se.inera.certificate.
     * clinicalprocess
     * .healthcond.certificate.createdraftcertificateresponder.v1.UtlatandeType)
     */
    @Override
    public ResultValidator validate(Utlatande utlatande) {
        ResultValidator errors = ResultValidator.newInstance();

        validateTypAvUtlatande(errors, utlatande.getTypAvUtlatande());
        validatePatient(errors, utlatande.getPatient());
        validateSkapadAv(errors, utlatande.getSkapadAv());

        return errors;
    }

    @Override
    public ResultValidator validateApplicationErrors(Utlatande utlatande, IntygUser user) {
        ResultValidator errors = ResultValidator.newInstance();

        String personId = utlatande.getPatient().getPersonId().getExtension();
        Personnummer personnummer = Personnummer.createValidatedPersonnummerWithDash(personId).orElse(null);

        // Check if PU-service is responding
        validatePUServiceAvailibility(errors, personnummer);
        if (errors.hasErrors()) {
            return errors;
        }

        validateSekretessmarkeringOchIntygsTyp(errors, personnummer, utlatande.getSkapadAv(), utlatande.getTypAvUtlatande(), user);
        validateCreateForAvlidenPatientAllowed(errors, personnummer, utlatande.getTypAvUtlatande().getCode());

        return errors;
    }

    private void validateSekretessmarkeringOchIntygsTyp(ResultValidator errors,
                                                        Personnummer personnummer,
                                                        HosPersonal skapadAv,
                                                        TypAvUtlatande typAvUtlatande,
                                                        IntygUser user) {

        // If intygstyp is NOT allowed to issue for sekretessmarkerad patient
        // we check sekretessmarkerad state through the PU-service.
        String intygsTyp = IntygsTypToInternal.convertToInternalIntygsTyp(typAvUtlatande.getCode());

        AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        if (!authoritiesValidator.given(user, intygsTyp)
                .features(WebcertFeature.HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .isVerified()) {
            errors.addError("Du saknar behörighet att skapa intyg med denna typ.");
        } else {
            validateBusinessRulesForSekretessmarkeradPatient(errors, personnummer, intygsTyp, user);
        }
    }

    private void validateTypAvUtlatande(ResultValidator errors, TypAvUtlatande typAvUtlatandeType) {
        String intygsTyp = typAvUtlatandeType.getCode();

        if (!moduleRegistry.moduleExists(intygsTyp)) {
            errors.addError("Intyg {0} is not supported", intygsTyp);
        }
    }

    private void  validatePatient(ResultValidator errors, Patient patient) {

        if (Strings.nullToEmpty(patient.getEfternamn()).trim().isEmpty()) {
            errors.addError("efternamn is required");
        }

        if (patient.getFornamn() == null || patient.getFornamn().isEmpty()) {
            errors.addError("At least one fornamn is required");
        }

        if (patient.getPersonId() == null || Strings.nullToEmpty(patient.getPersonId().getExtension()).trim().isEmpty()) {
            errors.addError("personId is required");
        } else {
            validatePersonnummer(errors, patient.getPersonId().getExtension());
        }
    }

    private void validateSkapadAv(ResultValidator errors, HosPersonal skapadAv) {
        if (Strings.nullToEmpty(skapadAv.getFullstandigtNamn()).trim().isEmpty()) {
            errors.addError("Physicians full name is required");
        }

        if (skapadAv.getPersonalId() == null || Strings.nullToEmpty(skapadAv.getPersonalId().getExtension()).trim().isEmpty()) {
            errors.addError("Physicians hsaId is required");
        }

        validateEnhet(errors, skapadAv.getEnhet());
    }

    private void validateEnhet(ResultValidator errors, Enhet enhet) {
        if (enhet == null) {
            errors.addError("Enhet is missing");
        } else {
            if (Strings.nullToEmpty(enhet.getEnhetsnamn()).trim().isEmpty()) {
                errors.addError("enhetsnamn is required");
            }

            if (enhet.getEnhetsId() == null || Strings.nullToEmpty(enhet.getEnhetsId().getExtension()).trim().isEmpty()) {
                errors.addError("enhetsId is required");
            }
        }
    }

}
