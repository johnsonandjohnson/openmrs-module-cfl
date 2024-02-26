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

import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.module.metadatadeploy.bundle.VersionedMetadataBundle;
import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractUserMetadata extends VersionedMetadataBundle {

  protected static final String OTHER_GENDER = "O";

  protected static final String EMAIL_ATTRIBUTE_TYPE_NAME = "EmailAttribute";

  protected static final String PHONE_NUMBER_ATTRIBUTE_TYPE_NAME = "Telephone Number";

  protected static final String FORCE_PASSWORD_PROPERTY_NAME = "forcePassword";

  protected static final String LOCATION_UUID_PROPERTY_NAME = "locationUuid";

  protected static final String LOCATION_NAME = "CFL Clinic";

  protected UserService userService;

  protected PersonService personService;

  protected LocationService locationService;

  protected ProviderService providerService;

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  public void setLocationService(LocationService locationService) {
    this.locationService = locationService;
  }

  public void setProviderService(ProviderService providerService) {
    this.providerService = providerService;
  }

  protected boolean isUserDoesNotExist(String username) {
    return userService.getUserByUsername(username) == null;
  }

  protected Set<PersonName> createUserNames(Person person, String givenName, String familyName) {
    PersonName personName = new PersonName();
    personName.setGivenName(givenName);
    personName.setFamilyName(familyName);
    personName.setPerson(person);

    return Stream.of(personName).collect(Collectors.toCollection(TreeSet::new));
  }

  protected Set<PersonAttribute> createUserAttributes(
      Person person, String email, String phoneNumber) {
    return Stream.of(
            createPersonAttribute(person, EMAIL_ATTRIBUTE_TYPE_NAME, email),
            createPersonAttribute(person, PHONE_NUMBER_ATTRIBUTE_TYPE_NAME, phoneNumber))
        .collect(Collectors.toCollection(TreeSet::new));
  }

  protected void createUserProvider(Person person, String username) {
    Provider provider = new Provider();
    provider.setPerson(person);
    provider.setIdentifier(username);

    providerService.saveProvider(provider);
  }

  protected User createUserObject(Person person, String username, String role) {
    User user = new User();
    user.setUsername(username);
    user.setPerson(person);
    user.setSystemId(username);
    user.setUserProperties(createUserProperties());
    user.setRoles(Collections.singleton(userService.getRole(role)));

    return user;
  }

  private Map<String, String> createUserProperties() {
    Map<String, String> userProperties = new HashMap<>();
    userProperties.put(FORCE_PASSWORD_PROPERTY_NAME, "false");
    userProperties.put(LOCATION_UUID_PROPERTY_NAME, getDefaultLocation().getUuid());

    return userProperties;
  }

  private Location getDefaultLocation() {
    Location location = locationService.getLocation(LOCATION_NAME);
    if (location != null) {
      return location;
    }

    return locationService.getAllLocations().get(0);
  }

  private PersonAttribute createPersonAttribute(Person person, String attributeName, String value) {
    PersonAttributeType attributeType = personService.getPersonAttributeTypeByName(attributeName);
    if (attributeType == null) {
      throw new EntityNotFoundException(
          String.format("Person attribute type with name: %s not found", attributeName));
    }

    PersonAttribute personAttribute = new PersonAttribute();
    personAttribute.setPerson(person);
    personAttribute.setAttributeType(attributeType);
    personAttribute.setValue(value);

    return personAttribute;
  }
}
