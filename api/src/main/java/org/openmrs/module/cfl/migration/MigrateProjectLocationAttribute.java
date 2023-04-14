/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.cfl.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.multiproject.Project;
import org.openmrs.module.multiproject.api.service.ProjectService;
import org.openmrs.module.multiproject.customdatatype.ProjectDatatype;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MigrateProjectLocationAttribute {
  private static final String ALL_BUT_SLUG_CHARS_REGEX = "[^a-z0-9\\-]+";
  private static final Log LOGGER = LogFactory.getLog(MigrateProjectLocationAttribute.class);

  private final ProjectService projectService;
  private final LocationService locationService;

  public MigrateProjectLocationAttribute() {
    this.projectService = Context.getService(ProjectService.class);
    this.locationService = Context.getLocationService();
  }

  public void doMigration() {
    final LocationAttributeType projectAttributeType =
        locationService.getLocationAttributeTypeByName("Project");

    if (projectAttributeType == null
        || projectAttributeType.getDatatypeClassname().equals(ProjectDatatype.class.getName())) {
      LOGGER.info("LocationAttribute with Project is correct, no migration needed.");
    } else {
      LOGGER.info(
          "Migrating LocationAttribute for Project, UUID: " + projectAttributeType.getUuid());
      updateProjectAttributeTypeDatatypeClass(projectAttributeType);
      migrateProjectAttributeValues(projectAttributeType);
    }
  }

  private void updateProjectAttributeTypeDatatypeClass(LocationAttributeType projectAttributeType) {
    projectAttributeType.setDatatypeClassname(ProjectDatatype.class.getName());
    locationService.saveLocationAttributeType(projectAttributeType);
  }

  private void migrateProjectAttributeValues(LocationAttributeType projectAttributeType) {
    final List<Location> locations = locationService.getAllLocations();
    final Map<String, Project> projects = getAllProjects();

    for (Location location : locations) {
      final Optional<LocationAttribute> projectAttribute =
          getActiveAttribute(location, projectAttributeType);

      if (projectAttribute.isPresent()) {
        final String projectName = projectAttribute.get().getValueReference();
        final Project attributeProject = projects.computeIfAbsent(projectName, this::createProject);
        projectAttribute.get().setValue(attributeProject);
        locationService.saveLocation(location);
      }
    }
  }

  private Optional<LocationAttribute> getActiveAttribute(
      Location location, LocationAttributeType projectAttributeType) {
    return location.getActiveAttributes(projectAttributeType).stream().findFirst();
  }

  private Project createProject(String name) {
    final Project project = new Project();
    project.setName(name);
    project.setSlug(name.toLowerCase().replaceAll(ALL_BUT_SLUG_CHARS_REGEX, "-"));
    return project;
  }

  private Map<String, Project> getAllProjects() {
    return projectService.getAllProjects().stream()
        .collect(Collectors.toMap(Project::getName, Function.identity()));
  }
}
