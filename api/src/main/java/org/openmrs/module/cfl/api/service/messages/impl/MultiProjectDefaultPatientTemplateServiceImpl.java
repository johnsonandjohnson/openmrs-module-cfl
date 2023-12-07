/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.cfl.api.service.messages.impl;

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.messages.api.builder.PatientTemplateBuilder;
import org.openmrs.module.messages.api.model.Actor;
import org.openmrs.module.messages.api.model.PatientTemplate;
import org.openmrs.module.messages.api.model.Template;
import org.openmrs.module.messages.api.service.ActorService;
import org.openmrs.module.messages.api.service.TemplateService;
import org.openmrs.module.messages.api.service.impl.DefaultPatientTemplateServiceImpl;
import org.openmrs.module.messages.api.util.DefaultPatientTemplateWrapper;
import org.openmrs.module.multiproject.Project;
import org.openmrs.module.multiproject.filter.CurrentProjectProvider;
import org.openmrs.module.multiproject.filter.ProjectAssignmentProjectBasedFilter;
import org.openmrs.module.multiproject.util.PersonProjectUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MultiProjectDefaultPatientTemplateServiceImpl extends DefaultPatientTemplateServiceImpl {
  @Override
  public List<PatientTemplate> findLackingPatientTemplates(
      Patient patient, List<PatientTemplate> existing) {
    List<DefaultPatientTemplateWrapper> actual = DefaultPatientTemplateWrapper.wrapToList(existing);
    List<DefaultPatientTemplateWrapper> expected =
        DefaultPatientTemplateWrapper.wrapToList(
            this.getProjectBasedPatientTemplatesWithDefaultValues(patient));
    Set<DefaultPatientTemplateWrapper> diff = new HashSet<>(expected);
    diff.removeAll(actual);
    return DefaultPatientTemplateWrapper.unwrapToList(diff);
  }

  private List<PatientTemplate> getProjectBasedPatientTemplatesWithDefaultValues(Patient patient) {
    List<Template> templates = getProjectBasedTemplates(patient);
    List<Actor> actors =
        Context.getService(ActorService.class).getAllActorsForPatientId(patient.getId());
    List<PatientTemplate> patientTemplates =
        new ArrayList<>(templates.size() + templates.size() * actors.size());

    for (Template template : templates) {
      patientTemplates.add((new PatientTemplateBuilder(template, patient)).build());

      for (Actor actor : actors) {
        patientTemplates.add((new PatientTemplateBuilder(template, actor, patient)).build());
      }
    }

    return patientTemplates;
  }

  private List<Template> getProjectBasedTemplates(Patient patient) {
    final List<Template> templates =
        new ArrayList<>(Context.getService(TemplateService.class).getAll(false));
    new ProjectAssignmentProjectBasedFilter<>(
            new PatientBasedProjectProvider(patient), Template.class, Template::getUuid)
        .doFilter(templates);
    return templates;
  }

  private static class PatientBasedProjectProvider implements CurrentProjectProvider {
    private final Optional<Project> project;

    PatientBasedProjectProvider(Patient patient) {
      project = PersonProjectUtils.getPersonProject(patient);
    }

    @Override
    public boolean isCurrentUserProjectSet() {
      return project.isPresent();
    }

    @Override
    public Optional<Project> getCurrentUserProject() throws APIException {
      return project;
    }
  }
}
