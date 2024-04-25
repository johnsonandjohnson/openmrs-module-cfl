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
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.cfl.api.activator.ModuleActivatorStep;
import org.openmrs.module.cflcore.api.dto.FlagDTO;
import org.openmrs.module.cflcore.api.program.PatientProgramDetails;
import org.openmrs.module.cflcore.api.service.CustomAdministrationService;
import org.openmrs.module.cflcore.api.service.FlagDTOService;
import org.openmrs.module.cflcore.api.service.PatientProgramDetailsService;
import org.openmrs.module.cflcore.api.util.GlobalPropertiesConstants;
import org.openmrs.module.messages.api.constants.ConfigConstants;
import org.openmrs.module.messages.api.model.PatientTemplate;
import org.openmrs.module.messages.api.model.Template;
import org.openmrs.module.messages.api.service.DefaultPatientTemplateService;
import org.openmrs.module.messages.api.service.MessagesAdministrationService;
import org.openmrs.module.messages.api.service.PatientTemplateService;
import org.openmrs.module.messages.api.service.TemplateService;
import org.openmrs.module.messages.domain.PagingInfo;
import org.openmrs.module.messages.domain.criteria.BaseCriteria;
import org.openmrs.module.multiproject.aop.GlobalPropertyWithUserContextAdvice;
import org.openmrs.module.multiproject.aop.GlobalPropertyWithoutUserContextAdvice;
import org.openmrs.module.multiproject.aop.MultiProjectAroundAdvisor;
import org.openmrs.module.multiproject.aop.ProjectBasedFilterAfterAdvice;
import org.openmrs.module.multiproject.api.service.NameAndProjectSlugSuffixGetter;
import org.openmrs.module.multiproject.filter.NameSuffixProjectBasedFilter;
import org.openmrs.module.multiproject.filter.ProjectAssignmentProjectBasedFilter;
import org.openmrs.module.patientflags.Flag;
import org.openmrs.module.patientflags.api.FlagService;
import org.openmrs.module.visits.api.entity.VisitStatus;
import org.openmrs.module.visits.api.entity.VisitTime;
import org.openmrs.module.visits.api.service.VisitStatusService;
import org.openmrs.module.visits.api.service.VisitTimeService;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public class InstallMultiProjectAdviceActivatorStep implements ModuleActivatorStep {

  private static final List<String> GLOBAL_PROPERTIES_SUPPORTING_MULTI_PROJECT_WITH_USER_CONTEXT =
      Arrays.asList(
          "visits.extraSchedulingInformationEnabled",
          "visits.outsideDateWindowInformationEnabled",
          "visits.visitTypesWithTimeWindow",
          "visits.upcoming-visits-limit",
          "visits.past-visits-limit",
          "visits.visit-form-uris",
          "message.daysToCallBeforeVisit.default");

  private static final List<String>
      GLOBAL_PROPERTIES_SUPPORTING_MULTI_PROJECT_WITHOUT_USER_CONTEXT =
          Arrays.asList(
              GlobalPropertiesConstants.SHOULD_CREATE_FIRST_VISIT_GP_KEY,
              GlobalPropertiesConstants.SHOULD_CREATE_FUTURE_VISITS_GP_KEY,
              GlobalPropertiesConstants.SHOULD_SEND_REMINDER_VIA_SMS_GP_KEY,
              GlobalPropertiesConstants.SHOULD_SEND_REMINDER_VIA_CALL_GP_KEY,
              GlobalPropertiesConstants.SHOULD_SEND_REMINDER_VIA_WHATSAPP_GP_KEY,
              ConfigConstants.CONSENT_CONTROL_KEY,
              ConfigConstants.DEFAULT_USER_TIMEZONE);

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
                "getExtensionsForCurrentUser", String.class, AppContextModel.class),
            AppFrameworkService.class.getMethod("getAllEnabledExtensions"),
            AppFrameworkService.class.getMethod("getAllEnabledExtensions", String.class)));

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

    Context.addAdvice(
        FlagDTOService.class,
        new ProjectBasedFilterAfterAdvice<>(
            new ProjectAssignmentProjectBasedFilter<>(Flag.class, FlagDTO::getUuid),
            FlagDTOService.class.getMethod("getAllEnabledFlags")));

    Context.addAdvice(
        PatientTemplateService.class,
        new ProjectBasedFilterAfterAdvice<>(
            new ProjectAssignmentProjectBasedFilter<>(
                Template.class,
                (PatientTemplate patientTemplate) -> patientTemplate.getTemplate().getUuid()),
            PatientTemplateService.class.getMethod("findAllByCriteria", BaseCriteria.class),
            PatientTemplateService.class.getMethod(
                "findAllByCriteria", BaseCriteria.class, PagingInfo.class)));

    Context.addAdvice(
        TemplateService.class,
        new ProjectBasedFilterAfterAdvice<>(
            new ProjectAssignmentProjectBasedFilter<>(Template.class, Template::getUuid),
            TemplateService.class.getMethod("findAllByCriteria", BaseCriteria.class),
            TemplateService.class.getMethod(
                "findAllByCriteria", BaseCriteria.class, PagingInfo.class)));

    Context.addAdvice(
        DefaultPatientTemplateService.class,
        new ProjectBasedFilterAfterAdvice<>(
            new ProjectAssignmentProjectBasedFilter<>(
                Template.class,
                (PatientTemplate patientTemplate) -> patientTemplate.getTemplate().getUuid()),
            DefaultPatientTemplateService.class.getMethod(
                "findLackingPatientTemplates", Patient.class),
            DefaultPatientTemplateService.class.getMethod(
                "findLackingPatientTemplates", Patient.class, List.class)));

    Context.addAdvice(
        VisitTimeService.class,
        new ProjectBasedFilterAfterAdvice<>(
            new ProjectAssignmentProjectBasedFilter<>(VisitTime.class, VisitTime::getUuid),
            VisitTimeService.class.getMethod("getAllVisitTimes", boolean.class)));

    Context.addAdvice(
        VisitStatusService.class,
        new ProjectBasedFilterAfterAdvice<>(
            new ProjectAssignmentProjectBasedFilter<>(VisitStatus.class, VisitStatus::getUuid),
            VisitStatusService.class.getMethod("getAllVisitStatuses", boolean.class)));

    Context.addAdvice(
        AdministrationService.class,
        new GlobalPropertyWithUserContextAdvice(
            GLOBAL_PROPERTIES_SUPPORTING_MULTI_PROJECT_WITH_USER_CONTEXT));

    Context.addAdvice(
        CustomAdministrationService.class,
        new GlobalPropertyWithoutUserContextAdvice(
            GLOBAL_PROPERTIES_SUPPORTING_MULTI_PROJECT_WITHOUT_USER_CONTEXT));

    Context.addAdvice(
        MessagesAdministrationService.class,
        new GlobalPropertyWithoutUserContextAdvice(
            GLOBAL_PROPERTIES_SUPPORTING_MULTI_PROJECT_WITHOUT_USER_CONTEXT));

    Context.addAdvisor(
        AppFrameworkService.class,
        new MultiProjectAroundAdvisor(AppFrameworkService.class.getMethod("getApp", String.class)));
  }
}
