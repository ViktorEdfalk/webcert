/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.route;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

import javax.xml.bind.JAXBException;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;

import se.inera.intyg.webcert.notification_sender.exception.PermanentException;
import se.inera.intyg.webcert.notification_sender.exception.TemporaryException;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/notifications/unit-test-notification-sender-config.xml")
@BootstrapWith(CamelTestContextBootstrapper.class)
@MockEndpointsAndSkip("bean:notificationTransformer|bean:notificationWSClient|direct:permanentErrorHandlerEndpoint|direct:temporaryErrorHandlerEndpoint")
public class RouteTest {

    private static final Logger LOG = LoggerFactory.getLogger(RouteTest.class);

    private static final String INTYG_JSON = "{\"id\":\"1234\",\"typ\":\"fk7263\"}";

    private static final String NOTIFICATION_MESSAGE = "{\"intygsId\":\"1234\",\"intygsTyp\":\"fk7263\",\"logiskAdress\":\"SE12345678-1234\",\"handelseTid\":\"2001-12-31T12:34:56.789\",\"handelse\":\"INTYGSUTKAST_ANDRAT\",\"utkast\":"
            + INTYG_JSON + ",\"fragaSvar\":{\"antalFragor\":0,\"antalSvar\":0,\"antalHanteradeFragor\":0,\"antalHanteradeSvar\":0}}";

    @Autowired
    CamelContext camelContext;

    @Produce(uri = "direct:receiveNotificationRequestEndpoint")
    private ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:bean:notificationWSClient")
    private MockEndpoint notificationWSClient;

    @EndpointInject(uri = "mock:bean:notificationTransformer")
    private MockEndpoint notificationTransformer;

    @EndpointInject(uri = "mock:direct:permanentErrorHandlerEndpoint")
    private MockEndpoint permanentErrorHandlerEndpoint;

    @EndpointInject(uri = "mock:direct:temporaryErrorHandlerEndpoint")
    private MockEndpoint temporaryErrorHandlerEndpoint;

    @Before
    public void setup() {
        MockEndpoint.resetMocks(camelContext);
        notificationTransformer.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody(new CertificateStatusUpdateForCareType());
            }
        });
    }

    @Test
    public void testNormalRoute() throws InterruptedException {
        // Given
        notificationWSClient.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(NOTIFICATION_MESSAGE);

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testTransformationException() throws InterruptedException {
        // Given
        notificationTransformer.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                throw new JAXBException("Testing transformation exception");
            }
        });

        notificationWSClient.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(NOTIFICATION_MESSAGE);

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testRuntimeException() throws InterruptedException {
        // Given
        notificationTransformer.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                throw new RuntimeException("Testing runtime exception");
            }
        });

        notificationWSClient.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(NOTIFICATION_MESSAGE);

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testTemporaryException() throws InterruptedException {
        // Given
        notificationWSClient.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                throw new TemporaryException("Testing application error, with exhausted retries");
            }
        });

        notificationWSClient.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(1);

        // When
        producerTemplate.sendBody(NOTIFICATION_MESSAGE);

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

    @Test
    public void testPermanentException() throws InterruptedException {
        // Given
        notificationWSClient.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                LOG.debug("Recieving");
                throw new PermanentException("Testing technical error");
            }
        });

        notificationWSClient.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBody(NOTIFICATION_MESSAGE);

        // Then
        assertIsSatisfied(notificationWSClient);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
    }

}
