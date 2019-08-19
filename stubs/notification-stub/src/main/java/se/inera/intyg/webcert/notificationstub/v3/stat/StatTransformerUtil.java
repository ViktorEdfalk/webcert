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
package se.inera.intyg.webcert.notificationstub.v3.stat;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

/**
 * Converts a collection of notification entries into a Map intygsId -> List of entries for that intygsId.
 *
 * Created by eriklupander on 2016-07-05.
 */
public class StatTransformerUtil {

    public Map<String, List<NotificationStubEntry>> toStat(Collection<CertificateStatusUpdateForCareType> notifs) {
        Map<String, List<NotificationStubEntry>> listMap = notifs.stream()
            .map(model -> new NotificationStubEntry(model.getIntyg().getIntygsId().getExtension(),
                model.getHandelse().getHandelsekod().getCode(), model.getHandelse().getTidpunkt()))
            .collect(Collectors.groupingBy(NotificationStubEntry::getIntygsId));

        return listMap;
    }
}
