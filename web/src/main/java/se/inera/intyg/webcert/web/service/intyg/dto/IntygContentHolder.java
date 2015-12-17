/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.intyg.dto;

import java.util.List;

import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.Utlatande;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;

public class IntygContentHolder {

    @JsonRawValue
    private final String contents;

    @JsonIgnore
    private final Utlatande utlatande;
    private final List<Status> statuses;
    private final boolean revoked;


    public IntygContentHolder(String contents, Utlatande utlatande, List<Status> statuses, boolean revoked) {
        super();
        this.contents = contents;
        this.utlatande = utlatande;
        this.statuses = statuses;
        this.revoked = revoked;
    }

    public String getContents() {
        return contents;
    }

    public Utlatande getUtlatande() {
        return utlatande;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public boolean isRevoked() {
        return revoked;
    }

}
