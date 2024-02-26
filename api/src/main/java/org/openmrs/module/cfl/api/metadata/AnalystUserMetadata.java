/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.cfl.api.metadata;

import org.openmrs.Person;

public class AnalystUserMetadata extends AbstractUserMetadata {

  private static final String GIVEN_NAME = "CfL";
  private static final String FAMILY_NAME = "Analyst";
  private static final String EMAIL_VALUE = "cfl@cfl.xyz";
  private static final String PHONE_NUMBER_VALUE = "+15555551235";
  private static final String USERNAME = "cflanalyst1";
  private static final String PASSWORD = "Cflanalyst1";
  private static final String ANALYST_ROLE = "Privilege Level: Analyst";

  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  protected void installEveryTime() {
    // nothing to do
  }

  @Override
  protected void installNewVersion() {
    if (isUserDoesNotExist(USERNAME)) {
      setUpDefaultAnalystUser();
    }
  }

  private void setUpDefaultAnalystUser() {
    Person analyst = createAnalystPerson();
    createUserProvider(analyst, USERNAME);

    userService.createUser(createUserObject(analyst, USERNAME, ANALYST_ROLE), PASSWORD);
  }

  private Person createAnalystPerson() {
    Person doctor = new Person();
    doctor.setGender(OTHER_GENDER);
    doctor.setNames(createUserNames(doctor, GIVEN_NAME, FAMILY_NAME));
    doctor.setAttributes(createUserAttributes(doctor, EMAIL_VALUE, PHONE_NUMBER_VALUE));

    return personService.savePerson(doctor);
  }
}
