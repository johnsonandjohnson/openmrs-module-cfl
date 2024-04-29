/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.cfl.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.openmrs.api.context.Context;
import org.openmrs.module.cfl.CfldistributionGlobalParameterConstants;
import org.openmrs.module.cfl.api.service.CaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Pattern;

public class CaptchaServiceImpl implements CaptchaService {

  private static final Pattern RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

  private static final Logger LOGGER = LoggerFactory.getLogger(CaptchaServiceImpl.class);

  private static final String RECAPTCHA_URL_TEMPLATE =
      "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s";

  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  private ReCaptchaAttemptService reCaptchaAttemptService;

  public CaptchaServiceImpl() {
    this(new HttpClient(new MultiThreadedHttpConnectionManager()));
  }

  CaptchaServiceImpl(HttpClient httpClient) {
    this.objectMapper = new ObjectMapper();
    this.httpClient = httpClient;
  }

  @Override
  public void processResponse(HttpServletRequest request) {
    final String response = request.getParameter("g-recaptcha-response");
    securityCheck(response, request);

    final String verifyUri =
        String.format(RECAPTCHA_URL_TEMPLATE, getReCaptchaSecret(), response, getClientIP(request));

    try {
      final HttpMethod getGoogleResponseMethod = new GetMethod(verifyUri);
      httpClient.executeMethod(getGoogleResponseMethod);

      final GoogleResponse googleResponse =
          objectMapper.readValue(
              getGoogleResponseMethod.getResponseBodyAsString(), GoogleResponse.class);
      LOGGER.debug("Google's response: {} ", googleResponse);

      if (!googleResponse.isSuccess()) {
        if (googleResponse.hasClientError()) {
          reCaptchaAttemptService.reCaptchaFailed(getClientIP(request));
        }
        throw new ReCaptchaInvalidException("reCaptcha was not successfully validated");
      }
    } catch (IOException exception) {
      throw new ReCaptchaInvalidException(
          "Login unavailable at this time.  Please try again later.", exception);
    }
    reCaptchaAttemptService.reCaptchaSucceeded(getClientIP(request));
  }

  private String getClientIP(HttpServletRequest request) {
    final String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader == null) {
      return request.getRemoteAddr();
    }
    return xfHeader.split(",")[0];
  }

  private String getReCaptchaSecret() {
    return Context.getAdministrationService()
        .getGlobalProperty(CfldistributionGlobalParameterConstants.GOOGLE_RECAPTCHA_SECRET_KEY);
  }

  private boolean responseSanityCheck(String response) {
    return StringUtils.hasLength(response) && RESPONSE_PATTERN.matcher(response).matches();
  }

  public void setReCaptchaAttemptService(ReCaptchaAttemptService reCaptchaAttemptService) {
    this.reCaptchaAttemptService = reCaptchaAttemptService;
  }

  private void securityCheck(String response, HttpServletRequest request) {
    if (!responseSanityCheck(response)) {
      throw new ReCaptchaInvalidException("Response contains invalid characters");
    }
    if (reCaptchaAttemptService.isBlocked(getClientIP(request))) {
      throw new ReCaptchaInvalidException("Client exceeded maximum number of failed attempts");
    }
  }
}
