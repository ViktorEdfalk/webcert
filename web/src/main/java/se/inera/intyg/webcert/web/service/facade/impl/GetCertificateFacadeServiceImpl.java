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

package se.inera.intyg.webcert.web.service.facade.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.util.IntygToCertificateConverter;
import se.inera.intyg.webcert.web.service.facade.util.UtkastToCertificateConverter;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@Service
public class GetCertificateFacadeServiceImpl implements GetCertificateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(GetCertificateFacadeServiceImpl.class);

    private final UtkastService utkastService;

    private final IntygService intygService;

    private final UtkastToCertificateConverter utkastToCertificateConverter;

    private final IntygToCertificateConverter intygToCertificateConverter;

    @Autowired
    public GetCertificateFacadeServiceImpl(UtkastService utkastService,
        IntygService intygService,
        UtkastToCertificateConverter utkastToCertificateConverter,
        IntygToCertificateConverter intygToCertificateConverter) {
        this.utkastService = utkastService;
        this.intygService = intygService;
        this.utkastToCertificateConverter = utkastToCertificateConverter;
        this.intygToCertificateConverter = intygToCertificateConverter;
    }

    @Override
    public Certificate getCertificate(String certificateId, boolean pdlLog) {
        final Utkast utkast = getCertificateFromWebcert(certificateId, pdlLog);
        if (utkast == null) {
            LOG.debug("Retrieving Intyg '{}' from IntygService with pdlLog argument as '{}'", certificateId, pdlLog);
            final var intygContentHolder = intygService.fetchIntygData(certificateId, null, pdlLog);

            LOG.debug("Converting IntygContentHolder to Certificate");
            return intygToCertificateConverter.convert(intygContentHolder);
        }

        LOG.debug("Converting Utkast to Certificate");
        return utkastToCertificateConverter.convert(utkast);
    }

    private Utkast getCertificateFromWebcert(String certificateId, boolean pdlLog) {
        try {
            LOG.debug("Retrieving Utkast '{}' from UtkastService with pdlLog argument as '{}'", certificateId, pdlLog);
            return utkastService.getDraft(certificateId, pdlLog);
        } catch (WebCertServiceException ex) {
            if (ex.getErrorCode().equals(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND)) {
                LOG.debug("Utkast with id '{}' doesn't exist in Webcert", certificateId);
                return null;
            }
            throw ex;
        }
    }
}
