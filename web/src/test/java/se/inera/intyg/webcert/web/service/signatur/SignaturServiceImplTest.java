/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.signatur;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.PagaendeSignering;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.PagaendeSigneringRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.signatur.asn1.ASN1Util;
import se.inera.intyg.webcert.web.service.signatur.asn1.ASN1UtilImpl;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.persistence.OptimisticLockException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.util.ReflectionUtils.setTypedField;

@RunWith(MockitoJUnitRunner.class)
public class SignaturServiceImplTest extends AuthoritiesConfigurationTestSetup {

    private static final String ENHET_ID = "testID";
    private static final String INTYG_ID = "abc123";
    private static final String INTYG_JSON = "A bit of text representing json";
    private static final String INTYG_TYPE = "fk7263";
    private static final String PERSON_ID = "191212121212";
    private static final Long PAGAENDE_SIGN_ID = 1L;

    @Mock
    private UtkastRepository mockUtkastRepository;
    @Mock
    private PagaendeSigneringRepository pagaendeSigneringRepository;
    @Mock
    private IntygService intygService;
    @Mock
    private LogService logService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MonitoringLogService monitoringService;
    @Mock
    private WebCertUserService webcertUserService;
    @Mock
    private IntygModuleRegistry moduleRegistry;
    @Mock
    private ModuleApi moduleApi;

    @Spy
    private ASN1Util asn1Util = new ASN1UtilImpl();

    @InjectMocks
    private SignaturServiceImpl intygSignatureService = new SignaturServiceImpl();

    private Utkast utkast;
    private Utkast completedUtkast;
    private Utkast signedUtkast;
    private HoSPersonal hoSPerson;
    private Vardenhet vardenhet;
    private Vardgivare vardgivare;
    private WebCertUser user;

    private PagaendeSignering pagaendeSignering;

    @Before
    public void setup() throws Exception {
        hoSPerson = new HoSPersonal();
        hoSPerson.setPersonId("AAA");
        hoSPerson.setFullstandigtNamn("Dr Dengroth");

        VardpersonReferens vardperson = new VardpersonReferens();
        vardperson.setHsaId(hoSPerson.getPersonId());
        vardperson.setNamn(hoSPerson.getFullstandigtNamn());

        utkast = createUtkast(INTYG_ID, 1, INTYG_TYPE, UtkastStatus.DRAFT_INCOMPLETE, INTYG_JSON, vardperson, ENHET_ID, PERSON_ID);
        completedUtkast = createUtkast(INTYG_ID, 2, INTYG_TYPE, UtkastStatus.DRAFT_COMPLETE, INTYG_JSON, vardperson, ENHET_ID, PERSON_ID);
        signedUtkast = createUtkast(INTYG_ID, 3, INTYG_TYPE, UtkastStatus.SIGNED, INTYG_JSON, vardperson, ENHET_ID, PERSON_ID);

        vardenhet = new Vardenhet(ENHET_ID, "testNamn");
        vardgivare = new Vardgivare("123", "vardgivare");
        vardgivare.setVardenheter(Collections.singletonList(vardenhet));

        user = createWebCertUser(true);

        when(webcertUserService.getUser()).thenReturn(user);
        when(moduleRegistry.getModuleApi(any())).thenReturn(moduleApi);
        when(moduleApi.updateBeforeSigning(any(), any(), any())).thenReturn(INTYG_JSON);

        Utlatande utlatande = mock(Utlatande.class);
        GrundData grunddata = new GrundData();
        grunddata.setSkapadAv(new HoSPersonal());
        when(utlatande.getGrundData()).thenReturn(grunddata);
        when(moduleApi.getUtlatandeFromJson(anyString())).thenReturn(utlatande);

        setTypedField(intygSignatureService, new SignaturTicketTracker());

        pagaendeSignering = mock(PagaendeSignering.class);
        when(pagaendeSignering.getInternReferens()).thenReturn(PAGAENDE_SIGN_ID);
        when(pagaendeSignering.getIntygData()).thenReturn(INTYG_JSON);
        when(pagaendeSignering.getIntygsId()).thenReturn(INTYG_ID);
        when(pagaendeSignering.getSigneradAvHsaId()).thenReturn(hoSPerson.getPersonId());
        when(pagaendeSignering.getSigneradAvNamn()).thenReturn(hoSPerson.getFullstandigtNamn());

        when(pagaendeSigneringRepository.findOne(anyLong())).thenReturn(pagaendeSignering);
        when(pagaendeSigneringRepository.save(any(PagaendeSignering.class))).thenReturn(pagaendeSignering);
    }

