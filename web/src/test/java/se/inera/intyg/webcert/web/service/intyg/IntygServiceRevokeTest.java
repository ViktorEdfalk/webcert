/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.intyg;

import org.apache.cxf.helpers.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.common.fk7263.model.internal.Fk7263Utlatande;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateMetaData;
import se.inera.intyg.common.support.modules.support.api.dto.CertificateResponse;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacadeException;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceRevokeTest extends AbstractIntygServiceTest {

    private static final String REVOKE_MSG = "This is revoked";
    private static final String REVOKE_REASON = "FELAKTIGT_INTYG";
    private static final String INTYG_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE = "fk7263";
    private static final String INTYG_TYPE_VERSION = "1.0";
    private static final String HSA_ID = "AAA";

    private static final String INTYG_ID = "123";

    private static final String USER_REFERENCE = "some-ref";

    private Utkast signedUtkast;
    private Utkast revokedUtkast;

    @Before
    public void setup() throws Exception {
        HoSPersonal person = buildHosPerson();
        VardpersonReferens vardperson = buildVardpersonReferens(person);
        WebCertUser user = buildWebCertUser(person);

        signedUtkast = buildUtkast(INTYG_ID, INTYG_TYPE, INTYG_TYPE_VERSION, UtkastStatus.SIGNED, INTYG_JSON, vardperson);
        revokedUtkast = buildUtkast(INTYG_ID, INTYG_TYPE, INTYG_TYPE_VERSION, UtkastStatus.SIGNED, json, vardperson);
        revokedUtkast.setAterkalladDatum(LocalDateTime.now());

        when(webCertUserService.getUser()).thenReturn(user);
    }

    @Override
    @Before
    public void setupMocks() throws Exception {
        json = FileUtils.getStringFromFile(new ClassPathResource("IntygServiceTest/utlatande.json").getFile());
        utlatande = objectMapper.readValue(json, Fk7263Utlatande.class);
        CertificateMetaData metaData = buildCertificateMetaData();
        certificateResponse = new CertificateResponse(json, utlatande, metaData, false);
        when(moduleFacade.getCertificate(any(String.class), any(String.class), anyString())).thenReturn(certificateResponse);
        when(intygRelationHelper.getRelationsForIntyg(anyString())).thenReturn(new Relations());

        when(patientDetailsResolver.resolvePatient(any(Personnummer.class), anyString(), anyString())).thenReturn(buildPatient(false, false));
        when(moduleRegistry.getModuleApi(anyString(), anyString())).thenReturn(moduleApi);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(new Fk7263Utlatande());
        when(moduleApi.updateBeforeViewing(anyString(), any(Patient.class))).thenReturn("MODEL");
    }

    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), eq(true))).thenReturn(true);
    }

    @Test
    public void testRevokeIntyg() throws Exception {

        when(logRequestFactory.createLogRequestFromUtlatande(any(Utlatande.class))).thenReturn(new LogRequest());
        when(intygRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);

        // do the call
        IntygServiceResult res = intygService.revokeIntyg(INTYG_ID, INTYG_TYP_FK, REVOKE_MSG, REVOKE_REASON);

        // verify that services were called
        verify(arendeService).closeAllNonClosedQuestions(INTYG_ID);
        verify(notificationService, times(1)).sendNotificationForIntygRevoked(INTYG_ID);
        verify(logService).logRevokeIntyg(any(LogRequest.class));
        verify(intygRepository).save(any(Utkast.class));
        verify(certificateSenderService, times(1)).revokeCertificate(eq(INTYG_ID), any(), eq(INTYG_TYP_FK), eq(INTYG_TYPE_VERSION));
        verify(moduleFacade, times(1)).getRevokeCertificateRequest(eq(INTYG_TYP_FK), any(), any(), eq(REVOKE_MSG));
        verify(monitoringService).logIntygRevoked(INTYG_ID, HSA_ID, REVOKE_REASON);

        assertEquals(IntygServiceResult.OK, res);
    }

    @Test(expected = WebCertServiceException.class)
    public void testRevokeIntygThatHasAlreadyBeenRevokedFails() throws IntygModuleFacadeException {
        when(intygRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        when(moduleFacade.getCertificate(anyString(), anyString(), anyString())).thenThrow(new IntygModuleFacadeException(""));
        // Do the call
        try {
            intygService.revokeIntyg(INTYG_ID, INTYG_TYP_FK, REVOKE_MSG, REVOKE_REASON);
        } finally {
            verifyZeroInteractions(certificateSenderService);
            verify(intygRepository, times(0)).save(any(Utkast.class));
            verifyZeroInteractions(notificationService);
            verifyZeroInteractions(logService);
        }
    }

    private HoSPersonal buildHosPerson() {
        HoSPersonal person = new HoSPersonal();
        person.setPersonId(HSA_ID);
        person.setFullstandigtNamn("Dr Dengroth");
        return person;
    }

    private Utkast buildUtkast(String intygId, String type, String intygTypeVersion, UtkastStatus status, String model, VardpersonReferens vardperson) {

        Utkast intyg = new Utkast();
        intyg.setIntygsId(intygId);
        intyg.setIntygsTyp(type);
        intyg.setIntygTypeVersion(intygTypeVersion);
        intyg.setStatus(status);
        intyg.setModel(model);
        intyg.setSkapadAv(vardperson);
        intyg.setSenastSparadAv(vardperson);

        return intyg;
    }

    private VardpersonReferens buildVardpersonReferens(HoSPersonal person) {
        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(person.getPersonId());
        vardperson.setNamn(person.getFullstandigtNamn());
        return vardperson;
    }

    private WebCertUser buildWebCertUser(HoSPersonal person) {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());
        user.setParameters(new IntegrationParameters(USER_REFERENCE, "", "", "", "", "", "", "", "", false, false, false, true));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setNamn(person.getFullstandigtNamn());
        user.setHsaId(person.getPersonId());

        return user;
    }

}
