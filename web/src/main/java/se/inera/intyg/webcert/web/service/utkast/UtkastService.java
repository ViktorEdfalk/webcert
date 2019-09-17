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
package se.inera.intyg.webcert.web.service.utkast;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.UpdatePatientOnDraftRequest;

public interface UtkastService {

    Utkast createNewDraft(CreateNewDraftRequest request);

    /**
     * Retrieves an utkast and always create a PDL log event.
     *
     * @param intygId utkast id
     * @param intygType utkast type
     * @return Utkast
     */
    Utkast getDraft(String intygId, String intygType);

    /**
     * Retrieves an utkast and will only create a PDL log event if createPdlLogEvent is set as true.
     *
     * Can be used when the utkast need to be retrieved for another service but won't be returned to the user.
     *
     * @param intygId utkast id
     * @param intygType utkast type
     * @param createPdlLogEvent true if a PDL log event should be logged.
     * @return Utkast
     */
    Utkast getDraft(String intygId, String intygType, boolean createPdlLogEvent);

    /**
     * Updates an Utkast with data from an existing signed certificate.
     *
     * @return {@link SaveDraftResponse}
     */
    SaveDraftResponse updateDraftFromCandidate(String fromIntygId, String fromIntygType, String toUtkastId, String toUtkastType);

    Utkast setNotifiedOnDraft(String intygsId, long version, Boolean notified);

    SaveDraftResponse saveDraft(String intygId, long version, String draftAsJson, boolean createPdlLogEvent);

    void updatePatientOnDraft(UpdatePatientOnDraftRequest request);

    DraftValidation validateDraft(String intygId, String intygType, String draft);

    List<Lakare> getLakareWithDraftsByEnhet(String enhetsId);

    List<Utkast> filterIntyg(UtkastFilter filter);

    Map<String, Long> getNbrOfUnsignedDraftsByCareUnits(List<String> careUnitIds);

    void deleteUnsignedDraft(String intygId, long version);

    int countFilterIntyg(UtkastFilter filter);

    String getQuestions(String intygsTyp, String version);

    void setKlarForSigneraAndSendStatusMessage(String intygsId, String intygsTyp);

    /**
     * Fairly specialized method to check if a person has existing Intyg of the same type.
     * Returns a Map of either "Utkast" or "Intyg" to Map of String, Boolean, where String is the intyg type and the
     * Boolean indicates that a previous Intyg or Utkast of the same type exists within the same caregiver.
     *
     * @param personnummer the personnummer of the patient to check for existing intyg
     * @param user the intended creator of the certificate or the logged in user
     */
    Map<String, Map<String, PreviousIntyg>> checkIfPersonHasExistingIntyg(Personnummer personnummer, IntygUser user);

    int lockOldDrafts(int lockedAfterDay, LocalDate today);

    void revokeLockedDraft(String intygId, String intygTyp, String revokeMessage, String reason);
}
