package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.junit.Test;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by marced on 17/11/15.
 */
public class UtkastApiControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testGetFk7263Utkast() {

        // Set up auth precondition
        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        CreateUtkastRequest utkastRequest = createUtkastRequest("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        Response response = given().request().contentType(ContentType.JSON).body(utkastRequest).expect().statusCode(200).when().post("api/utkast/fk7263").
                then().
                body(matchesJsonSchemaInClasspath("jsonschema/webcert-generic-utkast-response-schema.json")).
                body("intygsTyp", equalTo(utkastRequest.getIntygType())).
                body("skapadAv.hsaId", equalTo(DEFAULT_LAKARE.getHsaId())).
                body("enhetsId", equalTo(DEFAULT_LAKARE.getEnhetId())).
                body("version", equalTo(0)).
                body("skapadAv.namn", equalTo(DEFAULT_LAKARE.getFornamn() + " " + DEFAULT_LAKARE.getEfternamn())).extract().response();

        //The type-specific model is a serialized json "within" the model property, need to extract that first and assert in a more "manual" fashion.
        JsonPath draft = new JsonPath(response.body().asString());
        JsonPath model = new JsonPath(draft.getString("model"));

        assertTrue(model.getString("id").length() > 0);

        assertEquals(utkastRequest.getPatientPersonnummer().getPersonnummer(), model.getString("grundData.patient.personId"));
        assertEquals(utkastRequest.getPatientFornamn(), model.getString("grundData.patient.fornamn"));
        assertEquals(utkastRequest.getPatientEfternamn(), model.getString("grundData.patient.efternamn"));

    }
}
