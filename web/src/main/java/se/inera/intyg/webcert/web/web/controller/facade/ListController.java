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
package se.inera.intyg.webcert.web.web.controller.facade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.list.ListDraftsFacadeServiceImpl;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ListResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.list.ListDraftsRequestDTO;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/list")
public class ListController {

    private static final Logger LOG = LoggerFactory.getLogger(ListController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final ListDraftsFacadeServiceImpl listDraftsFacadeService;

    @Autowired
    public ListController(ListDraftsFacadeServiceImpl listDraftsFacadeService) {
        this.listDraftsFacadeService = listDraftsFacadeService;
    }

    @Path("/draft")
    @POST
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getListOfDrafts(ListDraftsRequestDTO request) {
        LOG.debug("Getting list of drafts");
        final var listInfo = listDraftsFacadeService.get(request.getFilter());
        return Response.ok(ListResponseDTO.create(listInfo.getList(), listInfo.getTotalCount())).build();
    }
}
