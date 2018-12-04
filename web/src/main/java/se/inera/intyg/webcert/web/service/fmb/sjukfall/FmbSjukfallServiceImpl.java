/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.fmb.sjukfall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.PersonId;
import se.riv.clinicalprocess.healthcond.rehabilitation.v1.IntygsData;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonResponderInterface;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonResponseType;
import se.inera.intyg.clinicalprocess.healthcond.rehabilitation.listsickleavesforperson.v1.ListSickLeavesForPersonType;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.fmb.sjukfall.converter.IntygstjanstConverter;

@Service
public class FmbSjukfallServiceImpl implements FmbSjukfallService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int MAX_GLAPP = 5;
    private static final int MAX_SEDAN_SJUKAVSLUT = 0;

    private final ListSickLeavesForPersonResponderInterface listSickLeavesForPersonResponder;
    private final SjukfallEngineService sjukfallEngineService;

    public FmbSjukfallServiceImpl(
            final ListSickLeavesForPersonResponderInterface listSickLeavesForPersonResponder,
            final SjukfallEngineService sjukfallEngineService) {
        this.listSickLeavesForPersonResponder = listSickLeavesForPersonResponder;
        this.sjukfallEngineService = sjukfallEngineService;
    }

    @Override
    public int totalSjukskrivningstidForPatientAndCareUnit(final Personnummer personnummer) {

        LOG.debug("Starting: Fetch intyg data to Calculate total sjukskrivningstid for patient and care unit");

        final ListSickLeavesForPersonType request = createRequest(personnummer);
        final ListSickLeavesForPersonResponseType response = listSickLeavesForPersonResponder.listSickLeavesForPerson("", request);

        final List<IntygsData> intygsData = response.getIntygsLista().getIntygsData();

        final LocalDate localDate = LocalDate.now();
        final List<IntygData> intygData = IntygstjanstConverter.toSjukfallFormat(intygsData);

        final IntygParametrar intygParametrar = new IntygParametrar(MAX_GLAPP, MAX_SEDAN_SJUKAVSLUT, localDate);
        final List<SjukfallEnhet> sjukfallForEnhet = sjukfallEngineService.beraknaSjukfallForEnhet(intygData, intygParametrar);

        LOG.debug("Done: Fetch intyg data to Calculate total sjukskrivningstid for patient and care unit");

        return getTotaltAntalDagar(sjukfallForEnhet);
    }


    private ListSickLeavesForPersonType createRequest(final Personnummer personnummer) {
        PersonId personId = new PersonId();
        personId.setExtension(personnummer.getOriginalPnr());

        ListSickLeavesForPersonType request = new ListSickLeavesForPersonType();
        request.setPersonId(personId);

        return request;
    }

    private int getTotaltAntalDagar(final List<SjukfallEnhet> sjukfallForEnhet) {
        return sjukfallForEnhet.stream()
                .map(SjukfallEnhet::getDagar)
                .reduce(0, Integer::sum);
    }
}
