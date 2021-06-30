/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.LockedDraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.GetCertificatesAvailableFunctions;
import se.inera.intyg.webcert.web.service.facade.GetCertificationResourceLinks;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Service
public class GetCertificationResourceLinksImpl implements GetCertificationResourceLinks {

    private final GetCertificatesAvailableFunctions getCertificatesAvailableFunctions;
    private final DraftAccessServiceHelper draftAccessServiceHelper;
    private final LockedDraftAccessServiceHelper lockedDraftAccessServiceHelper;
    private final CertificateAccessServiceHelper certificateAccessServiceHelper;

    @Autowired
    public GetCertificationResourceLinksImpl(
        GetCertificatesAvailableFunctions getCertificatesAvailableFunctions,
        DraftAccessServiceHelper draftAccessServiceHelper,
        LockedDraftAccessServiceHelper lockedDraftAccessServiceHelper,
        CertificateAccessServiceHelper certificateAccessServiceHelper) {
        this.getCertificatesAvailableFunctions = getCertificatesAvailableFunctions;
        this.draftAccessServiceHelper = draftAccessServiceHelper;
        this.lockedDraftAccessServiceHelper = lockedDraftAccessServiceHelper;
        this.certificateAccessServiceHelper = certificateAccessServiceHelper;
    }

    @Override
    public ResourceLinkDTO[] get(Certificate certificate) {
        final var accessEvaluationParameters = createAccessEvaluationParameters(certificate);
        final var availableFunctions = getCertificatesAvailableFunctions.get(certificate);
        final var functions = getAccessFunctions(certificate);
        return availableFunctions.stream()
            .filter(availableFunction -> {
                final var accessFunction = functions.get(availableFunction.getType());
                if (accessFunction == null) {
                    return true;
                }
                return accessFunction.hasAccess(accessEvaluationParameters, certificate);
            })
            .toArray(ResourceLinkDTO[]::new);
    }

    private Map<ResourceLinkTypeDTO, AccessCheck> getAccessFunctions(Certificate certificate) {
        switch (certificate.getMetadata().getStatus()) {
            case UNSIGNED:
                return getFunctionsForDraft();
            case SIGNED:
                return getFunctionsForCertificate();
            case LOCKED:
                return getFunctionsForLockedDraft();
            default:
                return Collections.emptyMap();
        }
    }

    private Map<ResourceLinkTypeDTO, AccessCheck> getFunctionsForDraft() {
        final var functions = new HashMap<ResourceLinkTypeDTO, AccessCheck>();

        functions.put(ResourceLinkTypeDTO.EDIT_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                draftAccessServiceHelper.isAllowToEditUtkast(accessEvaluationParameters)
        );

        functions.put(ResourceLinkTypeDTO.PRINT_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                draftAccessServiceHelper.isAllowToPrintUtkast(accessEvaluationParameters)
        );

        functions.put(ResourceLinkTypeDTO.REMOVE_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                draftAccessServiceHelper.isAllowToDeleteUtkast(accessEvaluationParameters)
        );

        functions.put(ResourceLinkTypeDTO.SIGN_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                draftAccessServiceHelper.isAllowToSign(accessEvaluationParameters, certificate.getMetadata().getId())
        );

        functions.put(ResourceLinkTypeDTO.FORWARD_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                draftAccessServiceHelper.isAllowedToForwardUtkast(accessEvaluationParameters)
        );

        return functions;
    }

    private Map<ResourceLinkTypeDTO, AccessCheck> getFunctionsForLockedDraft() {
        final var functions = new HashMap<ResourceLinkTypeDTO, AccessCheck>();

        functions.put(ResourceLinkTypeDTO.PRINT_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                lockedDraftAccessServiceHelper.isAllowToPrint(accessEvaluationParameters)
        );

        functions.put(ResourceLinkTypeDTO.COPY_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                lockedDraftAccessServiceHelper.isAllowToCopy(accessEvaluationParameters)
        );

        functions.put(ResourceLinkTypeDTO.REVOKE_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                lockedDraftAccessServiceHelper.isAllowToInvalidate(accessEvaluationParameters)
        );

        return functions;
    }

    private Map<ResourceLinkTypeDTO, AccessCheck> getFunctionsForCertificate() {
        final var functions = new HashMap<ResourceLinkTypeDTO, AccessCheck>();

        functions.put(ResourceLinkTypeDTO.PRINT_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                certificateAccessServiceHelper.isAllowToPrint(accessEvaluationParameters, false)
        );

        functions.put(ResourceLinkTypeDTO.REPLACE_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                certificateAccessServiceHelper.isAllowToReplace(accessEvaluationParameters)
        );

        functions.put(ResourceLinkTypeDTO.REPLACE_CERTIFICATE_CONTINUE,
            (accessEvaluationParameters, certificate) ->
                certificateAccessServiceHelper.isAllowToReplace(accessEvaluationParameters)
        );

        functions.put(ResourceLinkTypeDTO.RENEW_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                certificateAccessServiceHelper.isAllowToRenew(accessEvaluationParameters)
        );

        functions.put(ResourceLinkTypeDTO.REVOKE_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                certificateAccessServiceHelper.isAllowToInvalidate(accessEvaluationParameters)
        );

        functions.put(ResourceLinkTypeDTO.SEND_CERTIFICATE,
            (accessEvaluationParameters, certificate) ->
                certificateAccessServiceHelper.isAllowToSend(accessEvaluationParameters)
        );

        return functions;
    }

    private AccessEvaluationParameters createAccessEvaluationParameters(Certificate certificate) {
        final var unit = createUnit(certificate);

        return AccessEvaluationParameters.create(
            certificate.getMetadata().getType(),
            certificate.getMetadata().getTypeVersion(),
            unit,
            Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).orElseThrow(),
            certificate.getMetadata().isTestCertificate()
        );
    }

    private Vardenhet createUnit(Certificate certificate) {
        final var unit = new Vardenhet();
        unit.setEnhetsid(certificate.getMetadata().getUnit().getUnitId());
        unit.setVardgivare(new Vardgivare());
        unit.getVardgivare().setVardgivarid(certificate.getMetadata().getCareProvider().getUnitId());
        return unit;
    }

    private interface AccessCheck {

        boolean hasAccess(AccessEvaluationParameters accessEvaluationParameters, Certificate certificate);
    }
}
