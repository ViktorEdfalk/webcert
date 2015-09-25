package se.inera.auth.eleg;

import org.springframework.security.saml.SAMLCredential;

/**
 * Created by eriklupander on 2015-08-24.
 */
public interface ElegAuthenticationAttributeHelper {
    String getAttribute(SAMLCredential samlCredential, String attributeName);
}
