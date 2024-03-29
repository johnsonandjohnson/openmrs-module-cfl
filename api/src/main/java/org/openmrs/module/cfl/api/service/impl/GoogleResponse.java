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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"success", "score", "action", "challenge_ts", "hostname", "error-codes"})
public class GoogleResponse {

  @JsonProperty("success")
  private boolean success;

  @JsonProperty("challenge_ts")
  private String challengeTs;

  @JsonProperty("hostname")
  private String hostname;

  @JsonProperty("score")
  private float score;

  @JsonProperty("action")
  private String action;

  @JsonProperty("error-codes")
  private ErrorCode[] errorCodes;

  enum ErrorCode {
    MissingSecret,
    InvalidSecret,
    MissingResponse,
    InvalidResponse,
    BadRequest,
    TimeoutOrDuplicate;

    private static Map<String, ErrorCode> errorsMap = new HashMap<>(6);

    static {
      errorsMap.put("missing-input-secret", MissingSecret);
      errorsMap.put("invalid-input-secret", InvalidSecret);
      errorsMap.put("missing-input-response", MissingResponse);
      errorsMap.put("bad-request", BadRequest);
      errorsMap.put("invalid-input-response", InvalidResponse);
      errorsMap.put("timeout-or-duplicate", TimeoutOrDuplicate);
    }

    @JsonCreator
    public static ErrorCode forValue(final String value) {
      return errorsMap.get(value.toLowerCase());
    }
  }

  @JsonProperty("success")
  public boolean isSuccess() {
    return success;
  }

  @JsonProperty("success")
  public void setSuccess(boolean success) {
    this.success = success;
  }

  @JsonProperty("challenge_ts")
  public String getChallengeTs() {
    return challengeTs;
  }

  @JsonProperty("challenge_ts")
  public void setChallengeTs(String challengeTs) {
    this.challengeTs = challengeTs;
  }

  @JsonProperty("hostname")
  public String getHostname() {
    return hostname;
  }

  @JsonProperty("hostname")
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  @JsonProperty("error-codes")
  public void setErrorCodes(ErrorCode[] errorCodes) {
    this.errorCodes = Arrays.copyOf(errorCodes, errorCodes.length);
  }

  @JsonProperty("error-codes")
  public ErrorCode[] getErrorCodes() {
    return Arrays.copyOf(errorCodes, errorCodes.length);
  }

  @JsonProperty("score")
  public float getScore() {
    return score;
  }

  @JsonProperty("score")
  public void setScore(float score) {
    this.score = score;
  }

  @JsonProperty("action")
  public String getAction() {
    return action;
  }

  @JsonProperty("action")
  public void setAction(String action) {
    this.action = action;
  }

  @JsonIgnore
  public boolean hasClientError() {
    final ErrorCode[] errors = getErrorCodes();
    if (errors == null) {
      return false;
    }
    for (final ErrorCode error : errors) {
      switch (error) {
        case InvalidResponse:
        case MissingResponse:
        case BadRequest:
          return true;
        default:
          break;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "GoogleResponse{"
        + "success="
        + success
        + ", challengeTs='"
        + challengeTs
        + '\''
        + ", hostname='"
        + hostname
        + '\''
        + ", score='"
        + score
        + '\''
        + ", action='"
        + action
        + '\''
        + ", errorCodes="
        + Arrays.toString(errorCodes)
        + '}';
  }
}
