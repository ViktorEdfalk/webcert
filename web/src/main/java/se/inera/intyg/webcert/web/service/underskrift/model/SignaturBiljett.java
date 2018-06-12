package se.inera.intyg.webcert.web.service.underskrift.model;

import org.w3._2000._09.xmldsig_.SignatureType;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SignaturBiljett implements Serializable {
    private String ticketId;
    private String intygsId;
    private long version;
    private SignaturStatus status;
    private String intygDigest;
    private String signableDigest;
    private SignatureType signatureType;
    private LocalDateTime skapad;

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getIntygsId() {
        return intygsId;
    }

    public void setIntygsId(String intygsId) {
        this.intygsId = intygsId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public SignaturStatus getStatus() {
        return status;
    }

    public void setStatus(SignaturStatus status) {
        this.status = status;
    }

    public String getIntygDigest() {
        return intygDigest;
    }

    public void setIntygDigest(String intygDigest) {
        this.intygDigest = intygDigest;
    }

    public String getSignableDigest() {
        return signableDigest;
    }

    public void setSignableDigest(String signableDigest) {
        this.signableDigest = signableDigest;
    }

    public SignatureType getSignatureType() {
        return signatureType;
    }

    public void setSignatureType(SignatureType signatureType) {
        this.signatureType = signatureType;
    }

    public LocalDateTime getSkapad() {
        return skapad;
    }

    public void setSkapad(LocalDateTime skapad) {
        this.skapad = skapad;
    }


    public static final class SignaturBiljettBuilder {
        private String ticketId;
        private String intygsId;
        private long version;
        private SignaturStatus status;
        private String intygDigest;
        private String signableDigest;
        private SignatureType signatureType;
        private LocalDateTime skapad;

        private SignaturBiljettBuilder() {
        }

        public static SignaturBiljettBuilder aSignaturBiljett() {
            return new SignaturBiljettBuilder();
        }

        public SignaturBiljettBuilder withTicketId(String ticketId) {
            this.ticketId = ticketId;
            return this;
        }

        public SignaturBiljettBuilder withIntygsId(String intygsId) {
            this.intygsId = intygsId;
            return this;
        }

        public SignaturBiljettBuilder withVersion(long version) {
            this.version = version;
            return this;
        }

        public SignaturBiljettBuilder withStatus(SignaturStatus status) {
            this.status = status;
            return this;
        }

        public SignaturBiljettBuilder withIntygDigest(String intygDigest) {
            this.intygDigest = intygDigest;
            return this;
        }

        public SignaturBiljettBuilder withSignableDigest(String signableDigest) {
            this.signableDigest = signableDigest;
            return this;
        }

        public SignaturBiljettBuilder withSignatureType(SignatureType signatureType) {
            this.signatureType = signatureType;
            return this;
        }

        public SignaturBiljettBuilder withSkapad(LocalDateTime skapad) {
            this.skapad = skapad;
            return this;
        }

        public SignaturBiljett build() {
            SignaturBiljett signaturBiljett = new SignaturBiljett();
            signaturBiljett.setTicketId(ticketId);
            signaturBiljett.setIntygsId(intygsId);
            signaturBiljett.setVersion(version);
            signaturBiljett.setStatus(status);
            signaturBiljett.setIntygDigest(intygDigest);
            signaturBiljett.setSignableDigest(signableDigest);
            signaturBiljett.setSignatureType(signatureType);
            signaturBiljett.setSkapad(skapad);
            return signaturBiljett;
        }
    }
}
