package se.inera.webcert.notifications.routes;

import javax.xml.bind.JAXBException;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.util.toolbox.AggregationStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.process.EnrichWithIntygDataStrategy;
import se.inera.webcert.notifications.process.EnrichWithIntygModelDataStrategy;
import se.inera.webcert.notifications.process.FragaSvarEnricher;
import se.inera.webcert.notifications.service.exception.NonRecoverableCertificateStatusUpdateServiceException;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;

public class ProcessNotificationRequestRouteBuilder extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessNotificationRequestRouteBuilder.class);

    @Autowired
    private EnrichWithIntygModelDataStrategy intygModelEnricher;

    @Autowired
    private EnrichWithIntygDataStrategy intygPropertiesEnricher;

    @Autowired
    private FragaSvarEnricher fragaSvarEnricher;

    @Value("${errorhanding.maxRedeliveries}")
    private int maxRedeliveries;

    @Value("${errorhanding.redeliveryDelay}")
    private long redeliveryDelay;

    @Override
    public void configure() throws Exception {
        //Setup error handling strategy, using redelivery of 3 secs and then exponentially increasing the time interval
        errorHandler(deadLetterChannel("redeliveryExhaustedEndpoint")
                .maximumRedeliveries(maxRedeliveries).redeliveryDelay(redeliveryDelay).useExponentialBackOff());

        onException(NonRecoverableCertificateStatusUpdateServiceException.class)
        .handled(true)
        .to("errorHandlerEndpoint");

        onException(JAXBException.class)
        .handled(true)
        .to("errorHandlerEndpoint");

        from("ref:processNotificationRequestEndpoint").routeId("processNotificationRequest")
        .unmarshal("notificationRequestJaxb")
        .processRef("createAndInitCertificateStatusRequestProcessor")
        //Do not enrich for deleted drafts
        .choice()
            .when(header(RouteHeaders.HANDELSE).isEqualTo(HandelseType.INTYGSUTKAST_RADERAT.toString()))
                .log(LoggingLevel.DEBUG, LOG, "Sending INTYGSUTKAST_RADERAT status update")
                .to("sendCertificateStatusUpdateEndpoint")
            .otherwise()
                .log(LoggingLevel.DEBUG, LOG, "Enriching with data from intyg model")
                .enrich("getIntygFromWebcertRepositoryServiceEndpoint", AggregationStrategies.bean(intygPropertiesEnricher, "enrichWithIntygProperties"))
                .enrich("getIntygModelFromWebcertRepositoryServiceEndpoint", AggregationStrategies.bean(intygModelEnricher, "enrichWithArbetsformagorAndDiagnos"))
                //Check if intyg is signed, in that case enrich with fråga & svar
                .choice()
                    .when(header(RouteHeaders.INTYGS_STATUS).isEqualTo(UtkastStatus.SIGNED))
                        .log(LoggingLevel.DEBUG, LOG, "Enriching with data from fragasvar")
                        .enrich("getNbrOfQuestionsEndpoint", AggregationStrategies.bean(fragaSvarEnricher, "enrichWithNbrOfQuestionsForIntyg"))
                        .enrich("getNbrOfAnsweredQuestionsEndpoint", AggregationStrategies.bean(fragaSvarEnricher, "enrichWithNbrOfAnsweredQuestionsForIntyg"))
                        .enrich("getNbrOfHandledQuestionsEndpoint", AggregationStrategies.bean(fragaSvarEnricher, "enrichWithNbrOfHandledQuestionsForIntyg"))
                        .enrich("getNbrOfHandledAndAnsweredQuestionsEndpoint", AggregationStrategies.bean(fragaSvarEnricher, "enrichWithNbrOfHandledAndAnsweredQuestionsForIntyg"))
                        .to("sendCertificateStatusUpdateEndpoint")
                    .otherwise()
                        .to("sendCertificateStatusUpdateEndpoint");

        from("errorHandlerEndpoint").routeId("errorLogging")
            .log(LoggingLevel.ERROR, LOG, simple("Un-recoverable exception for intygs-id: ${in.headers.intygsId}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
            .stop();

        from("redeliveryExhaustedEndpoint").routeId("redeliveryErrorLogging")
            .log(LoggingLevel.ERROR, LOG, simple("Redelivery attempts exhausted for intygs-id: ${in.headers.intygsId}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
            .stop();
    }
}
