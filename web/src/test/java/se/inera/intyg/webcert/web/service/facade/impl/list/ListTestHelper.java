/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.facade.impl.list;

import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.*;
import se.inera.intyg.webcert.web.service.facade.list.dto.CertificateListItem;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.service.facade.list.dto.PatientListInfo;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.CreateUtkastRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

class ListTestHelper {
    private static final String PATIENT_EFTERNAMN = "Tolvansson";
    private static final String PATIENT_FORNAMN = "Tolvan";
    private static final String PATIENT_MELLANNAMN = "Von";
    private static final String PATIENT_POSTADRESS = "Testadress";
    private static final String PATIENT_POSTNUMMER = "12345";
    private static final String PATIENT_POSTORT = "Testort";

    private static final Personnummer PATIENT_PERSONNUMMER = createPnr("19121212-1212");
    private static final Personnummer PATIENT_PERSONNUMMER_PU_SEKRETESS = createPnr("20121212-1212");
    private static final java.lang.String INTYG_TYPE_VERSION = "1.2";

    public static ListFilter createListFilter() {
        final var filter = new ListFilter();
        final var map = new HashMap<String, ListFilterValue>();
        map.put("PAGESIZE", new ListFilterNumberValue(10));
        map.put("STARTFROM", new ListFilterNumberValue(0));
        map.put("ORDER_BY", new ListFilterTextValue("SAVED"));
        map.put("ASCENDING", new ListFilterBooleanValue(true));
        filter.setValues(map);
        return filter;
    }

    public static WebCertUser setupUser(WebCertUserService webcertUserService, String privilegeString, String intygType, String... features) {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());
        user.getFeatures().putAll(Stream.of(features).collect(Collectors.toMap(Function.identity(), s -> {
            Feature feature = new Feature();
            feature.setName(s);
            feature.setIntygstyper(Arrays.asList(intygType));
            feature.setGlobal(true);
            return feature;
        })));
        Privilege privilege = new Privilege();
        privilege.setIntygstyper(Arrays.asList(intygType));
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName("NORMAL");
        requestOrigin.setIntygstyper(privilege.getIntygstyper());
        privilege.setRequestOrigins(Arrays.asList(requestOrigin));
        user.getAuthorities().put(privilegeString, privilege);
        user.setOrigin("NORMAL");

        user.setValdVardenhet(buildVardenhet());
        user.setValdVardgivare(buildVardgivare());
        when(webcertUserService.getUser()).thenReturn(user);
        return user;
    }

    public static CreateUtkastRequest buildRequest(String typ) {
        CreateUtkastRequest request = new CreateUtkastRequest();
        request.setIntygType(typ);
        request.setPatientEfternamn(PATIENT_EFTERNAMN);
        request.setPatientFornamn(PATIENT_FORNAMN);
        request.setPatientMellannamn(PATIENT_MELLANNAMN);
        request.setPatientPersonnummer(PATIENT_PERSONNUMMER);
        request.setPatientPostadress(PATIENT_POSTADRESS);
        request.setPatientPostnummer(PATIENT_POSTNUMMER);
        request.setPatientPostort(PATIENT_POSTORT);
        return request;
    }

    public static SelectableVardenhet buildVardgivare() {
        Vardgivare vardgivare = new Vardgivare();
        vardgivare.setId("456");
        vardgivare.setNamn("vardgivarnamn");
        return vardgivare;
    }

    public static SelectableVardenhet buildVardenhet() {
        Vardenhet enhet = new Vardenhet();
        enhet.setId("123");
        enhet.setNamn("Enhetsnamn");
        enhet.setEpost("test@test.com");
        enhet.setTelefonnummer("12345");
        enhet.setPostadress("Enhetsadress");
        enhet.setPostnummer("12345");
        enhet.setPostort("Enhetsort");
        enhet.setArbetsplatskod("000000");
        return enhet;
    }

    public static Personnummer createPnr(String personId) {
        return Personnummer.createPersonnummer(personId).get();
    }

    public static CertificateListItem createCertificateListItemWithPersonId(String personId) {
        final var item = new CertificateListItem();
        item.addValue(ListColumnType.PATIENT_ID, new PatientListInfo(personId, true, true, true));
        return item;
    }

    public static CertificateListItem createCertificateListItemWithForwarded(boolean forwarded) {
        final var item = new CertificateListItem();
        item.addValue(ListColumnType.FORWARDED, forwarded);
        return item;
    }

    public static CertificateListItem createCertificateListItemWithSavedBy(String savedBy) {
        final var item = new CertificateListItem();
        item.addValue(ListColumnType.SAVED_BY, savedBy);
        return item;
    }

    public static CertificateListItem createCertificateListItemWithSaved(LocalDateTime saved) {
        final var item = new CertificateListItem();
        item.addValue(ListColumnType.SAVED, saved);
        return item;
    }

    public static CertificateListItem createCertificateListItemWithCertificateTypeName(String name) {
        final var item = new CertificateListItem();
        item.addValue(ListColumnType.CERTIFICATE_TYPE_NAME, name);
        return item;
    }

    public static CertificateListItem createCertificateListItemWithStatus(String name) {
        final var item = new CertificateListItem();
        item.addValue(ListColumnType.STATUS, name);
        return item;
    }

    public static ListIntygEntry createListIntygEntry(String status, boolean includePatientStatuses, boolean forwarded) {
        return createListIntygEntry(status, includePatientStatuses, forwarded, "191212121212");
    }

    public static ListIntygEntry createListIntygEntry(String status, boolean includePatientStatuses, boolean forwarded, String patientId) {
        final var listIntygEntry = new ListIntygEntry();
        listIntygEntry.setIntygType("luse");
        listIntygEntry.setIntygId("CERTIFICATE_ID");
        listIntygEntry.setIntygTypeName("CERTIFICATE_TYPE_NAME");
        listIntygEntry.setStatus(status);
        listIntygEntry.setPatientId(createPnr(patientId));
        listIntygEntry.setLastUpdatedSigned(LocalDateTime.now());
        listIntygEntry.setVidarebefordrad(forwarded);
        listIntygEntry.setAvliden(includePatientStatuses);
        listIntygEntry.setSekretessmarkering(includePatientStatuses);
        listIntygEntry.setTestIntyg(includePatientStatuses);
        listIntygEntry.setUpdatedSignedById("HSA_ID");
        return listIntygEntry;
    }

    public static CertificateListEntry createCertificateListEntry() {
        return createCertificateListEntry(true, true, "191212121212");
    }

        public static CertificateListEntry createCertificateListEntry(boolean isSent, boolean includePatientStatuses, String patientId) {
        final var entry = new CertificateListEntry();
        entry.setCertificateType("luse");
        entry.setCertificateId("CERTIFICATE_ID");
        entry.setCertificateTypeName("CERTIFICATE_TYPE_NAME");
        entry.setSent(isSent);
        entry.setCivicRegistrationNumber(patientId);
        entry.setSignedDate(LocalDateTime.now());
        entry.setDeceased(includePatientStatuses);
        entry.setProtectedIdentity(includePatientStatuses);
        entry.setTestIndicator(includePatientStatuses);
        return entry;
    }
}