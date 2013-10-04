package se.inera.webcert.converter.util;


import iso.v21090.dt.v1.II;
import org.joda.time.LocalDateTime;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType;
import se.inera.webcert.medcertqa.v1.Amnetyp;
import se.inera.webcert.medcertqa.v1.InnehallType;
import se.inera.webcert.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.webcert.medcertqa.v1.VardAdresseringsType;
import se.inera.webcert.persistence.fragasvar.model.Amne;
import se.inera.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.webcert.persistence.fragasvar.model.Vardperson;

/**
 * Created by pehr on 10/2/13.
 */
public class ConvertToFKTypes {

    static String VARDGIVARE_ROOT = "1.2.752.129.2.1.4.1";
    static String ARBETSPLATSKOD_ROOT ="1.2.752.29.4.71";
    static String HSAID_ROOT = "1.2.752.129.2.1.4.1";

    public static II toII(String root, String ext) {
        if (root == null||ext ==null) return null;

        II ii = new II();
        ii.setRoot(root);
        ii.setExtension(ext);
        return ii;
    }

    public static Amnetyp toAmneTyp(Amne amne){
        switch(amne){
            case ARBETSTIDSFORLAGGNING:
                return Amnetyp.ARBETSTIDSFORLAGGNING;
            case AVSTAMNINGSMOTE:
                return Amnetyp.AVSTAMNINGSMOTE;
            case KOMPLETTERING_AV_LAKARINTYG:
                return Amnetyp.KOMPLETTERING_AV_LAKARINTYG;
            case KONTAKT:
                return Amnetyp.KONTAKT;
            case MAKULERING_AV_LAKARINTYG:
                return Amnetyp.MAKULERING_AV_LAKARINTYG;
            case OVRIGT:
                return Amnetyp.OVRIGT;
            case PAMINNELSE:
                return Amnetyp.PAMINNELSE;
            default:
            return null;
        }

    }


    public static InnehallType toInnehallType(String text, LocalDateTime singeringsDatum){
        InnehallType iht = new InnehallType();
        iht.setMeddelandeText(text);
        iht.setSigneringsTidpunkt(singeringsDatum);
        return iht;
    }

    public static LakarutlatandeEnkelType toLakarUtlatande(IntygsReferens ir) {
        LakarutlatandeEnkelType lu = new LakarutlatandeEnkelType();
        lu.setLakarutlatandeId(ir.getIntygsId());
        PatientType pt = new PatientType();
        pt.setFullstandigtNamn(ir.getPatientNamn());
        pt.setPersonId(toII(ir.getPatientId().getPatientIdRoot(),ir.getPatientId().getPatientId()));

        return  lu;
    }

    public static VardAdresseringsType toVardAdresseringsType(Vardperson vp){
        VardAdresseringsType vat = new VardAdresseringsType();

        HosPersonalType hos = new HosPersonalType();
        hos.setForskrivarkod(vp.getForskrivarKod());
        hos.setFullstandigtNamn(vp.getNamn());
        hos.setPersonalId(toII(HSAID_ROOT, vp.getHsaId()));

        hos.setEnhet(toEnhetType(vp));

        vat.setHosPersonal(hos);

        return vat;
    }

    public static EnhetType toEnhetType(Vardperson vp){
        EnhetType et = new EnhetType();
        et.setEnhetsnamn(vp.getEnhetsnamn());
        et.setEpost(vp.getEpost());
        et.setPostadress(vp.getPostadress());
        et.setPostnummer(vp.getPostnummer());
        et.setPostort(vp.getPostort());
        et.setTelefonnummer(vp.getTelefonnummer());
        et.setArbetsplatskod(toII(ARBETSPLATSKOD_ROOT, vp.getArbetsplatsKod()));

        VardgivareType vgt = new VardgivareType();
        vgt.setVardgivarnamn(vp.getVardgivarnamn());
        vgt.setVardgivareId(toII(VARDGIVARE_ROOT, vp.getVardgivarId()));

        et.setVardgivare(vgt);

        return et;
    }
}
