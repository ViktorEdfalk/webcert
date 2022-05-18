/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
 *
 */

package se.inera.intyg.webcert.web.service.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

@Service
public class FHIRConditionServices {

    protected WebCertUserService webCertUserService;

    @Autowired
    public void setWebCertUserService(WebCertUserService webCertUserService) {
        this.webCertUserService = webCertUserService;
    }

    private String splitCharacter = ":";

    public List<String> getDiagnosisForPatient(String patientId) {
        FhirContext ctx = FhirContext.forR4();
        String serverBase = getWebCertUserService().getUser().getParameters().getFhirApi();
        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        Bundle bundle = client
            .search()
            .forResource(Condition.class)
            .where(Condition.PATIENT.hasId(patientId))
            .returnBundle(Bundle.class)
            .execute();

        var entries = bundle.getEntry();
        return createDiagnosListFromEntries(entries);
    }

    private List<String> createDiagnosListFromEntries(List<BundleEntryComponent> entries) {
        List<String> diagnosis = new ArrayList<>();

        for (var entry : entries) {
            var resource = (Condition) entry.getResource();
            var code = resource.getCode();
            var coding = code.getCoding().get(0);
            var diagnos = coding.getCode() + splitCharacter + coding.getDisplay() + splitCharacter + "ICD_10_SE";

            diagnosis.add(diagnos);
        }

        return diagnosis;
    }

    protected WebCertUserService getWebCertUserService() {
        return webCertUserService;
    }
}
