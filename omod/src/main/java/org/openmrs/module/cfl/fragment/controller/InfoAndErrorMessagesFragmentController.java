/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.cfl.fragment.controller;

import org.openmrs.module.cfl.CflWebConstants;
import org.openmrs.ui.framework.fragment.FragmentModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class InfoAndErrorMessagesFragmentController {
	
	public void controller(HttpServletRequest request, FragmentModel fragmentModel) {
		HttpSession session = request.getSession();
		String errorMessage = (String) session
		        .getAttribute(CflWebConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE);
		String infoMessage = (String) session.getAttribute(CflWebConstants.SESSION_ATTRIBUTE_INFO_MESSAGE);
		session.setAttribute(CflWebConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE, null);
		session.setAttribute(CflWebConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, null);
		fragmentModel.addAttribute("errorMessage", errorMessage);
		fragmentModel.addAttribute("infoMessage", infoMessage);
	}
	
}
