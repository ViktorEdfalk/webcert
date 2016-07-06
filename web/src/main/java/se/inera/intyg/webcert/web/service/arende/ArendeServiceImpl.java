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

package se.inera.intyg.webcert.web.service.arende;

import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.integration.hsa.services.HsaEmployeeService;
import se.inera.intyg.common.security.authorities.AuthoritiesHelper;
import se.inera.intyg.common.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.common.client.converter.SendMessageToRecipientTypeConverter;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Filter;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.ArendeListItemConverter;
import se.inera.intyg.webcert.web.converter.FilterConverter;
import se.inera.intyg.webcert.web.converter.util.ArendeViewConverter;
import se.inera.intyg.webcert.web.integration.builder.SendMessageToRecipientTypeBuilder;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderException;
import se.inera.intyg.webcert.web.service.certificatesender.CertificateSenderService;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.fragasvar.dto.*;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationEvent;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.*;
import se.inera.intyg.webcert.web.web.controller.api.dto.ArendeView.ArendeType;
import se.inera.intyg.webcert.web.web.controller.util.CertificateTypes;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToRecipient.v1.SendMessageToRecipientType;

@Service
@Transactional("jpaTransactionManager")
public class ArendeServiceImpl implements ArendeService {

    private static final Logger LOG = LoggerFactory.getLogger(ArendeServiceImpl.class);

    private static final List<String> BLACKLISTED = Arrays.asList(CertificateTypes.FK7263.toString(), CertificateTypes.TSBAS.toString(),
            CertificateTypes.TSDIABETES.toString());

    private static final List<ArendeAmne> VALID_VARD_AMNEN = Arrays.asList(
            ArendeAmne.ARBTID,
            ArendeAmne.AVSTMN,
            ArendeAmne.KONTKT,
            ArendeAmne.OVRIGT);

    @Value("${sendmessagetofk.logicaladdress}")
    private String sendMessageToFKLogicalAddress;

    @Autowired
    private ArendeRepository arendeRepository;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private WebCertUserService webcertUserService;

    @Autowired
    private AuthoritiesHelper authoritiesHelper;

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private ArendeViewConverter arendeViewConverter;

    @Autowired
    private HsaEmployeeService hsaEmployeeService;

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CertificateSenderService certificateSenderService;

    @Override
    public Arende processIncomingMessage(Arende arende) throws WebCertServiceException {
        if (arendeRepository.findOneByMeddelandeId(arende.getMeddelandeId()) != null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "meddelandeId not unique");
        }

        Utkast utkast = utkastRepository.findOne(arende.getIntygsId());

        validateArende(arende.getIntygsId(), utkast);

        LocalDateTime now = LocalDateTime.now();
        decorateArende(arende, utkast, now);

        updateRelated(arende, now);

        monitoringLog.logArendeReceived(arende.getIntygsId(), utkast.getIntygsTyp(), utkast.getEnhetsId(), arende.getRubrik());

        Arende saved = arendeRepository.save(arende);

