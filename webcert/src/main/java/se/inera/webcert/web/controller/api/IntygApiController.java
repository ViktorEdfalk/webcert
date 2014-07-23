package se.inera.webcert.web.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.webcert.converter.IntygDraftsConverter;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.repository.IntygFilter;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.draft.IntygDraftService;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.dto.IntygItem;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.dto.Patient;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.api.dto.CreateNewIntygRequest;
import se.inera.webcert.web.controller.api.dto.ListIntygEntry;
import se.inera.webcert.web.controller.api.dto.QueryIntygParameter;
import se.inera.webcert.web.controller.api.dto.QueryIntygResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for the API that serves WebCert.
 *
 * @author nikpet
 *
 */
@Path("/intyg")
public class IntygApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(IntygApiController.class);

    private static final List<IntygsStatus> ALL_DRAFTS = Arrays.asList(IntygsStatus.DRAFT_COMPLETE,
            IntygsStatus.DRAFT_INCOMPLETE);

    private static final List<IntygsStatus> COMPLETE_DRAFTS = Arrays.asList(IntygsStatus.DRAFT_COMPLETE);

    private static final List<IntygsStatus> INCOMPLETE_DRAFTS = Arrays.asList(IntygsStatus.DRAFT_INCOMPLETE);

    private static final Integer PAGE_SIZE = 10;

    @Autowired
    private IntygService intygService;

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private IntygDraftService intygDraftService;

    public IntygApiController() {

    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createNewDraft(CreateNewIntygRequest request) {

        if (!request.isValid()) {
            LOG.error("Request is invalid: " + request.toString());
            return Response.status(Status.BAD_REQUEST).build();
        }

        String intygType = request.getIntygType();

        LOG.debug("Attempting to create draft of type '{}'", intygType);

        CreateNewDraftRequest serviceRequest = createServiceRequest(request);

        String idOfCreatedDraft = intygDraftService.createNewDraft(serviceRequest);

        LOG.debug("Created a new draft of type '{}' with id '{}'", intygType, idOfCreatedDraft);

        return Response.ok().entity(idOfCreatedDraft).build();
    }

    @POST
    @Path("/kopiera/{intygsId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createNewCopy(CreateNewIntygRequest request, @PathParam("intygsId") String intygsId) {

        if (!request.isValid()) {
            LOG.error("Request is invalid: " + request.toString());
            return Response.status(Status.BAD_REQUEST).build();
        }

        String intygType = request.getIntygType();

        LOG.debug("Attempting to copy draft of type '{}' from {}", intygType, intygsId);

        CreateNewDraftRequest serviceRequest = createServiceRequest(request);

        String idOfCreatedDraft = intygDraftService.createNewDraftCopy(serviceRequest, intygsId);

        LOG.debug("Created a new draft copy of type '{}' with id '{}'", intygType, idOfCreatedDraft);

        return Response.ok().entity(idOfCreatedDraft).build();
    }

    private CreateNewDraftRequest createServiceRequest(CreateNewIntygRequest req) {
        CreateNewDraftRequest srvReq = new CreateNewDraftRequest();

        srvReq.setIntygType(req.getIntygType());

        Patient pat = new Patient();
        pat.setPersonnummer(req.getPatientPersonnummer());
        pat.setFornamn(req.getPatientFornamn());
        pat.setMellannamn(req.getPatientMellannamn());
        pat.setEfternamn(req.getPatientEfternamn());
        pat.setPostadress(req.getPatientPostadress());
        pat.setPostnummer(req.getPatientPostnummer());
        pat.setPostort(req.getPatientPostort());
        srvReq.setPatient(pat);

        srvReq.setHosPerson(createHoSPersonFromUser());
        srvReq.setVardenhet(createVardenhetFromUser());

        return srvReq;
    }

    /**
     * Compiles a list of Intyg from two data sources. Signed Intyg are
     * retrieved from Intygstjänst, drafts are retrieved from Webcerts db. Both
     * types of Intyg are converted and merged into one sorted list.
     *
     * @param personNummer personnummer
     * @return a Response carrying a list containing all Intyg for a person.
     */
    @GET
    @Path("/list/{personNummer}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response listDraftsAndIntygForPerson(@PathParam("personNummer") String personNummer) {

        LOG.debug("Retrieving intyg for person {}", personNummer);

        List<String> enhetsIds = getEnhetIdsForCurrentUser();

        if (enhetsIds.isEmpty()) {
            LOG.error("Current user has no assignments");
            return Response.status(Status.BAD_REQUEST).build();
        }

        List<IntygItem> signedIntygList = intygService.listIntyg(enhetsIds, personNummer);
        LOG.debug("Got {} signed intyg", signedIntygList.size());

        List<Intyg> draftIntygList = intygRepository.findDraftsByPatientAndEnhetAndStatus(personNummer, enhetsIds,
                ALL_DRAFTS);
        LOG.debug("Got {} draft intyg", draftIntygList.size());

        List<ListIntygEntry> allIntyg = IntygDraftsConverter.merge(signedIntygList, draftIntygList);

        return Response.ok(allIntyg).build();
    }

    @GET
    @Path("/unsigned")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response filterDraftsForUnit(@QueryParam("") QueryIntygParameter filterParameters) {

        IntygFilter intygFilter = createIntygFilter(filterParameters);
        QueryIntygResponse queryResponse = performIntygFilterQuery(intygFilter);

        return Response.ok(queryResponse).build();
    }

    private IntygFilter createIntygFilter(QueryIntygParameter filterParameters) {
        WebCertUser user = getWebCertUserService().getWebCertUser();
        String selectedUnitHsaId = user.getValdVardenhet().getId();

        IntygFilter intygFilter = new IntygFilter(selectedUnitHsaId);

        if (filterParameters != null) {
            if (Boolean.FALSE.equals(filterParameters.getComplete())) {
                intygFilter.setStatusList(INCOMPLETE_DRAFTS);
            } else if (Boolean.TRUE.equals(filterParameters.getComplete())) {
                intygFilter.setStatusList(COMPLETE_DRAFTS);
            } else {
                intygFilter.setStatusList(ALL_DRAFTS);
            }

            intygFilter.setSavedFrom(filterParameters.getSavedFrom());
            intygFilter.setSavedTo(filterParameters.getSavedTo());
            intygFilter.setSavedByHsaId(filterParameters.getSavedBy());
            intygFilter.setForwarded(filterParameters.getForwarded());
            intygFilter.setPageSize(filterParameters.getPageSize() == null ? PAGE_SIZE : filterParameters.getPageSize());
            intygFilter.setStartFrom(filterParameters.getStartFrom() == null ? 0 : filterParameters.getStartFrom());
        }

        return intygFilter;
    }

    private QueryIntygResponse performIntygFilterQuery(IntygFilter filter) {

        List<Intyg> intygList = intygRepository.filterIntyg(filter);

        List<ListIntygEntry> listIntygEntries = IntygDraftsConverter.convertIntygToListEntries(intygList);

        int totalCountOfFilteredIntyg = intygRepository.countFilterIntyg(filter);

        QueryIntygResponse response = new QueryIntygResponse(listIntygEntries);
        response.setTotalCount(totalCountOfFilteredIntyg);
        return response;
    }

    /**
     * Returns a list of doctors that have one or more unsigned intyg.
     *
     * @return a list of {@link se.inera.webcert.service.dto.Lakare} objects.
     */
    @GET
    @Path("/unsigned/lakare")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getLakareWithDraftsByEnheter() {

        WebCertUser user = getWebCertUserService().getWebCertUser();
        String selectedUnitHsaId = user.getValdVardenhet().getId();

        List<Lakare> lakareWithDraftsByEnhet = intygDraftService.getLakareWithDraftsByEnhet(selectedUnitHsaId);

        return Response.ok().entity(lakareWithDraftsByEnhet).build();
    }

    /**
     * Sets the forwarded flag on an Intyg.
     *
     * @param intygsId
     *            Id of the Intyg
     * @param forwarded
     *            True or False
     * @return
     *            Response
     */
    @PUT
    @Path("/forward/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setForwardOnIntyg(@PathParam("intygsId") String intygsId, Boolean forwarded) {

        Intyg updatedIntyg = intygDraftService.setForwardOnDraft(intygsId, forwarded);

        LOG.debug("Set forward to {} on intyg '{}'", updatedIntyg.getVidarebefordrad(), updatedIntyg.getIntygsId());

        ListIntygEntry intygEntry = IntygDraftsConverter.convertIntygToListIntygEntry(updatedIntyg);

        return Response.ok(intygEntry).build();
    }
}
