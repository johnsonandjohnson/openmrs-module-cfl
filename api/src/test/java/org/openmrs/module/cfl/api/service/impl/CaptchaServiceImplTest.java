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
import org.apache.commons.httpclient.HttpMethodUtils;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.cfl.CfldistributionGlobalParameterConstants;
import org.openmrs.test.BaseContextMockTest;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaptchaServiceImplTest extends BaseContextMockTest {
  @Mock private AdministrationService administrationServiceMock;
  @Mock private HttpServletRequest httpServletRequestMock;
  @Mock private ReCaptchaAttemptService reCaptchaAttemptServiceMock;
  @Mock private HttpClient httpClient;

  @Test(expected = ReCaptchaInvalidException.class)
  public void shouldFailIfRecaptchaResponseParameterContainsNonAlphanumericCharacters() {
    when(httpServletRequestMock.getParameter("g-recaptcha-response")).thenReturn("@/*");

    final CaptchaServiceImpl service = new CaptchaServiceImpl();

    service.processResponse(httpServletRequestMock);
  }

  @Test(expected = ReCaptchaInvalidException.class)
  public void shouldFailIfThereWasTooManyAttemptsForIPAddress() {
    final String blockedIp = "127.0.0.2";

    when(httpServletRequestMock.getParameter("g-recaptcha-response")).thenReturn("1234");
    when(httpServletRequestMock.getRemoteAddr()).thenReturn(blockedIp);
    when(reCaptchaAttemptServiceMock.isBlocked(blockedIp)).thenReturn(Boolean.TRUE);

    final CaptchaServiceImpl service = new CaptchaServiceImpl();
    service.setReCaptchaAttemptService(reCaptchaAttemptServiceMock);

    service.processResponse(httpServletRequestMock);
  }

  @Test(expected = ReCaptchaInvalidException.class)
  public void shouldFailIfResponseIsNotSuccess() throws IOException {
    final String anyIp = "127.0.0.2";

    when(administrationServiceMock.getGlobalProperty(
            CfldistributionGlobalParameterConstants.GOOGLE_RECAPTCHA_SECRET_KEY))
        .thenReturn("ac3282db-146b-4d42-87ef-45a483d07b9e");
    contextMockHelper.setAdministrationService(administrationServiceMock);

    when(httpServletRequestMock.getParameter("g-recaptcha-response")).thenReturn("1234");
    when(httpServletRequestMock.getRemoteAddr()).thenReturn(anyIp);
    when(reCaptchaAttemptServiceMock.isBlocked(anyIp)).thenReturn(Boolean.FALSE);

    final GoogleResponse googleResponse = new GoogleResponse();
    googleResponse.setSuccess(false);
    googleResponse.setErrorCodes(new GoogleResponse.ErrorCode[0]);
    final String googleResponseJSON = new ObjectMapper().writeValueAsString(googleResponse);

    when(httpClient.executeMethod(any(GetMethod.class)))
        .thenAnswer(
            invocationOnMock -> {
              final GetMethod getMethod = (GetMethod) invocationOnMock.getArguments()[0];
              HttpMethodUtils.setResponseStream(
                  getMethod, new ByteArrayInputStream(googleResponseJSON.getBytes()));
              return null;
            });

    final CaptchaServiceImpl service = new CaptchaServiceImpl(httpClient);
    service.setReCaptchaAttemptService(reCaptchaAttemptServiceMock);

    service.processResponse(httpServletRequestMock);
  }

  @Test
  public void shouldMarkValidationAsSuccess() throws IOException {
    final String anyIp = "127.0.0.2";

    when(administrationServiceMock.getGlobalProperty(
            CfldistributionGlobalParameterConstants.GOOGLE_RECAPTCHA_SECRET_KEY))
        .thenReturn("ac3282db-146b-4d42-87ef-45a483d07b9e");
    contextMockHelper.setAdministrationService(administrationServiceMock);

    when(httpServletRequestMock.getParameter("g-recaptcha-response")).thenReturn("1234");
    when(httpServletRequestMock.getRemoteAddr()).thenReturn(anyIp);
    when(reCaptchaAttemptServiceMock.isBlocked(anyIp)).thenReturn(Boolean.FALSE);

    final GoogleResponse googleResponse = new GoogleResponse();
    googleResponse.setSuccess(true);
    googleResponse.setErrorCodes(new GoogleResponse.ErrorCode[0]);
    final String googleResponseJSON = new ObjectMapper().writeValueAsString(googleResponse);

    when(httpClient.executeMethod(any(GetMethod.class)))
        .thenAnswer(
            invocationOnMock -> {
              final GetMethod getMethod = (GetMethod) invocationOnMock.getArguments()[0];
              HttpMethodUtils.setResponseStream(
                  getMethod, new ByteArrayInputStream(googleResponseJSON.getBytes()));
              return null;
            });

    final CaptchaServiceImpl service = new CaptchaServiceImpl(httpClient);
    service.setReCaptchaAttemptService(reCaptchaAttemptServiceMock);

    service.processResponse(httpServletRequestMock);

    verify(reCaptchaAttemptServiceMock).reCaptchaSucceeded(anyIp);
  }
}
