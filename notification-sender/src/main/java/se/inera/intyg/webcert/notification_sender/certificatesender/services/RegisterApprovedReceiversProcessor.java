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
package se.inera.intyg.webcert.notification_sender.certificatesender.services;

import java.io.IOException;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversResponseType;
import se.inera.intyg.clinicalprocess.healthcond.certificate.registerapprovedreceivers.v1.RegisterApprovedReceiversType;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;

public class RegisterApprovedReceiversProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterApprovedReceiversProcessor.class);

    @Autowired
    private RegisterApprovedReceiversResponderInterface registerApprovedReceiversClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void process(@Body String jsonBody, @Header(Constants.INTYGS_ID) String intygsId,
            @Header(Constants.LOGICAL_ADDRESS) String logicalAddress) throws TemporaryException, PermanentException {

        List<String> receiverIds = transformMessageBodyToReceiverList(jsonBody);

        try {
            RegisterApprovedReceiversType req = new RegisterApprovedReceiversType();
            IntygId intygId = new IntygId();
            intygId.setExtension(intygsId);
            req.setIntygId(intygId);
            req.getApprovedReceivers().addAll(receiverIds);

            RegisterApprovedReceiversResponseType responseType = registerApprovedReceiversClient.registerApprovedReceivers(logicalAddress,
                    req);

            // Any problems on the other end that yields an ERROR result are treated as PermanentExceptions.
            if (responseType.getResult().getResultCode() == ResultCodeType.ERROR) {
                throw new PermanentException(responseType.getResult().getResultText());
            }
        } catch (WebServiceException e) {
            LOG.error("Call to sendMessageToRecipient for intyg {} caused an error: {}. Will retry",
                    intygsId, e.getMessage());
            throw new TemporaryException(e.getMessage());
        }
    }

    private List<String> transformMessageBodyToReceiverList(@Body String jsonBody) throws PermanentException {
        try {
            return objectMapper.readValue(jsonBody, new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            throw new PermanentException("Could not parse message body into list of approved receivers.");
        }
    }
}
