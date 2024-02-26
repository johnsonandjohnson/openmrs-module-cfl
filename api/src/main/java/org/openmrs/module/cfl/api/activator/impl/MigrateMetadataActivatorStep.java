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
import org.openmrs.module.cfl.api.activator.ModuleActivatorStep;
import org.openmrs.module.cfl.migration.MigrateProjectLocationAttribute;
import org.openmrs.module.cfl.migration.MigrateUnnecessaryRoles;

/** Automatically migrates Metadata. */
public class MigrateMetadataActivatorStep implements ModuleActivatorStep {

  @Override
  public int getOrder() {
    return ModuleActivatorStepOrderEnum.MIGRATE_METADATA.ordinal();
  }

  @Override
  public void startup(Log log) {
    new MigrateProjectLocationAttribute().doMigration();
    new MigrateUnnecessaryRoles().doMigration();
  }
}
