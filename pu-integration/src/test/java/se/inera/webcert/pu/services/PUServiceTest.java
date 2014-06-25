package se.inera.webcert.pu.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.webcert.pu.model.Person;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:PUServiceTest/test-context.xml")
public class PUServiceTest {

    @Autowired
    private PUService service;

    @Test
    public void checkExistingPersonWithFullAddress() {
        Person person = service.getPerson("19121212-1212");
        assertEquals("Tolvan", person.getFornamn());
        assertEquals("Tolvansson", person.getEfternamn());
        assertEquals("C/O\nrad1\nrad2", person.getPostadress());
        assertEquals("12345", person.getPostnummer());
        assertEquals("Småmåla", person.getPostort());
    }

    @Test
    public void checkExistingPersonWithMinimalAddress() {
        Person person = service.getPerson("20121212-1212");
        assertEquals("Lilltolvan", person.getFornamn());
        assertEquals("Tolvansson", person.getEfternamn());
        assertEquals("rad1", person.getPostadress());
        assertEquals("12345", person.getPostnummer());
        assertEquals("Småmåla", person.getPostort());
    }
}