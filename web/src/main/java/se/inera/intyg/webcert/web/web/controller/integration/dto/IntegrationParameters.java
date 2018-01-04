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
package se.inera.intyg.webcert.web.web.controller.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.inera.intyg.webcert.web.web.controller.integration.IntegrationState;

import java.io.Serializable;

public final class IntegrationParameters implements Serializable {

    private final String reference;
    private final String responsibleHospName;
    private final String alternateSsn;
    private final String fornamn;
    private final String mellannamn;
    private final String efternamn;
    private final String postadress;
    private final String postnummer;
    private final String postort;

    private final boolean sjf; //Sammanhållen JournalFöring
    private final boolean patientDeceased;
    private final boolean inactiveUnit;
    private final boolean copyOk;

    private String beforeAlternateSsn = ""; // Sätts när alternateSsn skiljer från intygets patientId för att kunna visa det i utkastet.

    @JsonIgnore
    private IntegrationState state = new IntegrationState();


    // CHECKSTYLE:OFF ParameterNumber
    public IntegrationParameters(String reference, String responsibleHospName, String alternateSsn, String fornamn,
                                 String mellannamn, String efternamn, String postadress, String postnummer, String postort,
                                 boolean sjf, boolean patientDeceased, boolean inactiveUnit, boolean copyOk) {

        this.reference = reference;
        this.responsibleHospName = responsibleHospName;
        this.alternateSsn = alternateSsn;
        this.fornamn = fornamn;
        this.mellannamn = mellannamn;
        this.efternamn = efternamn;
        this.postadress = postadress;
        this.postnummer = postnummer;
        this.postort = postort;
        this.sjf = sjf;
        this.patientDeceased = patientDeceased;
        this.inactiveUnit = inactiveUnit;
        this.copyOk = copyOk;
    }
    // CHECKSTYLE:ON ParameterNumber


    // final class members

    public String getReference() {
        return reference;
    }

    public String getResponsibleHospName() {
        return responsibleHospName;
    }

    public String getAlternateSsn() {
        return alternateSsn;
    }

    public String getFornamn() {
        return fornamn;
    }

    public String getMellannamn() {
        return mellannamn;
    }

    public String getEfternamn() {
        return efternamn;
    }

    public String getPostadress() {
        return postadress;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getPostort() {
        return postort;
    }

    public boolean isSjf() {
        return sjf;
    }

    public boolean isPatientDeceased() {
        return patientDeceased;
    }

    public boolean isInactiveUnit() {
        return inactiveUnit;
    }

    public boolean isCopyOk() {
        return copyOk;
    }


    // non-final class members

    public String getBeforeAlternateSsn() {
        return beforeAlternateSsn;
    }

    public void setBeforeAlternateSsn(String beforeAlternateSsn) {
        this.beforeAlternateSsn = beforeAlternateSsn;
    }

    public IntegrationState getState() {
        return state;
    }

    public void setState(IntegrationState state) {
        this.state = state;
    }

}
