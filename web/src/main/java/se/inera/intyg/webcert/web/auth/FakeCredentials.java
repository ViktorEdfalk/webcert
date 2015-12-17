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

package se.inera.intyg.webcert.web.auth;

import java.io.Serializable;

/**
 * @author andreaskaltenbach
 */
public class FakeCredentials implements Serializable {

    private static final long serialVersionUID = -7620199916206349045L;

    private String hsaId;
    private String fornamn;
    private String efternamn;
    private String enhetId;
    private boolean lakare = false;
    private boolean tandlakare = false;
    private String befattningsKod;
    private String forskrivarKod;

    public FakeCredentials() {
    }

    public FakeCredentials(FakeCredentialsBuilder builder) {
        this.hsaId = builder.hsaId;
        this.fornamn = builder.fornamn;
        this.efternamn = builder.efternamn;
        this.enhetId = builder.enhetId;
        this.lakare = builder.lakare;
        this.tandlakare = builder.tandlakare;
        this.befattningsKod = builder.befattningsKod;
        this.forskrivarKod = builder.forskrivarKod;
    }

    // ~ Getter and setters
    // ~====================================================================================

    public String getBefattningsKod() {
        return befattningsKod;
    }

    public void setBefattningsKod(String befattningsKod) {
        this.befattningsKod = befattningsKod;
    }

    public String getEfternamn() {
        return efternamn;
    }

    public void setEfternamn(String efternamn) {
        this.efternamn = efternamn;
    }

    public String getEnhetId() {
        return enhetId;
    }

    public void setEnhetId(String enhetId) {
        this.enhetId = enhetId;
    }

    public String getFornamn() {
        return fornamn;
    }

    public void setFornamn(String fornamn) {
        this.fornamn = fornamn;
    }

    public String getForskrivarKod() {
        return forskrivarKod;
    }

    public void setForskrivarKod(String forskrivarKod) {
        this.forskrivarKod = forskrivarKod;
    }

    public String getHsaId() {
        return hsaId;
    }

    public void setHsaId(String hsaId) {
        this.hsaId = hsaId;
    }

    public boolean isLakare() {
        return lakare;
    }

    public boolean isTandlakare() {
        return tandlakare;
    }

    public void setTandlakare(boolean tandlakare) {
        this.tandlakare = tandlakare;
    }


    // ~ Public methods
    // ~====================================================================================

    @Override
    public String toString() {
        return "FakeCredentials{"
                + "hsaId='" + hsaId + '\''
                + ", fornamn='" + fornamn + '\''
                + ", efternamn='" + efternamn + '\''
                + ", lakare=" + lakare
                + '}';
    }


    // ~ Builder class
    // ~====================================================================================

    public static class FakeCredentialsBuilder {
        private String hsaId;
        private String fornamn;
        private String efternamn;
        private String enhetId;
        private boolean lakare = false;
        private boolean tandlakare = false;
        private String befattningsKod;
        private String forskrivarKod;

        public FakeCredentialsBuilder(String hsaId, String fornamn, String efternamn, String enhetId) {
            this.hsaId = hsaId;
            this.fornamn = fornamn;
            this.efternamn = efternamn;
            this.enhetId = enhetId;
        }

        public FakeCredentialsBuilder hsaId(String hsaId) {
            this.hsaId = hsaId;
            return this;
        }

        public FakeCredentialsBuilder fornamn(String fornamn) {
            this.fornamn = fornamn;
            return this;
        }

        public FakeCredentialsBuilder efternamn(String efternamn) {
            this.efternamn = efternamn;
            return this;
        }

        public FakeCredentialsBuilder enhetId(String enhetId) {
            this.enhetId = enhetId;
            return this;
        }

        public FakeCredentialsBuilder lakare(boolean lakare) {
            this.lakare = lakare;
            return this;
        }

        public FakeCredentialsBuilder tandlakare(boolean tandlakare) {
            this.tandlakare = tandlakare;
            return this;
        }

        public FakeCredentialsBuilder forskrivarKod(String forskrivarKod) {
            this.forskrivarKod = forskrivarKod;
            return this;
        }

        public FakeCredentialsBuilder befattningsKod(String befattningsKod) {
            this.befattningsKod = befattningsKod;
            return this;
        }

        public FakeCredentials build() {
            return new FakeCredentials(this);
        }
    }

}
