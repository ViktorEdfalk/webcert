/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.eleg;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.saml.SAMLCredential;
import se.inera.intyg.webcert.web.auth.common.BaseSAMLCredentialTest;

/**
 * Created by eriklupander on 2015-08-26.
 */
public class ElegAuthenticationAttributeHelperTest extends BaseSAMLCredentialTest {

    private ElegAuthenticationAttributeHelperImpl testee;

    @BeforeClass
    public static void setupAsssertions() throws Exception {
        bootstrapSamlAssertions();
    }

    @Test
    public void testReadStringAttribute() {
        testee = new ElegAuthenticationAttributeHelperImpl();
        SAMLCredential cred = buildPrivatlakareSamlCredential();
        String personId = testee.getAttribute(cred, CgiElegAssertion.PERSON_ID_ATTRIBUTE);
        assertEquals("197705232382", personId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadUnknownAttribute() {
        testee = new ElegAuthenticationAttributeHelperImpl();
        SAMLCredential cred = buildLandstingslakareSamlCredential();
        testee.getAttribute(cred, CgiElegAssertion.PERSON_ID_ATTRIBUTE);
    }

//    @Test
//    public void testReadDOMTypeAttribute() {
//        testee = new ElegAuthenticationAttributeHelperImpl();
//        SAMLCredential cred = buildLandstingslakareSamlCredential();
//        String fornamn = testee.getAttribute(cred, CgiElegAssertion.FORNAMN_ATTRIBUTE);
//        assertEquals("Markus", fornamn);
//    }
}
