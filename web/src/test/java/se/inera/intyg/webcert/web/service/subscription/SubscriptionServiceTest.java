/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.subscription;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.web.web.controller.integration.dto.MissingSubscriptionAction;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Map<String, Feature> createFeatures(boolean subscriptionDuringAdjustmentPeriodActive, boolean subscriptionPastAdjustmentPeriodActive) {
        var featureSubscriptionDuringAdjustmentPeriod = new Feature();
        featureSubscriptionDuringAdjustmentPeriod.setGlobal(subscriptionDuringAdjustmentPeriodActive);
        featureSubscriptionDuringAdjustmentPeriod.setName(AuthoritiesConstants.FEATURE_SUBSCRIPTION_DURING_ADJUSTMENT_PERIOD);

        var featureSubscriptionPastAdjustmentPeriod = new Feature();
        featureSubscriptionPastAdjustmentPeriod.setGlobal(subscriptionPastAdjustmentPeriodActive);
        featureSubscriptionPastAdjustmentPeriod.setName(AuthoritiesConstants.FEATURE_SUBSCRIPTION_PAST_ADJUSTMENT_PERIOD);

        var features = new HashMap<String, Feature>();
        features.put(AuthoritiesConstants.FEATURE_SUBSCRIPTION_DURING_ADJUSTMENT_PERIOD, featureSubscriptionDuringAdjustmentPeriod);
        features.put(AuthoritiesConstants.FEATURE_SUBSCRIPTION_PAST_ADJUSTMENT_PERIOD, featureSubscriptionPastAdjustmentPeriod);
        return features;
    }

    @Test
    public void testFetchSubscriptionInfoActionNone() {
        var subscriptionInfo = subscriptionService.fetchSubscriptionInfo(UserOriginType.DJUPINTEGRATION.name(),
            createFeatures(false, false), new ArrayList<>());

        assertEquals(MissingSubscriptionAction.NONE, subscriptionInfo.getMissingSubscriptionAction());
    }

    @Test
    public void testFetchSubscriptionInfoActionWarn() {
        var subscriptionInfo = subscriptionService.fetchSubscriptionInfo(UserOriginType.NORMAL.name(),
            createFeatures(true, false), new ArrayList<>());

        assertEquals(MissingSubscriptionAction.WARN, subscriptionInfo.getMissingSubscriptionAction());
    }

    @Test
    public void testFetchSubscriptionInfoActionBlock() {
        var subscriptionInfo = subscriptionService.fetchSubscriptionInfo(UserOriginType.NORMAL.name(),
            createFeatures(false, true), new ArrayList<>());

        assertEquals(MissingSubscriptionAction.BLOCK, subscriptionInfo.getMissingSubscriptionAction());

        subscriptionInfo = subscriptionService.fetchSubscriptionInfo(UserOriginType.NORMAL.name(),
            createFeatures(true, true), new ArrayList<>());

        assertEquals(MissingSubscriptionAction.BLOCK, subscriptionInfo.getMissingSubscriptionAction());
    }

}
