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

import org.openmrs.PersonAttributeType;
import org.openmrs.module.metadatadeploy.bundle.VersionedMetadataBundle;

/** Adds Person Attributes. */
public class PersonAttributesMetadata extends VersionedMetadataBundle {

  private static final String PERSON_ATTRIBUTE_TYPE_DESCRIPTION =
      "Used on CfL Patient registration form.";
  private double sortWeightGen = 0;

  @Override
  public int getVersion() {
    return 2;
  }

  @Override
  protected void installEveryTime() {
    // nothing to do
  }

  @Override
  protected void installNewVersion() {
    install(
        newPersonAttributeType(
            "personLanguage",
            "Attribute used to store information about patient language",
            "ff34db58-c1f4-475b-8bac-30f26e251333"));
    install(
        newPersonAttributeType(
            "dndConsent",
            "Attribute used to store information about patient dnd consent",
            "a5476c49-32d2-489e-a90b-d08be4b5cff9"));
    install(
        newPersonAttributeType(
            "Gender Identity",
            PERSON_ATTRIBUTE_TYPE_DESCRIPTION,
            "603bc838-ed52-4de5-b7c7-57a47de7a2ff"));
    install(
        newPersonAttributeType(
            "Nationality",
            PERSON_ATTRIBUTE_TYPE_DESCRIPTION,
            "1cf8b660-65a7-4335-b56d-672722182bc2"));
    install(
        newPersonAttributeType(
            "City of current residence",
            PERSON_ATTRIBUTE_TYPE_DESCRIPTION,
            "66119d21-4eda-466a-ac19-cadab568cf25"));
    install(
        newPersonAttributeType(
            "City of origin",
            PERSON_ATTRIBUTE_TYPE_DESCRIPTION,
            "03acac42-9c29-4007-9253-5226ac3e2b6d"));
    install(
        newPersonAttributeType(
            "Civil status",
            PERSON_ATTRIBUTE_TYPE_DESCRIPTION,
            "8d4a881a-e2fd-495c-b04f-d73f8a6e6146"));
    install(
        newPersonAttributeType(
            "Education degree",
            PERSON_ATTRIBUTE_TYPE_DESCRIPTION,
            "af032e9f-721e-4d2d-833a-5fc452e79e05"));
    install(
        newPersonAttributeType(
            "Sector",
            PERSON_ATTRIBUTE_TYPE_DESCRIPTION,
            "83208670-f43c-476f-a519-10a131d03c2d"));
    install(
        newPersonAttributeType(
            "Job",
            PERSON_ATTRIBUTE_TYPE_DESCRIPTION,
            "8dc7d8a5-3061-4894-8651-a7d35da3a8aa"));
    install(
        newPersonAttributeType(
            "Vaccination program",
            "Attribute used to store information about patient vaccination program",
            "1b903587-2843-4914-85db-a6a6a8ecfb5b"));
    install(
        newPersonAttributeType(
            "Telephone Number",
            "The telephone number for the person",
            "14d4f066-15f5-102d-96e4-000c29c2a5d7"));
    install(
        newPersonAttributeType(
            "Patient consent",
            "Attribute used to store information about patient consent",
            "b1df2b2e-1d55-4845-846d-6b34a0fad9f4"));
    install(
        newPersonAttributeType(
            "Unknown patient",
            "Used to flag patients that cannot be identified during the check-in process",
            "8b56eac7-5c76-4b9c-8c6f-1deab8d3fc47"));
  }

  private PersonAttributeType newPersonAttributeType(String name, String description, String uuid) {
    final PersonAttributeType type = new PersonAttributeType();
    type.setFormat(String.class.getName());
    type.setName(name);
    type.setDescription(description);
    type.setUuid(uuid);
    type.setSortWeight(sortWeightGen++);
    return type;
  }
}
