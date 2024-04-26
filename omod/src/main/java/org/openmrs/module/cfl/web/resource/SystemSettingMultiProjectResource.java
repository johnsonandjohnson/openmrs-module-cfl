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

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.multiproject.api.service.NameAndProjectSlugSuffixGetter;
import org.openmrs.module.multiproject.filter.NameSuffixProjectBasedFilter;
import org.openmrs.module.multiproject.filter.ProjectBasedFilter;
import org.openmrs.module.multiproject.web.resource.MultiProjectFilteringDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.SystemSettingResource1_9;

import java.util.ArrayList;
import java.util.List;

/**
 * The SystemSettingMultiProjectResource overrides default REST for Global Properties
 * (/v1/systemsetting) and applying Project-based filter on search by prefix for global property
 */
@Resource(
    order = SystemSettingMultiProjectResource.RESOURCE_ORDER,
    name = "v1/systemsetting",
    supportedClass = GlobalProperty.class,
    supportedOpenmrsVersions = {
      "1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*"
    })
public class SystemSettingMultiProjectResource
    extends MultiProjectFilteringDelegatingCrudResource<SystemSettingResource1_9, GlobalProperty> {
  public static final int RESOURCE_ORDER = 10;

  private final ProjectBasedFilter<GlobalProperty> projectBasedFilter;

  public SystemSettingMultiProjectResource() {
    super(new SystemSettingResource1_9());
    this.projectBasedFilter =
        new NameSuffixProjectBasedFilter<>(
            new NameAndProjectSlugSuffixGetter<>(GlobalProperty::getProperty));
  }

  @Override
  protected PageableResult doGetAll(RequestContext requestContext) throws ResponseException {
    List<GlobalProperty> allGlobalProperties =
        Context.getAdministrationService().getAllGlobalProperties();
    return new NeedsPaging<>(allGlobalProperties, requestContext);
  }

  @Override
  protected PageableResult doSearch(RequestContext requestContext) {
    AdministrationService service = Context.getAdministrationService();
    List<GlobalProperty> searchResults =
        new ArrayList<>(service.getGlobalPropertiesByPrefix(requestContext.getParameter("q")));
    projectBasedFilter.doFilter(searchResults);
    return new NeedsPaging<>(searchResults, requestContext);
  }
}
