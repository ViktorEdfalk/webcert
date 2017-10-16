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

package se.inera.intyg.webcert.web.web.controller.integration;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.infra.integration.hsa.model.SelectableVardenhet;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.service.CommonFeatureService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.test.TestIntygFactory;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Stream.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Magnus Ekstrand on 2017-10-13.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntygIntegrationServiceImplTest {

    private final String ALTERNATE_SSN = "19010101-0101";

    private final String INTYGSTYP = "lisjp";
    private final String INTYGSID = "A1234-B5678-C90123-D4567";
    private final String ENHETSID = "11111";

    private final String VARDENHETID_USER = "222222";
    private final String VARDENHETNAMN_USER = "Vardenhet2";
    private final String VARDGIVAREID_USER = "vg1";
    private final String VARDGIVARENAMN_USER = "Vardgivare1";
    private final String VARDGIVAREID_UTKAST = "vg2";
    private final String VARDGIVARENAMN_UTKAST = "Vardgivare2";


    @Mock
    private MonitoringLogService monitoringLog;

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private UtkastRepository utkastRepository;

    @Mock
    private UtkastService utkastService;

    @Mock
    private CommonFeatureService commonFeatureService;

    @InjectMocks
    private IntygIntegrationServiceImpl testee;


    @Before
    public void setupMock() {
        doNothing().when(monitoringLog).logIntegratedOtherCaregiver(anyString(), anyString(), anyString(), anyString());
        doNothing().when(monitoringLog).logIntegratedOtherUnit(anyString(), anyString(), anyString());
    }

    @Test
    public void prepareRedirectToIntygSuccess() throws Exception {
        // given
        when(utkastRepository.findOne(anyString())).thenReturn(createUtkast());
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);

        IntegrationParameters parameters = new IntegrationParameters(null, null, ALTERNATE_SSN,
                "Nollan", null, "Nollansson", "Nollgatan", "000000", "Nollby",
                false, false, false, false);

        WebCertUser user = createDefaultUser();
        user.setParameters(parameters);

        // when
        PrepareRedirectToIntyg prepareRedirectToIntyg = testee.prepareRedirectToIntyg(INTYGSTYP, INTYGSID, user);

        // then
        verify(utkastRepository).findOne(anyString());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));

        assertEquals(INTYGSTYP, prepareRedirectToIntyg.getIntygTyp());
        assertEquals(INTYGSID, prepareRedirectToIntyg.getIntygId());
        assertTrue(prepareRedirectToIntyg.isUtkast());
    }

    @Test
    public void userIsAuthorizedToHandleSekretessmarkeradPatient() throws Exception {
        // given
        when(utkastRepository.findOne(anyString())).thenReturn(createUtkast());
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);

        IntegrationParameters parameters = new IntegrationParameters(null, null, ALTERNATE_SSN,
                "Nollan", null, "Nollansson", "Nollgatan", "000000", "Nollby",
                false, false, false, false);

        Privilege p = createPrivilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT,
                Arrays.asList("lisjp", "ts-bas"), // p1 is restricted to these intygstyper
                Arrays.asList(
                        createRequestOrigin(WebCertUserOriginType.DJUPINTEGRATION.name(), Arrays.asList("lisjp")),
                        createRequestOrigin(WebCertUserOriginType.DJUPINTEGRATION.name(), Arrays.asList("ts-bas"))));

        WebCertUser user = createDefaultUser();
        user.setParameters(parameters);
        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT, p);

        // when
        PrepareRedirectToIntyg prepareRedirectToIntyg = testee.prepareRedirectToIntyg(INTYGSTYP, INTYGSID, user);

        // then
        verify(utkastRepository).findOne(anyString());
        verify(patientDetailsResolver).getSekretessStatus(any(Personnummer.class));

        assertEquals(INTYGSTYP, prepareRedirectToIntyg.getIntygTyp());
        assertEquals(INTYGSID, prepareRedirectToIntyg.getIntygId());
        assertTrue(prepareRedirectToIntyg.isUtkast());
    }

    @Test
    public void verifyMonitoringWhenSammanhallenSjukforingAndOtherVardgivare() throws Exception {
        // given
        when(utkastRepository.findOne(anyString())).thenReturn(createUtkast());
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);

        IntegrationParameters parameters = new IntegrationParameters(null, null, ALTERNATE_SSN,
                "Nollan", null, "Nollansson", "Nollgatan", "000000", "Nollby",
                true, false, false, false);

        WebCertUser user = createDefaultUser();
        user.setParameters(parameters);
        user.setValdVardgivare(createVardgivare());

        // when
        testee.prepareRedirectToIntyg(INTYGSTYP, INTYGSID, user);

        // then
        verify(monitoringLog).logIntegratedOtherCaregiver(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void verifyMonitoringWhenSammanhallenSjukforingAndOtherVardenhet() throws Exception {
        // given
        when(utkastRepository.findOne(anyString())).thenReturn(createUtkast());
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);

        IntegrationParameters parameters = new IntegrationParameters(null, null, ALTERNATE_SSN,
                "Nollan", null, "Nollansson", "Nollgatan", "000000", "Nollby",
                true, false, false, false);

        WebCertUser user = createDefaultUser();
        user.setParameters(parameters);
        user.setValdVardgivare(createVardenhet());

        // when
        testee.prepareRedirectToIntyg(INTYGSTYP, INTYGSID, user);

        // then
        verify(monitoringLog).logIntegratedOtherCaregiver(anyString(), anyString(), anyString(), anyString());

    }

    @Test(expected = WebCertServiceException.class)
    public void invalidParametersShouldThrowException() throws Exception {
        // given
        when(utkastRepository.findOne(anyString())).thenReturn(createUtkast());
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);

        IntegrationParameters parameters = new IntegrationParameters(null, null, ALTERNATE_SSN,
                null, null, "Nollansson", "Nollgatan", "000000", "Nollby",
                false, false, false, false);

        WebCertUser user = createDefaultUser();
        user.setParameters(parameters);

        // when
        testee.prepareRedirectToIntyg(INTYGSTYP, INTYGSID, user);

        // if code reaches this point we fail the test
        fail();
    }

    @Test(expected = WebCertServiceException.class)
    public void expectExceptionWhenSekretessStatusIsUndefined() throws Exception {
        // given
        when(utkastRepository.findOne(anyString())).thenReturn(createUtkast());
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);

        IntegrationParameters parameters = new IntegrationParameters(null, null, ALTERNATE_SSN,
                "Nollan", null, "Nollansson", "Nollgatan", "000000", "Nollby",
                false, false, false, false);

        WebCertUser user = createDefaultUser();
        user.setParameters(parameters);

        // when
        testee.prepareRedirectToIntyg(INTYGSTYP, INTYGSID, user);

        // if code reaches this point we fail the test
        fail();
    }

    @Test
    public void ensureDraftPatientInfoUpdated() throws Exception {

        IntegrationParameters parameters = new IntegrationParameters(null, null, ALTERNATE_SSN,
                null, null, null, null, null, null,
                false, false, false, false);

        WebCertUser user = createDefaultUser();
        user.setParameters(parameters);

        testee.ensureDraftPatientInfoUpdated("lisjp", null, 0l, user);
    }

    private SelectableVardenhet createVardenhet() {
        SelectableVardenhet selectableVardenhet = new SelectableVardenhet() {
            @Override
            public String getId() {
                return VARDENHETID_USER;
            }

            @Override
            public String getNamn() {
                return VARDENHETNAMN_USER;
            }

            @Override
            public List<String> getHsaIds() {
                return Arrays.asList(VARDENHETID_USER);
            }
        };
        return selectableVardenhet;
    }

    private SelectableVardenhet createVardgivare() {
        SelectableVardenhet selectableVardenhet = new SelectableVardenhet() {
            @Override
            public String getId() {
                return VARDGIVAREID_USER;
            }

            @Override
            public String getNamn() {
                return VARDGIVARENAMN_USER;
            }

            @Override
            public List<String> getHsaIds() {
                return null;
            }
        };
        return selectableVardenhet;
    }

    private Utkast createUtkast() {
        Utkast utkast =  TestIntygFactory.createUtkast(INTYGSID, LocalDateTime.now());
        utkast.setIntygsTyp(INTYGSTYP);
        utkast.setVardgivarId(VARDGIVAREID_UTKAST);
        utkast.setVardgivarNamn(VARDGIVARENAMN_UTKAST);
        utkast.setEnhetsId(ENHETSID);
        return utkast;
    }

    private RequestOrigin createRequestOrigin(String name, List<String> intygstyper) {
        RequestOrigin o = new RequestOrigin();
        o.setName(name);
        o.setIntygstyper(intygstyper);
        return o;
    }

    private Privilege createPrivilege(String name, List<String> intygsTyper, List<RequestOrigin> requestOrigins) {
        Privilege p = new Privilege();
        p.setName(name);
        p.setIntygstyper(intygsTyper);
        p.setRequestOrigins(requestOrigins);
        return p;
    }

    private WebCertUser createUser(String roleName, Privilege p, Set<String> features, String origin) {
        WebCertUser user = new WebCertUser();

        HashMap<String, Privilege> pMap = new HashMap<>();
        pMap.put(p.getName(), p);
        user.setAuthorities(pMap);

        user.setOrigin(origin);
        user.setFeatures(features);

        HashMap<String, Role> rMap = new HashMap<>();
        Role role = new Role();
        role.setName(roleName);
        rMap.put(roleName, role);

        user.setRoles(rMap);
        return user;
    }

    private WebCertUser createDefaultUser() {
        return createUser(AuthoritiesConstants.ROLE_LAKARE,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
                        Arrays.asList("lisjp", "ts-bas"), // p1 is restricted to these intygstyper
                        Arrays.asList(
                                createRequestOrigin(WebCertUserOriginType.DJUPINTEGRATION.name(), Arrays.asList("lisjp")),
                                createRequestOrigin(WebCertUserOriginType.DJUPINTEGRATION.name(), Arrays.asList("ts-bas")))),
                ImmutableSet.of(WebcertFeature.HANTERA_INTYGSUTKAST.getName(), WebcertFeature.HANTERA_INTYGSUTKAST.getName() + ".lisjp",
                        "base_feature"), WebCertUserOriginType.DJUPINTEGRATION.name());
    }

    private List<RequestOrigin> getOriginList(RequestOrigin... requestOrigin) {
        return of(requestOrigin).collect(toCollection(ArrayList::new));
    }

    private List<String> getStringList(String... values) {
        return of(values).collect(toCollection(ArrayList::new));
    }

    private Set<String> getStringSet(String... values) {
        return of(values).collect(toCollection(HashSet::new));
    }
}