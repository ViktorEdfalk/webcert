package se.inera.webcert.web.controller.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.service.feature.WebcertFeature;
import se.inera.webcert.web.controller.AbstractApiController;

/**
 * Controller that logs messages from JavaScript to the normal log.
 */
@Path("/jslog")
public class JsLogApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(JsLogApiController.class);

    @POST
    @Path("/debug")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response debug(String message) {
        LOG.debug(message);
        return Response.ok().build();
    }
    
    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public Response test() {
        checkFeatureAvailable(WebcertFeature.MAKULERA_INTYG);
        return Response.ok().entity("This is the test method").build();
    }
}
