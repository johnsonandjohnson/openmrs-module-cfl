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

import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.patientflags.Tag;
import org.openmrs.module.patientflags.api.FlagService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MigrateUnnecessaryRoles {

  private static final List<String> ROLES_TO_REMOVE = Collections.singletonList("Super Admin");

  public void doMigration() {
    removeUnnecessaryRolesFromFlagTags();
    removeUnnecessaryUserRoles();
  }

  private void removeUnnecessaryRolesFromFlagTags() {
    FlagService flagService = Context.getService(FlagService.class);
    for (Tag tag : flagService.getAllTags()) {
      Set<Role> roles = tag.getRoles();
      roles.removeIf(role -> ROLES_TO_REMOVE.contains(role.getRole()));
      flagService.saveTag(tag);
    }
  }

  private void removeUnnecessaryUserRoles() {
    UserService userService = Context.getUserService();
    for (String roleToRemove : ROLES_TO_REMOVE) {
      Role role = userService.getRole(roleToRemove);
      if (role != null) {
        List<User> users = userService.getUsersByRole(role);
        for (User user : users) {
          user.getRoles().removeIf(userRole -> ROLES_TO_REMOVE.contains(userRole.getRole()));
          userService.saveUser(user);
        }
        userService.purgeRole(role);
      }
    }
  }
}
