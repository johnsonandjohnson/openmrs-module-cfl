/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.cfl.web.resource;

import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.multiproject.filter.ProjectAssignmentProjectBasedFilter;
import org.openmrs.module.multiproject.filter.ProjectBasedFilter;
import org.openmrs.module.multiproject.web.resource.MultiProjectFilteringDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.VisitTypeResource1_9;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Resource(
    order = VisitTypeMultiProjectResource.RESOURCE_ORDER,
    name = RestConstants.VERSION_1 + "/visittype",
    supportedClass = VisitType.class,
    supportedOpenmrsVersions = {
      "1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*"
    })
public class VisitTypeMultiProjectResource
    extends MultiProjectFilteringDelegatingCrudResource<VisitTypeResource1_9, VisitType> {
  public static final int RESOURCE_ORDER = 10;

  private final ProjectBasedFilter<VisitType> projectBasedFilter;

  public VisitTypeMultiProjectResource() {
    super(new VisitTypeResource1_9());
    this.projectBasedFilter =
        new ProjectAssignmentProjectBasedFilter<>(VisitType.class, VisitType::getUuid);
  }

  @Override
  protected PageableResult doGetAll(RequestContext context) throws ResponseException {
    final List<VisitType> visitTypes =
        new ArrayList<>(Context.getVisitService().getAllVisitTypes(!context.getIncludeAll()));

    projectBasedFilter.doFilter(visitTypes);
    return new NeedsPaging<>(visitTypes, context);
  }

  @Override
  protected PageableResult doSearch(RequestContext context) {
    final List<VisitType> visitTypes =
        Context.getVisitService().getVisitTypes(context.getParameter("q"));
    final List<VisitType> filteredVisitTypes;

    if (Boolean.TRUE.equals(context.getIncludeAll())) {
      filteredVisitTypes = new ArrayList<>(visitTypes);
    } else {
      filteredVisitTypes =
          visitTypes.stream()
              .filter(visitType -> Boolean.FALSE.equals(visitType.getRetired()))
              .collect(Collectors.toList());
    }

    projectBasedFilter.doFilter(filteredVisitTypes);
    return new NeedsPaging<>(filteredVisitTypes, context);
  }
}
