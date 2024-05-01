/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.cfl;

public final class CfldistributionGlobalParameterConstants {
  public static final String CFL_DISTRO_BOOTSTRAPPED_KEY = "cfl.distro.bootstrapped";
  public static final String CFL_DISTRO_BOOTSTRAPPED_DEFAULT_VALUE = "false";
  public static final String CFL_DISTRO_BOOTSTRAPPED_DEFAULT_DESCRIPTION =
      "The global property which indicates that CFL"
          + " distribution has been installed and all 'run only once' updates has been executed.";

  public static final String SHOULD_DISABLE_APPS_AND_EXTENSIONS_KEY =
      "cfl.shouldDisableAppsAndExtensions";
  public static final String SHOULD_DISABLE_APPS_AND_EXTENSIONS_DEFAULT_VALUE = "true";
  public static final String SHOULD_DISABLE_APPS_AND_EXTENSIONS_DESCRIPTION =
      "Used to determine if the module should disable "
          + "specified apps and extensions on module startup. Possible values: true/false. Note: the server need "
          + "to "
          + "be restart after change this GP and in order to revert those changes you need to manually clean the "
          + "appframework_component_state table";

  public static final String GOOGLE_RECAPTCHA_SITE_KEY = "google.recaptcha.site.key";
  public static final String GOOGLE_RECAPTCHA_SITE_VALUE = "";
  public static final String GOOGLE_RECAPTCHA_SITE_DESCRIPTION =
      "Used to set Google Recaptcha Site Key";

  public static final String GOOGLE_RECAPTCHA_SECRET_KEY = "google.recaptcha.secret.key";
  public static final String GOOGLE_RECAPTCHA_SECRET_VALUE = "";
  public static final String GOOGLE_RECAPTCHA_SECRET_DESCRIPTION =
      "Used to set Google Recaptcha Secret Key";

  public static final String CAPTCHA_ENABLE_KEY = "cfl.captchaEnable";
  public static final String CAPTCHA_ENABLED_DEFAULT_VALUE = "false";
  public static final String CAPTCHA_ENABLED_DESCRIPTION =
      "Set to true to Enable Captcha and to false to disable";

  public static final String GOOGLE_RECAPTCHA_MAX_FAILED_ATTEMPTS_KEY =
      "google.recaptcha.max.failed.attempts";
  public static final String GOOGLE_RECAPTCHA_MAX_FAILED_ATTEMPTS_DEFAULT_VALUE = "4";
  public static final String GOOGLE_RECAPTCHA_MAX_FAILED_ATTEMPTS_DESCRIPTION =
      "Maximum Failed Attempts Allowed Before Captcha Gets Blocked";

  public static final String CFL_LOCATION_ATTRIBUTE_TYPE_UUID =
      "0a93cbc6-5d65-4886-8091-47a25d3df944";

  public static final String CFL_TELEPHONE_NUMBER_PERSON_ATTRIBUTE_TYPE_UUID =
      "14d4f066-15f5-102d-96e4-000c29c2a5d7";

  public static final String CFL_EMAIL_ADDRESS_PERSON_ATTRIBUTE_TYPE_UUID =
      "58cb9e76-75a1-4f49-956f-4ff6d0e02312";

  public static final String REGISTRATIONCORE_IDENTIFIER_SOURCE_ID_KEY =
      "registrationcore.identifierSourceId";

  public static final String CFL_SHOW_STACKTRACE_IN_ERROR_PAGE_KEY =
      "cfl.showStackTraceInErrorPage";
  public static final String CFL_SHOW_STACKTRACE_IN_ERROR_PAGE_DESCRIPTION =
      "[true,false] whether the error pages "
          + "should show exception stack traces. For security reasons, stack traces on error pages should be hidden in "
          + "productive environments. Default: false";
  public static final String CFL_SHOW_STACKTRACE_IN_ERROR_PAGE_DEFAULT_VALUE = "false";

  public static final String CONDITIONS_LIST_NON_CODED_UUID_KEY = "conditionList.nonCodedUuid";
  public static final String CONDITIONS_LIST_NON_CODED_UUID_DESC = "UUID of non coded concept";
  public static final String GENERIC_OTHER_NON_CODED_CONCEPT_UUID =
      "5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

  public static final String HTML_FORM_DATE_FORMAT_KEY = "htmlformentry.dateFormat";
  public static final String HTML_FORM_DATE_FORMAT_DESC =
      "Always display dates in HTML Forms in this (Java) date format. E.g. \"dd/MMM/yyyy\" for 31/Jan/2012.";
  public static final String DEFAULT_CFL_HTML_FORM_DATE_FORMAT = "dd/MM/yyyy";

  public static final String ROLE_UUIDS_EXCLUDED_FROM_UI_KEY = "cfl.rolesExcludedFromUI";

  public static final String ROLE_UUIDS_EXCLUDED_FROM_UI_DESC =
      "Comma separated list of user role UUIDS that should be hidden in UI selects with roles";

  public static final String ROLE_UUIDS_EXCLUDED_FROM_UI_DEFAULT_VALUE =
      "774b2af3-6437-4e5a-a310-547554c7c65c,f7fd42ef-880e-40c5-972d-e4ae7c990de2,ab2160f6-0941-430c-9752-6714353fbd3c,"
          + "f089471c-e00b-468e-96e8-46aea1b339af,8d94f280-c2cc-11de-8d13-0010c6dffd0f,922c166d-0a39-47bc-a6a3-d402134e1291";

  private CfldistributionGlobalParameterConstants() {}
}
