package se.inera.webcert.service.intyg.converter;

import se.inera.certificate.model.common.internal.Utlatande;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.webcert.service.intyg.dto.IntygItem;
import se.riv.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;

import java.util.List;


public interface IntygServiceConverter {

    List<IntygItem> convertToListOfIntygItem(List<CertificateMetaType> source);

    SendType buildSendTypeFromUtlatande(Utlatande utlatande);

    RevokeType buildRevokeTypeFromUtlatande(Utlatande utlatande, String revokeMessage);
}