    private WebCertUser createWebCertUser(boolean doctor) {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);
        if (!doctor) {
            role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_ADMIN);
        }

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setNamn(hoSPerson.getFullstandigtNamn());
        user.setHsaId(hoSPerson.getPersonId());
        user.setVardgivare(Collections.singletonList(vardgivare));
        user.setValdVardenhet(vardenhet);
        user.setValdVardgivare(vardgivare);
        user.setAuthenticationMethod(AuthenticationMethod.SITHS);

        return user;
    }

    @Test(expected = WebCertServiceException.class)
    public void getSignatureHashReturnsErrorIfIntygNotCompleted() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(utkast);
        intygSignatureService.createDraftHash(INTYG_ID, utkast.getVersion());
    }

    @Test(expected = WebCertServiceException.class)
    public void getSignatureHashReturnsErrorIfIntygAlreadySigned() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        intygSignatureService.createDraftHash(INTYG_ID, signedUtkast.getVersion());
    }

    @Test(expected = OptimisticLockException.class)
    public void getSignatureHashReturnsErrorIfWrongVersion() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(signedUtkast);
        intygSignatureService.createDraftHash(INTYG_ID, signedUtkast.getVersion() - 1);
    }

    @Test
    public void getSignatureHashReturnsTicket() throws ModuleNotFoundException, ModuleException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        assertEquals(INTYG_ID, ticket.getIntygsId());
        assertEquals(completedUtkast.getVersion(), ticket.getVersion());
        assertEquals(SignaturTicket.Status.BEARBETAR, ticket.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfTicketDoesNotExist() {

        intygSignatureService.clientSignature("unknownId", "SIGNATURE");
    }

    @Test(expected = WebCertServiceException.class)
    public void clientSignatureFailsIfIntygWasModified() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());

        completedUtkast.setModel("{}");

        String signature = buildSignature();

        intygSignatureService.clientSignature(ticket.getId(), signature);
    }

    @Test
    public void clientSignatureSuccess() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        user.setHsaId("TSTNMT2321000156-1025");

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        SignaturTicket status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.BEARBETAR, status.getStatus());

        String signature = buildSignature();

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.clientSignature(ticket.getId(), signature);

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        // Assert pdl log
        verify(logService).logSignIntyg(any(LogRequest.class), isNull());

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());

        // Assert ticket status has changed from BEARBETAR to SIGNERAD
        status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.SIGNERAD, status.getStatus());
    }

    private String buildSignature() throws IOException {
        InputStream is = new ClassPathResource("netid-siths-sig2.txt").getInputStream();
        return "{\"signatur\":\"" + IOUtils.toString(is) + "\"}";
    }

    @Test(expected = IllegalStateException.class)
    public void clientSignaturNoHsaId() throws IOException {
        String signature = "{\"signatur\":\"SIGNATURE\"}";
        intygSignatureService.clientSignature("", signature);
    }

    @Test
    public void clientSignatureKOMPLTSuccess() throws IOException, ModuleNotFoundException {

        when(pagaendeSigneringRepository.findOne(anyLong())).thenReturn(pagaendeSignering);

        completedUtkast.setRelationKod(RelationKod.KOMPLT);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        user.setHsaId("TSTNMT2321000156-1025");

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        SignaturTicket status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.BEARBETAR, status.getStatus());

        String signature = buildSignature();
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.clientSignature(ticket.getId(), signature);

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        // Assert pdl log
        verify(logService).logSignIntyg(any(LogRequest.class), isNull());

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());

        // Assert ticket status has changed from BEARBETAR to SIGNERAD
        status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.SIGNERAD, status.getStatus());
    }

    @Test
    public void clientGrpSignatureSuccess() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        SignaturTicket status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.BEARBETAR, status.getStatus());

        String signature = "{\"signatur\":\"SIGNATURE\"}";

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.clientGrpSignature(ticket.getId(), signature, user);

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        // Assert pdl log
        verify(logService).logSignIntyg(any(LogRequest.class), isNull());

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());

        // Assert ticket status has changed from BEARBETAR to SIGNERAD
        status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.SIGNERAD, status.getStatus());
    }

    @Test
    public void clientGrpSignatureKOMPLTSuccess() throws IOException, ModuleNotFoundException {
        completedUtkast.setRelationKod(RelationKod.KOMPLT);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        SignaturTicket status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.BEARBETAR, status.getStatus());

        String signature = "{\"signatur\":\"SIGNATURE\"}";
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.clientGrpSignature(ticket.getId(), signature, user);

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        // Assert pdl log
        verify(logService).logSignIntyg(any(LogRequest.class), isNull());

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());

        // Assert ticket status has changed from BEARBETAR to SIGNERAD
        status = intygSignatureService.ticketStatus(ticket.getId());
        assertEquals(SignaturTicket.Status.SIGNERAD, status.getStatus());
    }

    @Test
    public void serverSignatureSuccess() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        // Assert pdl log
        verify(logService).logSignIntyg(any(LogRequest.class));

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());
    }

    @Test
    public void serverSignatureSuccessUniqueIntyg() {
        final String replacedId = "replaced";
        final String revokedId = "revoked";

        Feature f = new Feature();
        f.setIntygstyper(ImmutableList.of(INTYG_TYPE));
        f.setGlobal(true);
        user.getFeatures().put(AuthoritiesConstants.FEATURE_UNIKT_INTYG, f);

        Utkast revoked = createUtkast(revokedId, 1L, INTYG_TYPE, UtkastStatus.SIGNED, "model", null, ENHET_ID, PERSON_ID);
        revoked.setSignatur(new Signatur());
        revoked.setAterkalladDatum(LocalDateTime.of(2018, 1, 1, 12, 24));

        Utkast replaced = createUtkast(replacedId, 1L, INTYG_TYPE, UtkastStatus.SIGNED, "model", null, ENHET_ID, PERSON_ID);
        replaced.setSignatur(new Signatur());

        completedUtkast.setRelationIntygsId(replacedId);

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);
        when(mockUtkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(any(String.class), anySet()))
                .thenReturn(ImmutableList.of(revoked, replaced));

        SignaturTicket signatureTicket = intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        verify(logService).logSignIntyg(any(LogRequest.class));

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());
    }

    @Test
    public void serverSignatureFailureUniqueIntyg() {

        Feature f = new Feature();
        f.setIntygstyper(ImmutableList.of(INTYG_TYPE));
        f.setGlobal(true);
        user.getFeatures().put(AuthoritiesConstants.FEATURE_UNIKT_INTYG, f);

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);

        Utkast otherUtkast = createUtkast("otherId", 1L, INTYG_TYPE, UtkastStatus.SIGNED, "", null, "otherEnhet", PERSON_ID);
        otherUtkast.setSignatur(new Signatur());
        when(mockUtkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(any(String.class), anySet()))
                .thenReturn(ImmutableList.of(otherUtkast));

        try {
            intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());
        } catch (WebCertServiceException e) {
            assertEquals(WebCertServiceErrorCodeEnum.INVALID_STATE_INTYG_EXISTS, e.getErrorCode());
        }
    }

    @Test
    public void serverSignatureKOMPLTSuccess() throws IOException, ModuleNotFoundException {
        completedUtkast.setRelationKod(RelationKod.KOMPLT);
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(mockUtkastRepository.save(any(Utkast.class))).thenReturn(completedUtkast);

        // Do the call
        SignaturTicket signatureTicket = intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());

        verify(intygService).storeIntyg(completedUtkast);
        verify(notificationService).sendNotificationForDraftSigned(any(Utkast.class));
        // Assert pdl log
        verify(logService).logSignIntyg(any(LogRequest.class));

        assertNotNull(signatureTicket);

        assertNotNull(completedUtkast.getSignatur());
        assertEquals(UtkastStatus.SIGNED, completedUtkast.getStatus());
    }

    @Test(expected = WebCertServiceException.class)
    public void signeraServerIntygIdDiffers() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);

        when(pagaendeSignering.getIntygsId()).thenReturn("something-else");

        // Do the call
        try {
            intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());
        } finally {
            verify(mockUtkastRepository, times(0)).save(any(Utkast.class));
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void signeraServerHashDiffers() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);

        when(pagaendeSignering.getIntygData()).thenReturn(INTYG_JSON + "-invalid");

        // Do the call
        try {
            intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());
        } finally {
            verify(mockUtkastRepository, times(0)).save(any(Utkast.class));
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void signeraServerNoPagaendeSignaturFound() {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);

        when(pagaendeSigneringRepository.findOne(anyLong())).thenReturn(null);

        // Do the call
        try {
            intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());
        } finally {
            verify(mockUtkastRepository, times(0)).save(any(Utkast.class));
        }
    }

    @Test(expected = WebCertServiceException.class)
    public void userNotAuthorizedDraft() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        user.setVardgivare(Collections.<Vardgivare>emptyList());

        intygSignatureService.createDraftHash(INTYG_ID, 1);
    }

    @Test(expected = WebCertServiceException.class)
    public void userIsNotDoctorDraft() throws IOException {
        user = createWebCertUser(false);

        when(webcertUserService.getUser()).thenReturn(user);

        intygSignatureService.createDraftHash(INTYG_ID, 1);
    }

    @Test(expected = WebCertServiceException.class)
    public void userNotAuthorizedClientSignature() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());
        user.setVardgivare(Collections.<Vardgivare>emptyList());

        intygSignatureService.clientSignature(ticket.getId(), "test");
    }

    @Test(expected = WebCertServiceException.class)
    public void userIsNotDoctorClientSignature() throws IOException {
        user = createWebCertUser(false);

        when(webcertUserService.getUser()).thenReturn(user);

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());

        intygSignatureService.clientSignature(ticket.getId(), "test");
    }

    @Test(expected = WebCertServiceException.class)
    public void userNotAuthorizedServerSignature() throws IOException {
        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        user.setVardgivare(Collections.<Vardgivare>emptyList());

        intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());
    }

    @Test(expected = WebCertServiceException.class)
    public void userIsNotDoctorServerSignature() throws IOException {
        user = createWebCertUser(false);

        when(webcertUserService.getUser()).thenReturn(user);

        intygSignatureService.serverSignature(INTYG_ID, completedUtkast.getVersion());
    }

    @Test(expected = WebCertServiceException.class)
    public void abortClientSignIfHsaIdOnSigDoesNotMatchSession() throws IOException {

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);

        user = createWebCertUser(true);
        user.setAuthenticationMethod(AuthenticationMethod.SITHS);
        when(webcertUserService.getUser()).thenReturn(user);

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());

        String signature = buildSignature();
        intygSignatureService.clientSignature(ticket.getId(), signature);
    }

    @Test(expected = WebCertServiceException.class)
    public void abortClientSignIfPersonIdOnSigDoesNotMatchSession() throws IOException {

        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_PRIVATLAKARE);

        user = createWebCertUser(true);
        user.setAuthenticationMethod(AuthenticationMethod.NET_ID);
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setPrivatLakareAvtalGodkand(true);
        user.setPersonId(PERSON_ID);

        when(mockUtkastRepository.findOne(INTYG_ID)).thenReturn(completedUtkast);
        when(webcertUserService.getUser()).thenReturn(user);

        SignaturTicket ticket = intygSignatureService.createDraftHash(INTYG_ID, completedUtkast.getVersion());

        String signature = buildSignature();
        intygSignatureService.clientSignature(ticket.getId(), signature);
    }

    private Utkast createUtkast(String intygId, long version, String type, UtkastStatus status, String model,
            VardpersonReferens vardperson, String enhetsId, String personId) {

        Utkast utkast = new Utkast();
        utkast.setIntygsId(intygId);
        utkast.setVersion(version);
        utkast.setIntygsTyp(type);
        utkast.setStatus(status);
        utkast.setModel(model);
        utkast.setSkapadAv(vardperson);
        utkast.setSenastSparadAv(vardperson);
        utkast.setEnhetsId(enhetsId);
        utkast.setPatientPersonnummer(Personnummer.createPersonnummer(personId).get());

        return utkast;
    }

}
