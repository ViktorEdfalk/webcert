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

package se.inera.intyg.webcert.web.service.facade.list.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListFilterConfig;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.TableHeading;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.ListFilterConfigFactory;
import se.inera.intyg.webcert.web.service.facade.list.config.factory.TableHeadingFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;

import java.util.*;

@Service
public class ListDraftsConfigFacadeServiceImpl implements ListConfigFacadeService {

    private static final String TITLE = "Ej signerade utkast";
    private static final String OPEN_CERTIFICATE_TOOLTIP = "Öppna utkastet.";
    private static final String SEARCH_CERTIFICATE_TOOLTIP = "Sök efter utkast.";
    private static final String DESCRIPTION = "Nedan visas alla ej signerade utkast för den enhet du är inloggad på.";
    private static final String EMPTY_LIST_TEXT = "Det finns inga ej signerade utkast för den enhet du är inloggad på.";

    private final GetStaffInfoFacadeService getStaffInfoFacadeService;
    private final WebCertUserService webCertUserService;

    @Autowired
    public ListDraftsConfigFacadeServiceImpl(GetStaffInfoFacadeService getStaffInfoFacadeService, WebCertUserService webCertUserService) {
        this.getStaffInfoFacadeService = getStaffInfoFacadeService;
        this.webCertUserService = webCertUserService;
    }

    @Override
    public ListConfig get() {
        return getListDraftsConfig();
    }

    private ListConfig getListDraftsConfig() {
        final var config = new ListConfig();
        config.setTitle(TITLE);
        config.setFilters(getListDraftsFilters());
        //config.setOpenCertificateTooltip(OPEN_CERTIFICATE_TOOLTIP);
        //config.setSearchCertificateTooltip(SEARCH_CERTIFICATE_TOOLTIP);
        config.setTableHeadings(getTableHeadings());
        config.setDescription(DESCRIPTION);
        config.setEmptyListText(EMPTY_LIST_TEXT);
        config.setSecondaryTitle(getSecondaryTitle());
        return config;
    }

    private String getSecondaryTitle() {
        final var user = webCertUserService.getUser();
        return "Intyg visas för " + user.getValdVardenhet().getNamn();
    }

    public TableHeading[] getTableHeadings() {
        return new TableHeading[] {
                TableHeadingFactory.text(ListColumnType.CERTIFICATE_TYPE_NAME),
                TableHeadingFactory.text(ListColumnType.STATUS, getStatusDescription()),
                TableHeadingFactory.date(ListColumnType.SAVED, false),
                TableHeadingFactory.patientInfo(ListColumnType.PATIENT_ID),
                TableHeadingFactory.text(ListColumnType.SAVED_BY),
                TableHeadingFactory.forwarded(ListColumnType.FORWARDED, "Visar om utkastet är vidarebefordrat."),
                TableHeadingFactory.openButton(ListColumnType.CERTIFICATE_ID)
        };
    }

    private List<ListFilterConfig> getListDraftsFilters() {
        final var filters = new ArrayList<ListFilterConfig>();
        filters.add(ListFilterConfigFactory.forwardedSelect());
        filters.add(ListFilterConfigFactory.draftStatusSelect());
        filters.add(getSavedByFilter());
        filters.add(ListFilterConfigFactory.defaultPersonId());
        filters.add(ListFilterConfigFactory.savedDateRange());
        filters.add(ListFilterConfigFactory.orderBy(ListColumnType.SAVED));
        filters.add(ListFilterConfigFactory.ascending());
        filters.add(ListFilterConfigFactory.pageSize());
        return filters;
    }

    private ListFilterConfig getSavedByFilter() {
        final var savedByList = getStaffInfoFacadeService.get();
        final var defaultValue = getStaffInfoFacadeService.isLoggedInUserDoctor() ? getStaffInfoFacadeService.getLoggedInStaffHsaId() : "";
        return ListFilterConfigFactory.createStaffSelect("SAVED_BY", "Sparat av", savedByList, defaultValue);
    }

    private String getStatusDescription() {
        return "<p>Visar utkastets status:<ul>"
                + "<li>Utkast, uppgifter saknas = utkastet är sparat, men obligatoriska uppgifter saknas."
                + "</li><li>Utkast, kan signeras = utkastet är komplett, sparat och kan signeras.</li>"
                + "<li>Utkast, låst = Utkastet är låst.</li></ul></p>";
    }
}
