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
package se.inera.intyg.webcert.web.integration.util;

import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;

import java.util.Map;
import java.util.Optional;

public final class AuthoritiesHelperUtil {
    private static AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    private AuthoritiesHelperUtil() {
    }

    public static boolean mayNotCreateUtkastForSekretessMarkerad(SekretessStatus sekretessStatus, IntygUser user,
                                                                 String intygsTyp) {

        if (SekretessStatus.UNDEFINED.equals(sekretessStatus)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                    "Could not fetch sekretesstatus for patient from PU service");
        }
        boolean sekr = sekretessStatus == SekretessStatus.TRUE;
        return (sekr && !authoritiesValidator.given(user, intygsTyp)
                .privilege(AuthoritiesConstants.PRIVILEGE_HANTERA_SEKRETESSMARKERAD_PATIENT).isVerified());
    }

    /**
     * Used for checking if utkast conforms to uniqueness-rules.
     *
     * @return Nothing if utkast conforms, otherwise an error message why it doesn't
     */
    public static Optional<String> validateUtkastMustBeUnique(IntygUser user, String intygsTyp, Map<String, Map<String,
            PreviousIntyg>> intygstypToStringToPreviousIntyg) {
        PreviousIntyg utkastExists = intygstypToStringToPreviousIntyg.get("utkast").get(intygsTyp);

        if (utkastExists != null && utkastExists.isSameVardgivare()
                && authoritiesValidator.given(user, intygsTyp).features(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG).isVerified()) {
            return Optional.of("Draft of this type must be unique within caregiver");
        } else {
            return Optional.empty();
        }
    }

    /**
     * Used for checking if intyg (signed utkast) conforms to uniqueness-rules.
     *
     * @return Nothing if intyg conforms, otherwise an error message why it doesn't
     */
    public static Optional<String> validateIntygMustBeUnique(IntygUser user, String intygsTyp, Map<String, Map<String,
            PreviousIntyg>> intygstypToStringToPreviousIntyg) {
        if (authoritiesValidator.given(user, intygsTyp).features(AuthoritiesConstants.FEATURE_UNIKT_INTYG,
                AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG).isVerified()) {

            PreviousIntyg intygExists = intygstypToStringToPreviousIntyg.get("intyg").get(intygsTyp);

            if (intygExists != null) {
                if (authoritiesValidator.given(user, intygsTyp).features(AuthoritiesConstants.FEATURE_UNIKT_INTYG).isVerified()) {
                    return Optional.of("Certificates of this type must be globally unique.");
                } else if (intygExists.isSameVardgivare() && authoritiesValidator.given(user, intygsTyp).features(
                        AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG).isVerified()) {
                    return Optional.of("Certificates of this type must be unique within this caregiver.");
                }
            }
        }
        return Optional.empty();
    }
}
