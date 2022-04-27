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
 */

package se.inera.intyg.webcert.web.service.facade.list;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.UserService;
import se.inera.intyg.webcert.web.service.facade.impl.ResourceLinkFactory;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.dto.*;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CertificateListItemConverterImpl implements CertificateListItemConverter {

    private final CertificateAccessServiceHelper certificateAccessServiceHelper;
    private final WebCertUserService webCertUserService;
    private final UserService userService;
    private final HsaOrganizationsService hsaOrganizationsService;

    @Autowired
    public CertificateListItemConverterImpl(CertificateAccessServiceHelper certificateAccessServiceHelper,
                                            WebCertUserService webCertUserService, UserService userService, HsaOrganizationsService hsaOrganizationsService) {
        this.certificateAccessServiceHelper = certificateAccessServiceHelper;
        this.webCertUserService = webCertUserService;
        this.userService = userService;
        this.hsaOrganizationsService = hsaOrganizationsService;
    }

    @Override
    public CertificateListItem convert(ListIntygEntry listIntygEntry, ListType listType) {
        return convertListItem(listIntygEntry, listType);
    }

    @Override
    public CertificateListItem convert(CertificateListEntry entry) {
        return convertListItem(entry);
    }

    private CertificateListItem convertListItem(ListIntygEntry listIntygEntry, ListType listType) {
        final var listItem = new CertificateListItem();
        final var convertedStatus = convertStatus(listIntygEntry.getStatus());
        final var patientListInfo = getPatientListInfo(listIntygEntry);

        listItem.addValue(ListColumnType.CERTIFICATE_TYPE_NAME, listIntygEntry.getIntygTypeName());
        listItem.addValue(ListColumnType.STATUS, convertedStatus);
        listItem.addValue(ListColumnType.SAVED, listIntygEntry.getLastUpdatedSigned());
        listItem.addValue(ListColumnType.PATIENT_ID, patientListInfo);
        listItem.addValue(listType == ListType.DRAFTS ? ListColumnType.SAVED_BY : ListColumnType.SAVED_SIGNED_BY, listIntygEntry.getUpdatedSignedBy());
        listItem.addValue(ListColumnType.FORWARDED, getForwardedListInfo(listIntygEntry));
        listItem.addValue(ListColumnType.CERTIFICATE_ID, listIntygEntry.getIntygId());
        listItem.addValue(ListColumnType.LINKS, convertResourceLinks(
                listIntygEntry.getLinks(), listIntygEntry.getVardenhetId(), listIntygEntry.getIntygType(), listIntygEntry.getStatus())
        );
        return listItem;
    }

    private CertificateListItem convertListItem(CertificateListEntry entry) {
        final var listItem = new CertificateListItem();
        final var patientListInfo = getPatientListInfo(entry);
        listItem.addValue(ListColumnType.CERTIFICATE_TYPE_NAME, entry.getCertificateTypeName());
        listItem.addValue(ListColumnType.SIGNED, entry.getSignedDate());
        listItem.addValue(ListColumnType.PATIENT_ID, patientListInfo);
        listItem.addValue(ListColumnType.CERTIFICATE_ID, entry.getCertificateId());
        listItem.addValue(ListColumnType.STATUS, entry.isSent()
                ? CertificateStatus.SENT.getName() : CertificateStatus.NOT_SENT.getName()
        );
        listItem.addValue(ListColumnType.LINKS, convertResourceLinks(
                getResourceLinks(entry), "", entry.getCertificateType(), (String) listItem.getValue(ListColumnType.STATUS)
        ));
        return listItem;
    }

    private String convertStatus(String status) {
        final var convertedStatus = getCertificateStatus(status);
        return convertedStatus != null ? convertedStatus.getName() : "Okänd";
    }

    private CertificateStatus getCertificateStatus(String status) {
        if (status.equals(UtkastStatus.DRAFT_COMPLETE.toString())) {
            return CertificateStatus.COMPLETE;
        } else if (status.equals(UtkastStatus.DRAFT_INCOMPLETE.toString())) {
            return CertificateStatus.INCOMPLETE;
        } else if (status.equals(UtkastStatus.DRAFT_LOCKED.toString())) {
            return CertificateStatus.LOCKED;
        } else if (status.equals("RENEWED")) {
            return CertificateStatus.RENEWED;
        } else if (status.equals("COMPLEMENTED")) {
            return CertificateStatus.COMPLEMENTED;
        } else if (status.equals("CANCELLED")) {
            return CertificateStatus.REVOKED;
        } else if (status.equals("SENT")  || status.equals("RECEIVED")) {
            return CertificateStatus.SENT;
        } else if (status.equals("SIGNED")) {
            return CertificateStatus.SIGNED;
        }
        return null;
    }

    private PatientListInfo getPatientListInfo(ListIntygEntry listIntygEntry) {
        return new PatientListInfo(
                listIntygEntry.getPatientId().getPersonnummerWithDash(),
                listIntygEntry.isSekretessmarkering(),
                listIntygEntry.isAvliden(),
                listIntygEntry.isTestIntyg()
        );
    }

    private PatientListInfo getPatientListInfo(CertificateListEntry certificateListEntry) {
        return new PatientListInfo(
                certificateListEntry.getCivicRegistrationNumber(),
                certificateListEntry.isProtectedIdentity(),
                certificateListEntry.isDeceased(),
                certificateListEntry.isTestIndicator()
        );
    }

    private List<ActionLink> getResourceLinks(CertificateListEntry entry) {
        final var user = webCertUserService.getUser();
        final var careUnit = createCareUnit(user.getValdVardenhet().getId(), user.getValdVardgivare().getId());
        final var links = new ArrayList<ActionLink>();

        final AccessEvaluationParameters accessEvaluationParameters = AccessEvaluationParameters.create(
                entry.getCertificateType(),
                entry.getCertificateTypeVersion(),
                careUnit,
                Personnummer.createPersonnummer(entry.getCivicRegistrationNumber()).get(),
                entry.isTestIndicator()
        );

        if (certificateAccessServiceHelper.isAllowToRead(accessEvaluationParameters)) {
            links.add(new ActionLink(ActionLinkType.LASA_INTYG));
        }

        if (certificateAccessServiceHelper.isAllowToRenew(accessEvaluationParameters)) {
            links.add(new ActionLink(ActionLinkType.FORNYA_INTYG));
        }

        return links;
    }

    private List<ResourceLinkDTO> convertResourceLinks(List<ActionLink> links, String unitId, String certificateType, String status) {
        return links.stream()
                .map((link) -> getConvertedResourceLink(link, unitId, certificateType, status))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ResourceLinkDTO getConvertedResourceLink(ActionLink link, String savedUnitId, String certificateType, String status) {
        if (link.getType() == ActionLinkType.LASA_INTYG) {
            return ResourceLinkFactory.read();
        } else if (link.getType() == ActionLinkType.VIDAREBEFORDRA_UTKAST && !status.equals(UtkastStatus.DRAFT_LOCKED.toString())) {
            return ResourceLinkFactory.forwardGeneric();
        } else if (link.getType() == ActionLinkType.FORNYA_INTYG) {
            final var loggedInCareUnitId = userService.getLoggedInCareUnit(webCertUserService.getUser()).getId();
            return ResourceLinkFactory.renew(loggedInCareUnitId, savedUnitId, certificateType);
        }
        return null;
    }

    private Vardenhet createCareUnit(String unitId, String caregiverId) {
        final var vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(unitId);
        vardenhet.setVardgivare(new Vardgivare());
        vardenhet.getVardgivare().setVardgivarid(caregiverId);
        return vardenhet;
    }

    private ForwardedListInfo getForwardedListInfo(ListIntygEntry entry) {
        final var unit = hsaOrganizationsService.getVardenhet(entry.getVardenhetId());
        final var careGiver = hsaOrganizationsService.getVardgivareInfo(entry.getVardgivarId());
        return new ForwardedListInfo(
                entry.isVidarebefordrad(),
                unit.getNamn(),
                careGiver.getNamn());
    }
}
