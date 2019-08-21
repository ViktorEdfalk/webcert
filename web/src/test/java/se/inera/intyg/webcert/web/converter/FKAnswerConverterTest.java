/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.converter;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.JAXBElement;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import se.inera.ifv.insuranceprocess.healthreporting.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.ObjectFactory;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * This test makes use of Equals and HashCode from JAXB basics. All types must implement
 * this.
 */
public class FKAnswerConverterTest {

    private FragaSvarConverter fragaSvarConverter = new FragaSvarConverter();

    private QuestionFromFkType inflateQuestionFromFK() throws Exception {
        ClassPathResource resource = new ClassPathResource("FragaSvarConverterTest/question.xml");
        JAXBElement<QuestionFromFkType> jaxbElement = XmlMarshallerHelper.unmarshal(resource.getInputStream());
        return jaxbElement.getValue();
    }

    private AnswerToFkType inflateAnswerToFK() throws Exception {
        ClassPathResource resource = new ClassPathResource("FragaSvarConverterTest/answer_to_fk.xml");
        JAXBElement<AnswerToFkType> jaxbElement = XmlMarshallerHelper.unmarshal(resource.getInputStream());
        return jaxbElement.getValue();
    }

    private String jaxbToXml(AnswerToFkType object) {
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<AnswerToFkType> jaxbElement = objectFactory.createAnswer(object);
        return XmlMarshallerHelper.marshal(jaxbElement);
    }

    @Test
    public void testConvertAnswer() throws Exception {
        QuestionFromFkType questionFromFK = inflateQuestionFromFK();
        AnswerToFkType referenceAnswerToFK = inflateAnswerToFK();

        // convert QuestionFromFK to FragaSvar entity
        FragaSvar fragaSvar = fragaSvarConverter.convert(questionFromFK);

        // add some data
        fragaSvar.setInternReferens(321L);
        fragaSvar.setSvarsText(fragaSvar.getFrageText());
        fragaSvar.setSvarSigneringsDatum(fragaSvar.getFrageSigneringsDatum());

        // convert FragaSvar entity to AnswerToFK
        AnswerToFkType convertedAnswerToFK = FKAnswerConverter.convert(fragaSvar);

        // compare convertedAnswerToFK to reference
        String expected = jaxbToXml(referenceAnswerToFK);
        String actual = jaxbToXml(convertedAnswerToFK);
        assertEquals(expected, actual);
    }

}