        if (ArendeAmne.PAMINN == saved.getAmne()) {
            notificationService.sendNotificationForQuestionReceived(saved);
        } else if (saved.getSvarPaId() != null) {
            notificationService.sendNotificationForAnswerRecieved(saved);
        } else {
            notificationService.sendNotificationForQuestionReceived(saved);
        }
        return saved;
    }

    @Override
    public ArendeConversationView createMessage(String intygId, ArendeAmne amne, String rubrik, String meddelande) throws WebCertServiceException {
        if (!VALID_VARD_AMNEN.contains(amne)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Invalid Amne " + amne
                    + " for new question from vard!");
        }
        Utkast utkast = utkastRepository.findOne(intygId);

        validateArende(intygId, utkast);

        verifyEnhetsAuth(utkast.getEnhetsId(), false);

        Arende arende = buildArendeQuestionFromUtkast(amne, rubrik, meddelande, utkast);

        Arende saved = processOutgoingMessage(arende);

        sendNotification(saved, NotificationEvent.QUESTION_SENT_TO_FK);

        ArendeView arendeView = arendeViewConverter.convert(saved);

        return ArendeConversationView.create(arendeView, null, saved.getSenasteHandelse(), new ArrayList<>());
    }

    @Override
    public ArendeConversationView answer(String svarPaMeddelandeId, String meddelande) {
        if (StringUtils.isEmpty(meddelande)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "SvarsText cannot be empty!");
        }
        Arende svarPaMeddelande = lookupArende(svarPaMeddelandeId);

        verifyEnhetsAuth(svarPaMeddelande.getEnhetId(), false);

        if (!Status.PENDING_INTERNAL_ACTION.equals(svarPaMeddelande.getStatus())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Arende with id "
                    + svarPaMeddelandeId + " has invalid state for saving answer("
                    + svarPaMeddelande.getStatus() + ")");
        }

        // Implement Business Rule FS-007
        if (ArendeAmne.PAMINN.equals(svarPaMeddelande.getAmne())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Arende with id "
                    + svarPaMeddelandeId + " has invalid Amne(" + svarPaMeddelande.getAmne()
                    + ") for saving answer");
        }

        // Implement Business Rule FS-005, FS-006
        WebCertUser user = webcertUserService.getUser();
        AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        if (ArendeAmne.KOMPLT.equals(svarPaMeddelande.getAmne())
                && !authoritiesValidator.given(user).privilege(AuthoritiesConstants.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA).isVerified()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM, "Arende with id "
                    + svarPaMeddelandeId + " and amne (" + svarPaMeddelande.getAmne()
                    + ") can only be answered by user that is Lakare");
        }
        Arende arende = buildArendeAnswerFromQuestion(meddelande, svarPaMeddelande);

        Arende saved = processOutgoingMessage(arende);

        sendNotification(saved, NotificationEvent.ANSWER_SENT_TO_FK);

        ArendeView arendeViewQuestion = arendeViewConverter.convert(svarPaMeddelande);
        ArendeView arendeViewAnswer = arendeViewConverter.convert(saved);
        List<ArendeView> arendeViewPaminnelser = getPaminnelser(svarPaMeddelandeId);

        return ArendeConversationView.create(arendeViewQuestion, arendeViewAnswer, svarPaMeddelande.getSenasteHandelse(), arendeViewPaminnelser);
    }

    @Override
    public ArendeConversationView setForwarded(String meddelandeId, boolean vidarebefordrad) {
        Arende arende = lookupArende(meddelandeId);
        arende.setVidarebefordrad(vidarebefordrad);

        Arende updatedArende = arendeRepository.save(arende);

        ArendeView arendeViewQuestion = arendeViewConverter.convert(updatedArende);
        ArendeView arendeViewAnswer = null;
        List<Arende> svar = arendeRepository.findBySvarPaId(meddelandeId);
        if (CollectionUtils.isNotEmpty(svar)) {
            arendeViewAnswer = arendeViewConverter.convert(svar.get(0));
        }
        List<ArendeView> arendeViewPaminnelser = getPaminnelser(meddelandeId);

        return ArendeConversationView.create(arendeViewQuestion, arendeViewAnswer, updatedArende.getSenasteHandelse(), arendeViewPaminnelser);
    }

    @Override
    public ArendeConversationView openArendeAsUnhandled(String meddelandeId) {
        Arende arende = lookupArende(meddelandeId);
        boolean arendeIsAnswered = !arendeRepository.findBySvarPaId(meddelandeId).isEmpty();

        // Enforce business rule FS-011, from FK + answer should remain closed
        if (!FrageStallare.WEBCERT.isKodEqual(arende.getSkickatAv())
                && arendeIsAnswered) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "FS-011: Cant revert status for question " + meddelandeId);
        }

        NotificationEvent notificationEvent = determineNotificationEvent(arende, arendeIsAnswered);

        if (arendeIsAnswered) {
            arende.setStatus(Status.ANSWERED);
        } else {
            if (FrageStallare.WEBCERT.isKodEqual(arende.getSkickatAv())) {
                arende.setStatus(Status.PENDING_EXTERNAL_ACTION);
            } else {
                arende.setStatus(Status.PENDING_INTERNAL_ACTION);
            }
        }
        Arende openedArende = arendeRepository.save(arende);

        if (notificationEvent != null) {
            sendNotification(openedArende, notificationEvent);
        }

        ArendeView arendeViewQuestion = arendeViewConverter.convert(openedArende);
        ArendeView arendeViewAnswer = null;
        List<Arende> svar = arendeRepository.findBySvarPaId(meddelandeId);
        if (CollectionUtils.isNotEmpty(svar)) {
            arendeViewAnswer = arendeViewConverter.convert(svar.get(0));
        }
        List<ArendeView> arendeViewPaminnelser = getPaminnelser(meddelandeId);

        return ArendeConversationView.create(arendeViewQuestion, arendeViewAnswer, openedArende.getSenasteHandelse(), arendeViewPaminnelser);
    }

    @Override
    public List<Lakare> listSignedByForUnits(String enhetsId) throws WebCertServiceException {

        List<String> enhetsIdParams = new ArrayList<>();
        if (enhetsId != null) {
            verifyEnhetsAuth(enhetsId, true);
            enhetsIdParams.add(enhetsId);
        } else {
            enhetsIdParams.addAll(webcertUserService.getUser().getIdsOfSelectedVardenhet());
        }

        List<Lakare> arendeList = arendeRepository.findSigneratAvByEnhet(enhetsIdParams).stream()
                .map(arr -> new Lakare((String) arr[0], (String) arr[1]))
                .collect(Collectors.toList());

        // We need to maintain backwards compatibility. When FragaSvar no longer exist remove this part and return above
        // arendeList
        List<Lakare> fragaSvarList = fragaSvarService.getFragaSvarHsaIdByEnhet(enhetsId);
        return Lakare.merge(arendeList, fragaSvarList);
    }

    @Override
    public List<Arende> listArendeForUnits() throws WebCertServiceException {
        WebCertUser user = webcertUserService.getUser();
        List<String> unitIds = user.getIdsOfSelectedVardenhet();

        return arendeRepository.findByEnhet(unitIds);
    }

    @Override
    public List<ArendeConversationView> getArenden(String intygsId) {
        List<Arende> arendeList = arendeRepository.findByIntygsId(intygsId);

        WebCertUser user = webcertUserService.getUser();
        List<String> hsaEnhetIds = user.getIdsOfSelectedVardenhet();

        Iterator<Arende> iterator = arendeList.iterator();
        while (iterator.hasNext()) {
            Arende arende = iterator.next();
            if (arende.getEnhetId() != null && !hsaEnhetIds.contains(arende.getEnhetId())) {
                arendeList.remove(arende);
            }
        }
        List<ArendeView> arendeViews = new ArrayList<>();
        for (Arende arende : arendeList) {
            ArendeView latestDraft = arendeViewConverter.convert(arende);
            arendeViews.add(latestDraft);
        }
        List<ArendeConversationView> arendeConversations = buildArendeConversations(arendeViews);
        Collections.sort(arendeConversations, (a, b) -> {
            boolean aIsEmpty = a.getPaminnelser().isEmpty();
            boolean bIsEmpty = b.getPaminnelser().isEmpty();
            if (aIsEmpty == bIsEmpty) {
                return b.getSenasteHandelse().compareTo(a.getSenasteHandelse());
            } else if (aIsEmpty) {
                return 1;
            } else {
                return -1;
            }
        });

        return arendeConversations;
    }

    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public QueryFragaSvarResponse filterArende(QueryFragaSvarParameter filterParameters) {

        WebCertUser user = webcertUserService.getUser();
        Set<String> intygstyperForPrivilege = authoritiesHelper.getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);

        Filter filter;
        if (StringUtils.isNotEmpty(filterParameters.getEnhetId())) {
            verifyEnhetsAuth(filterParameters.getEnhetId(), true);
            filter = FilterConverter.convert(filterParameters, Arrays.asList(filterParameters.getEnhetId()), intygstyperForPrivilege);
        } else {
            filter = FilterConverter.convert(filterParameters, user.getIdsOfSelectedVardenhet(), intygstyperForPrivilege);
        }

        int originalStartFrom = filter.getStartFrom();
        int originalPageSize = filter.getPageSize();

        // update page size and start from to be able to merge FragaSvar and Arende properly
        filter.setStartFrom(Integer.valueOf(0));
        filter.setPageSize(originalPageSize + originalStartFrom);

        List<ArendeListItem> results = arendeRepository.filterArende(filter).stream()
                .map(ArendeListItemConverter::convert)
                .filter(Objects::nonNull)
                // We need to decorate the ArendeListItem with information whether there exist a reminder or not because
                // they want to display this information to the user. We cannot do this without a database access, hence
                // we do it after the convert
                .map(item -> {
                    item.setPaminnelse(!arendeRepository.findByPaminnelseMeddelandeId(item.getMeddelandeId()).isEmpty());
                    return item;
                })
                .collect(Collectors.toList());
        QueryFragaSvarResponse fsResults = fragaSvarService.filterFragaSvar(filter);

        int totalResultsCount = arendeRepository.filterArendeCount(filter) + fsResults.getTotalCount();

        results.addAll(fsResults.getResults());
        results.sort(Comparator.comparing(ArendeListItem::getReceivedDate).reversed());

        QueryFragaSvarResponse response = new QueryFragaSvarResponse();
        if (originalStartFrom >= results.size()) {
            response.setResults(new ArrayList<>());
        } else {
            response.setResults(results.subList(originalStartFrom, Math.min(originalPageSize + originalStartFrom, results.size())));
        }
        response.setTotalCount(totalResultsCount);

        return response;
    }

    @Override
    @Transactional
    public ArendeConversationView closeArendeAsHandled(String meddelandeId) {
        Arende arendeToClose = lookupArende(meddelandeId);
        NotificationEvent notificationEvent = determineNotificationEvent(arendeToClose, false);
        Arende closedArende = closeArendeAsHandled(arendeToClose);

        if (notificationEvent != null) {
            sendNotification(closedArende, notificationEvent);
        }

        ArendeView arendeViewQuestion = arendeViewConverter.convert(closedArende);
        ArendeView arendeViewAnswer = null;
        List<Arende> svar = arendeRepository.findBySvarPaId(meddelandeId);
        if (CollectionUtils.isNotEmpty(svar)) {
            arendeViewAnswer = arendeViewConverter.convert(svar.get(0));
        }
        List<ArendeView> arendeViewPaminnelser = getPaminnelser(meddelandeId);

        return ArendeConversationView.create(arendeViewQuestion, arendeViewAnswer, closedArende.getSenasteHandelse(), arendeViewPaminnelser);
    }

    @Override
    public Arende getArende(String meddelandeId) {
        return arendeRepository.findOneByMeddelandeId(meddelandeId);
    }

    @Override
    public Map<String, Long> getNbrOfUnhandledArendenForCareUnits(List<String> vardenheterIds, Set<String> intygsTyper) {
        Map<String, Long> resultsMap = new HashMap<>();

        if ((vardenheterIds == null) || vardenheterIds.isEmpty()) {
            LOG.warn("No ids for Vardenheter was supplied");
            return resultsMap;
        }

        if ((intygsTyper == null) || intygsTyper.isEmpty()) {
            LOG.warn("No intygsTyper for querying Arenden was supplied");
            return resultsMap;
        }

        List<Object[]> results = arendeRepository.countUnhandledGroupedByEnhetIdsAndIntygstyper(vardenheterIds, intygsTyper);

        for (Object[] resArr : results) {
            String id = (String) resArr[0];
            Long nbr = (Long) resArr[1];
            resultsMap.put(id, nbr);
        }

        return resultsMap;
    }

    protected void verifyEnhetsAuth(String enhetsId, boolean isReadOnlyOperation) {
        if (!webcertUserService.isAuthorizedForUnit(enhetsId, isReadOnlyOperation)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for enhet " + enhetsId);
        }
    }

    private NotificationEvent determineNotificationEvent(Arende arende, boolean arendeIsAnswered) {
        FrageStallare frageStallare = FrageStallare.getByKod(arende.getSkickatAv());
        Status arendeSvarStatus = arende.getStatus();

        if (FrageStallare.FORSAKRINGSKASSAN.equals(frageStallare)) {
            if (Status.PENDING_INTERNAL_ACTION.equals(arendeSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_FK_HANDLED;
            } else if (Status.CLOSED.equals(arendeSvarStatus)) {
                return NotificationEvent.QUESTION_FROM_FK_UNHANDLED;
            }
        }

        if (FrageStallare.WEBCERT.equals(frageStallare)) {
            if (Status.ANSWERED.equals(arendeSvarStatus)) {
                return NotificationEvent.ANSWER_FROM_FK_HANDLED;
            } else if (Status.CLOSED.equals(arendeSvarStatus) && arendeIsAnswered) {
                return NotificationEvent.ANSWER_FROM_FK_UNHANDLED;
            }
        }

        return null;
    }

    private void sendNotification(Arende arende, NotificationEvent event) {
        switch (event) {
        case ANSWER_FROM_FK_HANDLED:
            notificationService.sendNotificationForAnswerHandled(arende);
            break;
        case ANSWER_FROM_FK_UNHANDLED:
            notificationService.sendNotificationForAnswerHandled(arende);
            break;
        case ANSWER_SENT_TO_FK:
            notificationService.sendNotificationForQuestionHandled(arende);
            break;
        case QUESTION_FROM_FK_HANDLED:
            notificationService.sendNotificationForQuestionHandled(arende);
            break;
        case QUESTION_FROM_FK_UNHANDLED:
            notificationService.sendNotificationForQuestionHandled(arende);
            break;
        case QUESTION_SENT_TO_FK:
            notificationService.sendNotificationForQuestionSent(arende);
            break;
        default:
            LOG.warn("ArendeServiceImpl.sendNotification(Arende, NotificationEvent) - cannot send notification. Incoming event not handled!");
        }
    }

    private Arende closeArendeAsHandled(Arende arende) {
        arende.setStatus(Status.CLOSED);
        return arendeRepository.save(arende);
    }

    private Arende lookupArende(String meddelandeId) {
        Arende arende = arendeRepository.findOneByMeddelandeId(meddelandeId);
        if (arende == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "Could not find Arende with id:" + meddelandeId);
        }
        return arende;
    }

    private void validateArende(String arendeIntygsId, Utkast utkast) {
        if (utkast == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "Certificate " + arendeIntygsId + " not found.");
        } else if (utkast.getSignatur() == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Certificate " + arendeIntygsId + " not signed.");
        } else if (BLACKLISTED.contains(utkast.getIntygsTyp())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Certificate " + arendeIntygsId + " has wrong type. " + utkast.getIntygsTyp() + " is blacklisted.");
        }
    }

    private Arende processOutgoingMessage(Arende arende) throws WebCertServiceException {
        Arende saved = arendeRepository.save(arende);
        monitoringLog.logArendeCreated(arende.getIntygsId(), arende.getIntygTyp(), arende.getEnhetId(), arende.getRubrik());

        updateRelated(arende, arende.getSenasteHandelse());

        SendMessageToRecipientType request = SendMessageToRecipientTypeBuilder.build(arende, webcertUserService.getUser(),
                sendMessageToFKLogicalAddress);

        // Send to recipient
        try {
            certificateSenderService.sendMessageToRecipient(arende.getIntygsId(), SendMessageToRecipientTypeConverter.toXml(request));
        } catch (JAXBException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getMessage());
        } catch (CertificateSenderException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getMessage());
        }

        return saved;
    }

    private List<ArendeView> getPaminnelser(String meddelandeId) {
        List<ArendeView> arendeViewPaminnelser = new ArrayList<>();

        List<Arende> paminnelser = arendeRepository.findByPaminnelseMeddelandeId(meddelandeId);
        for (Arende paminnelse : paminnelser) {
            arendeViewPaminnelser.add(arendeViewConverter.convert(paminnelse));
        }
        Collections.sort(arendeViewPaminnelser, Comparator.comparing(ArendeView::getTimestamp).reversed());
        return arendeViewPaminnelser;
    }

    private List<ArendeConversationView> buildArendeConversations(List<ArendeView> arendeViews) {
        List<ArendeConversationView> arendeConversations = new ArrayList<>();
        Map<String, List<ArendeView>> threads = new HashMap<>();
        String meddelandeId = null;
        for (ArendeView arende : arendeViews) { // divide into threads
            meddelandeId = getMeddelandeId(arende);
            if (threads.get(meddelandeId) == null) {
                threads.put(meddelandeId, new ArrayList<ArendeView>());
            }
            threads.get(meddelandeId).add(arende);
        }

        for (String meddelandeIdd : threads.keySet()) {
            List<ArendeView> arendeConversationContent = threads.get(meddelandeIdd);
            List<ArendeView> paminnelser = new ArrayList<>();
            ArendeView fraga = null, svar = null;
            LocalDateTime senasteHandelse = null;
            for (ArendeView view : arendeConversationContent) {
                if (view.getArendeType() == ArendeType.FRAGA) {
                    fraga = view;
                } else if (view.getArendeType() == ArendeType.SVAR) {
                    svar = view;
                } else {
                    paminnelser.add(view);
                }
                if (senasteHandelse == null || senasteHandelse.isBefore(view.getTimestamp())) {
                    senasteHandelse = view.getTimestamp();
                }
            }

            Collections.sort(paminnelser, Comparator.comparing(ArendeView::getTimestamp).reversed());

            // Since fraga is required to be nonNull by AutoValue_ArendeConversationView need to make sure this is
            // enforced to avoid throwing an exception and showing nothing at all
            if (fraga != null) {
                arendeConversations.add(ArendeConversationView.create(fraga, svar, senasteHandelse, paminnelser));
            }
        }
        return arendeConversations;
    }

    private String getMeddelandeId(ArendeView arende) {
        String referenceId = (arende.getSvarPaId() != null) ? arende.getSvarPaId() : arende.getPaminnelseMeddelandeId();
        String meddelandeId = (referenceId != null) ? referenceId : arende.getInternReferens();
        return meddelandeId;
    }

    private void updateRelated(Arende arende, LocalDateTime dateTime) {
        Arende orig;
        if (arende.getSvarPaId() != null) {
            orig = arendeRepository.findOneByMeddelandeId(arende.getSvarPaId());
            if (orig != null) {
                orig.setSenasteHandelse(dateTime);
                orig.setStatus(arende.getStatus());
                arendeRepository.save(orig);
            }
        } else if (arende.getPaminnelseMeddelandeId() != null) {
            orig = arendeRepository.findOneByMeddelandeId(arende.getPaminnelseMeddelandeId());
            if (orig != null) {
                orig.setSenasteHandelse(dateTime);
                arendeRepository.save(orig);
            }
        }
    }

    private void decorateArende(Arende arende, Utkast utkast, LocalDateTime now) {
        arende.setTimestamp(now);
        arende.setSenasteHandelse(now);
        arende.setStatus(arende.getSvarPaId() == null ? Status.PENDING_INTERNAL_ACTION : Status.ANSWERED);
        arende.setVidarebefordrad(Boolean.FALSE);

        arende.setIntygTyp(utkast.getIntygsTyp());
        arende.setSigneratAv(utkast.getSignatur().getSigneradAv());
        arende.setSigneratAvName(getSignedByName(utkast));
        arende.setEnhetId(utkast.getEnhetsId());
        arende.setEnhetName(utkast.getEnhetsNamn());
        arende.setVardgivareName(utkast.getVardgivarNamn());
    }

    private Arende buildArendeQuestionFromUtkast(ArendeAmne amne, String rubrik, String meddelande, Utkast utkast) {
        Arende arende = new Arende();
        arende.setStatus(Status.PENDING_EXTERNAL_ACTION);
        arende.setAmne(amne);
        arende.setEnhetId(utkast.getEnhetsId());
        arende.setEnhetName(utkast.getEnhetsNamn());
        arende.setVardgivareName(utkast.getVardgivarNamn());
        arende.setIntygsId(utkast.getIntygsId());
        arende.setIntygTyp(utkast.getIntygsTyp());
        arende.setMeddelande(meddelande);
        arende.setPatientPersonId(utkast.getPatientPersonnummer().getPersonnummerWithoutDash());
        arende.setRubrik(rubrik);
        arende.setSigneratAv(utkast.getSignatur().getSigneradAv());
        arende.setSigneratAvName(getSignedByName(utkast));
        decorateNewArende(arende, LocalDateTime.now());
        return arende;
    }

    private Arende buildArendeAnswerFromQuestion(String meddelande, Arende svarPaMeddelande) {
        Arende arende = new Arende();
        arende.setStatus(Status.CLOSED);
        arende.setSvarPaId(svarPaMeddelande.getMeddelandeId());
        arende.setSvarPaReferens(svarPaMeddelande.getReferensId());
        arende.setAmne(svarPaMeddelande.getAmne());
        arende.setEnhetId(svarPaMeddelande.getEnhetId());
        arende.setEnhetName(svarPaMeddelande.getEnhetName());
        arende.setVardgivareName(svarPaMeddelande.getVardgivareName());
        arende.setIntygsId(svarPaMeddelande.getIntygsId());
        arende.setIntygTyp(svarPaMeddelande.getIntygTyp());
        arende.setMeddelande(meddelande);
        arende.setPatientPersonId(svarPaMeddelande.getPatientPersonId());
        arende.setRubrik(svarPaMeddelande.getRubrik());
        arende.setSigneratAv(svarPaMeddelande.getSigneratAv());
        arende.setSigneratAvName(svarPaMeddelande.getSigneratAvName());
        decorateNewArende(arende, LocalDateTime.now());
        return arende;
    }

    private void decorateNewArende(Arende arende, LocalDateTime now) {
        arende.setMeddelandeId(UUID.randomUUID().toString());
        arende.setSkickatAv(FrageStallare.WEBCERT.getKod());
        arende.setVidarebefordrad(Boolean.FALSE);
        arende.setSenasteHandelse(now);
        arende.setSkickatTidpunkt(now);
        arende.setTimestamp(now);
        arende.setVardaktorName(webcertUserService.getUser().getNamn());
    }

    // If we already have the signer's name in the information in the certificate we use this. This information could be
    // either in skapadAv or senastSparadAv. If neither of those matches the signer of the certificate we ask HSA.
    private String getSignedByName(Utkast utkast) {
        if (utkast.getSkapadAv() != null && utkast.getSkapadAv().getHsaId().equals(utkast.getSignatur().getSigneradAv())) {
            return utkast.getSkapadAv().getNamn();
        } else if (utkast.getSenastSparadAv() != null && utkast.getSenastSparadAv().getHsaId().equals(utkast.getSignatur().getSigneradAv())) {
            return utkast.getSenastSparadAv().getNamn();
        } else {
            try {
                return hsaEmployeeService.getEmployee(utkast.getSignatur().getSigneradAv(), null)
                        .stream()
                        .filter(pit -> StringUtils.isNotEmpty(pit.getMiddleAndSurName()))
                        .map(pit -> StringUtils.isNotEmpty(pit.getGivenName())
                                ? pit.getGivenName() + " " + pit.getMiddleAndSurName()
                                : pit.getMiddleAndSurName())
                        .findFirst()
                        .orElseThrow(() -> new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "No name was found in HSA"));
            } catch (WebServiceException e) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.EXTERNAL_SYSTEM_PROBLEM,
                        "Could not communicate with HSA. Cause: " + e.getMessage());
            }
        }
    }

}
