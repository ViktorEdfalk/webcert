package se.inera.webcert.service;

import static org.joda.time.LocalDateTime.now;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.jms.Session;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import se.inera.log.messages.ActivityPurpose;
import se.inera.log.messages.ActivityType;
import se.inera.log.messages.IntygReadMessage;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.web.service.WebCertUserService;
import se.inera.webcert.web.service.WebCertUserServiceImpl;

/**
 * Created by pehr on 13/11/13.
 */
@RunWith(MockitoJUnitRunner.class)
public class LogServiceImplTest {

    @Mock
    private JmsTemplate template = mock(JmsTemplate.class);

    @Mock
    protected WebCertUserService webCertUserService = new WebCertUserServiceImpl();

    @InjectMocks
    LogServiceImpl logService = new LogServiceImpl();

    @Before
    public void setup() {
        logService.systemId = "webcert";
        logService.systemName = "WebCert";
    }
    
    @Test
    public void serviceSendsDocumentAndIdForCreate() throws Exception {
        
        ArgumentCaptor<MessageCreator> messageCreatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);

        when(webCertUserService.getWebCertUser()).thenReturn(createWcUser());

        //GetCertificateForCareResponseType certificate = certificate();

        logService.logReadOfIntyg("abc123", "19121212-1212");

        verify(template, only()).send(messageCreatorCaptor.capture());

        MessageCreator messageCreator = messageCreatorCaptor.getValue();

        Session session = mock(Session.class);
        ArgumentCaptor<IntygReadMessage> intygReadMessageCaptor = ArgumentCaptor.forClass(IntygReadMessage.class);
        when(session.createObjectMessage(intygReadMessageCaptor.capture())).thenReturn(null);

        messageCreator.createMessage(session);

        IntygReadMessage intygReadMessage = intygReadMessageCaptor.getValue();

        assertNotNull(intygReadMessage.getLogId());
        assertEquals(ActivityType.READ, intygReadMessage.getActivityType());
        assertEquals(ActivityPurpose.CARE_TREATMENT, intygReadMessage.getPurpose());
        assertEquals("Intyg", intygReadMessage.getResourceType());
        assertEquals("abc123", intygReadMessage.getActivityLevel());

        assertEquals("HSAID", intygReadMessage.getUserId());
        assertEquals("Markus Gran", intygReadMessage.getUserName());

        assertEquals("VARDENHET_ID", intygReadMessage.getEnhet().getEnhetsId());
        assertEquals("Vårdenheten", intygReadMessage.getEnhet().getEnhetsNamn());
        assertEquals("VARDGIVARE_ID", intygReadMessage.getEnhet().getVardgivareId());
        assertEquals("Vårdgivaren", intygReadMessage.getEnhet().getVardgivareNamn());

        assertEquals("19121212-1212", intygReadMessage.getPatient().getPatientId());
        //assertEquals("Hans Olof van der Test", intygReadMessage.getPatient().getPatientNamn());

        assertTrue(intygReadMessage.getTimestamp().minusSeconds(10).isBefore(now()));
        assertTrue(intygReadMessage.getTimestamp().plusSeconds(10).isAfter(now()));

        assertEquals("webcert", intygReadMessage.getSystemId());
        assertEquals("WebCert", intygReadMessage.getSystemName());
    }

    private WebCertUser createWcUser() {
        
        Vardenhet ve = new Vardenhet("VARDENHET_ID", "Vårdenheten");
        
        Vardgivare vg = new Vardgivare("VARDGIVARE_ID", "Vårdgivaren");
        vg.setVardenheter(Arrays.asList(ve));
        
        WebCertUser wcu = new WebCertUser();
        wcu.setVardgivare(Arrays.asList(vg));
        wcu.changeValdVardenhet("VARDENHET_ID");
        
        wcu.setHsaId("HSAID");
        wcu.setNamn("Markus Gran");
        
        return wcu;
    }
}
