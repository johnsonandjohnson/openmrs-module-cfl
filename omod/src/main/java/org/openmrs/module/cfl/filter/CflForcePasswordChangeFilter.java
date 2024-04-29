/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of
 * the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * OpenMRS is also distributed under the terms of the Healthcare Disclaimer located at
 * http://openmrs.org/license.
 *
 * <p>Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a
 * trademark of OpenMRS Inc.
 */
package org.openmrs.module.cfl.filter;

import org.openmrs.api.context.Context;
import org.openmrs.web.user.UserProperties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * This filter checks if an authenticated user has been flagged by the admin to change his password
 * on first/subsequent login. It will intercept any requests made to a *.html or a *.form to force
 * the user to change his password.
 *
 * <p>It is intended to run before OpenMRS filter from legacy UI module which redirects to wrong
 * page.
 */
public class CflForcePasswordChangeFilter implements Filter {

  private static final String CHANGE_PASSWORD_ADMIN_UI_PAGE =
      "/adminui/myaccount/changePassword.page";
  private static final List<String> EXCLUDED_URIS =
      Arrays.asList(
          "changePasswordForm",
          "csrfguard",
          "logout",
          "logout.action",
          ".js",
          ".css",
          ".gif",
          ".jpg",
          ".jpeg",
          ".png");
  private static final List<String> LOGOUT_URIS = Arrays.asList("logout", "logout.action");
  private static final String SAVE_LOGOUT_URL = "/ms/logout";

  private FilterConfig config;

  @Override
  public void init(FilterConfig config) {
    this.config = config;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String requestURI = ((HttpServletRequest) request).getRequestURI();

    if (Context.isAuthenticated()
        && new UserProperties(Context.getAuthenticatedUser().getUserProperties())
            .isSupposedToChangePassword()
        && !endsWithOneOf(requestURI, EXCLUDED_URIS)) {
      config
          .getServletContext()
          .getRequestDispatcher(CHANGE_PASSWORD_ADMIN_UI_PAGE)
          .forward(request, response);
    } else if (endsWithOneOf(requestURI, LOGOUT_URIS)) {
      config.getServletContext().getRequestDispatcher(SAVE_LOGOUT_URL).forward(request, response);
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {}

  private boolean endsWithOneOf(String url, List<String> uris) {
    for (String uri : uris) {
      if (url.endsWith(uri)) {
        return true;
      }
    }
    return false;
  }
}
