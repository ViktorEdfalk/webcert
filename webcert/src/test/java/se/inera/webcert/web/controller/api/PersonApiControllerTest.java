package se.inera.webcert.web.controller.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.webcert.pu.model.Person;
import se.inera.webcert.pu.services.PUService;
import se.inera.webcert.web.controller.api.dto.PersonuppgifterResponse;

import javax.ws.rs.core.Response;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static se.inera.webcert.web.controller.api.dto.PersonuppgifterResponse.Status.FOUND;
import static se.inera.webcert.web.controller.api.dto.PersonuppgifterResponse.Status.NOT_FOUND;

@RunWith(MockitoJUnitRunner.class)
public class PersonApiControllerTest {

    @Mock
    private PUService puService;

    @InjectMocks
    private PersonApiController personCtrl = new PersonApiController();

    @Test
    public void testGetPersonuppgifter() {
        when(puService.getPerson(anyString())).thenReturn(new Person("fnamn", "enamn", "paddr", "pnr", "port"));

        Response response = personCtrl.getPersonuppgifter("19121212-1212");

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(FOUND, res.getStatus());
        assertEquals("fnamn", res.getPerson().getFornamn());
        assertEquals("enamn", res.getPerson().getEfternamn());
        assertEquals("paddr", res.getPerson().getPostadress());
        assertEquals("pnr", res.getPerson().getPostnummer());
        assertEquals("port", res.getPerson().getPostort());
    }

    @Test
    public void testGetPersonuppgifterMissingPerson() {

        Response response = personCtrl.getPersonuppgifter("18121212-1212");

        assertNotNull(response);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        PersonuppgifterResponse res = (PersonuppgifterResponse) response.getEntity();
        assertEquals(NOT_FOUND, res.getStatus());
        assertNull(res.getPerson());
    }
}
