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
package se.inera.intyg.webcert.web.web.controller.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.WebUserFeaturesRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserApiControllerTest {

    @InjectMocks
    UserApiController userApiController;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private WebCertUser webCertUser;

    @Captor
    private ArgumentCaptor<Map<String, Feature>> captor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(webCertUserService.getUser()).thenReturn(webCertUser);
    }

    @Test
    public void testUserFeaturesEnableDisabled() {
        //Given
        final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
        webUserFeaturesRequest.setJsLoggning(true);

        final HashMap<String, Feature> features = new HashMap<>();
        when(webCertUser.getFeatures()).thenReturn(features);

        //When
        userApiController.userFeatures(webUserFeaturesRequest);

        //Then
        Mockito.verify(webCertUser, times(1)).setFeatures(captor.capture());
        assertTrue(captor.getValue().containsKey(AuthoritiesConstants.FEATURE_JS_LOGGNING));
    }

    @Test
    public void testUserFeaturesDisableEnabled() {
        //Given
        final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
        webUserFeaturesRequest.setJsLoggning(false);

        final HashMap<String, Feature> features = new HashMap<>();
        Feature f1 = new Feature();
        f1.setName(AuthoritiesConstants.FEATURE_JS_LOGGNING);
        features.put(f1.getName(), f1);
        when(webCertUser.getFeatures()).thenReturn(features);

        //When
        userApiController.userFeatures(webUserFeaturesRequest);

        //Then
        Mockito.verify(webCertUser, times(1)).setFeatures(captor.capture());
        assertFalse(captor.getValue().containsKey(AuthoritiesConstants.FEATURE_JS_LOGGNING));
    }

    @Test
    public void testUserFeaturesEnableEnabled() {
        //Given
        final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
        webUserFeaturesRequest.setJsLoggning(true);

        final HashMap<String, Feature> features = new HashMap<>();
        Feature f1 = new Feature();
        f1.setName(AuthoritiesConstants.FEATURE_JS_LOGGNING);
        features.put(f1.getName(), f1);
        when(webCertUser.getFeatures()).thenReturn(features);

        //When
        userApiController.userFeatures(webUserFeaturesRequest);

        //Then
        Mockito.verify(webCertUser, times(1)).setFeatures(captor.capture());
        assertTrue(captor.getValue().containsKey(AuthoritiesConstants.FEATURE_JS_LOGGNING));
    }

    @Test
    public void testUserFeaturesDisableDisabled() {
        //Given
        final WebUserFeaturesRequest webUserFeaturesRequest = new WebUserFeaturesRequest();
        webUserFeaturesRequest.setJsLoggning(false);

        final HashMap<String, Feature> features = new HashMap<>();
        when(webCertUser.getFeatures()).thenReturn(features);

        //When
        userApiController.userFeatures(webUserFeaturesRequest);

        //Then
        Mockito.verify(webCertUser, times(1)).setFeatures(captor.capture());
        assertFalse(captor.getValue().containsKey(AuthoritiesConstants.FEATURE_JS_LOGGNING));
    }

    @Test
    public void testLogout() {
        String sessionId = "sessionId";
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);

        when(attributes.getSessionId()).thenReturn(sessionId);

        RequestContextHolder.setRequestAttributes(attributes);

        userApiController.logoutUserAfterTimeout();

        verify(webCertUserService).scheduleSessionRemoval(eq(sessionId), isNull());
    }

    @Test
    public void testLogoutCancel() {
        String sessionId = "sessionId";
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);

        when(attributes.getSessionId()).thenReturn(sessionId);

        RequestContextHolder.setRequestAttributes(attributes);

        userApiController.cancelLogout();

        verify(webCertUserService).cancelScheduledLogout(sessionId);
    }
}
