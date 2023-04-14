/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.cfl.api.activator.impl;

import org.apache.commons.logging.Log;
import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.cfl.api.activator.ModuleActivatorStep;
import org.openmrs.module.cflcore.api.program.PatientProgramDetails;
import org.openmrs.module.cflcore.api.service.PatientProgramDetailsService;
import org.openmrs.module.multiproject.aop.ProjectBasedFilterAfterAdvice;
import org.openmrs.module.multiproject.api.service.NameAndProjectSlugSuffixGetter;
import org.openmrs.module.multiproject.filter.NameSuffixProjectBasedFilter;
import org.openmrs.module.multiproject.filter.ProjectAssignmentProjectBasedFilter;
import org.openmrs.module.patientflags.Flag;
import org.openmrs.module.patientflags.api.FlagService;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;

public class InstallMultiProjectAdviceActivatorStep implements ModuleActivatorStep {
  @Override
  public int getOrder() {
    return ModuleActivatorStepOrderEnum.INSTALL_MULTI_PROJECT_ADVICE_ACTIVATOR_STEP.ordinal();
  }

  @Override
  public void startup(Log log) throws Exception {
    Context.addAdvice(
        AppFrameworkService.class,
        new ProjectBasedFilterAfterAdvice<>(
            new NameSuffixProjectBasedFilter<>(
                new NameAndProjectSlugSuffixGetter<>(Extension::getId)),
            AppFrameworkService.class.getMethod("getExtensionsForCurrentUser"),
            AppFrameworkService.class.getMethod("getExtensionsForCurrentUser", String.class),
            AppFrameworkService.class.getMethod(
                "getExtensionsForCurrentUser", String.class, AppContextModel.class)));

    Context.addAdvice(
        PatientProgramDetailsService.class,
        new ProjectBasedFilterAfterAdvice<>(
            new ProjectAssignmentProjectBasedFilter<>(
                Program.class, PatientProgramDetails::getProgramUuid),
            PatientProgramDetailsService.class.getMethod(
                "getPatientProgramsDetails", Patient.class, FragmentConfiguration.class)));

    Context.addAdvice(
        FlagService.class,
        new ProjectBasedFilterAfterAdvice<>(
            new ProjectAssignmentProjectBasedFilter<>(Flag.class, Flag::getUuid),
            FlagService.class.getMethod("generateFlagsForPatient", Patient.class)));
  }
}
