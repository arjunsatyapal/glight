/*
 * Copyright (C) Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.light.server.servlets;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OpenIdAuthDomain.getAuthDomainByValue;
import static com.google.light.server.constants.RequestParmKeyEnum.AUTH_DOMAIN;
import static com.google.light.server.constants.RequestParmKeyEnum.GAE_USER_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.GAE_USER_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.PERSON_ID;
import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Inject;
import com.google.light.server.annotations.AnotHttpSession;
import com.google.light.server.constants.LightAppIdEnum;
import com.google.light.server.constants.OpenIdAuthDomain;
import com.google.light.server.constants.RequestParmKeyEnum;
import javax.servlet.http.HttpSession;

/**
 * Class to wrap functions around sessions. This class should be injected in LightScope.
 * 
 * @author Arjun Satyapal
 */
public class SessionManager {
  private HttpSession session;

  @Inject
  public SessionManager(@AnotHttpSession HttpSession session) {
    this.session = checkNotNull(session);
  }

  /**
   * Get AuthDomain for user from Session. TODO(arjuns): Figure out what needs to be set for
   * Prod/test.
   * 
   * @return
   */
  public OpenIdAuthDomain getAuthDomain() {
    String authDomain = checkNotBlank((String) session.getAttribute(AUTH_DOMAIN.get()),
        "authDomain is blank.");
    return getAuthDomainByValue(authDomain);
  }

  /**
   * Get AuthDomain for Current Logged in user from Session.This should be called only when user is
   * Logged in.
   * 
   * @return
   */
  public void setAuthDomain(OpenIdAuthDomain domain) {
    checkNotNull(domain);
    session.setAttribute(AUTH_DOMAIN.get(), domain.get());
  }

  /**
   * Get User Email from Session.
   * 
   * @return
   */
  public String getGaeEmail() {
    String email = (String) session.getAttribute(RequestParmKeyEnum.GAE_USER_EMAIL.get());
    return checkEmail(email);
  }

  /**
   * Set User Email from Session.
   * 
   * @return
   */
  public void setUserEmail(String email) {
    session.setAttribute(GAE_USER_EMAIL.get(), checkEmail(email));
  }

  /**
   * Get GaeUserId from Session.
   * 
   * @return
   */
  public String getGaeUserId() {
    String userId = (String) session.getAttribute(GAE_USER_ID.get());
    return checkNotBlank(userId);
  }

  /**
   * Set GaeUserId from Session.
   * 
   * @return
   */
  public void setGaeUserId(String id) {
    session.setAttribute(GAE_USER_ID.get(), checkNotBlank(id));
  }

  /**
   * Get PersonId from Session.
   * 
   * @return
   */
  public Long getPersonId() {
    Long personId = (Long) session.getAttribute(PERSON_ID.get());
    return checkPersonId(personId);
  }

  /**
   * Set PersonId to Session.
   * 
   * @return
   */
  public void setPersonId(Long personId) {
    session.setAttribute(PERSON_ID.get(), checkPersonId(personId));
  }

  /**
   * Returns true if Person is logged in.
   * 
   * @return
   */
  public boolean isPersonLoggedIn() {
    if (LightAppIdEnum.PROD == LightAppIdEnum.getLightAppIdEnum()) {
      return UserServiceFactory.getUserService().isUserLoggedIn();
    }

    // For test/qa rely on session. If session is present, user is logged in.
    return session != null;
  }

  /**
   * Ensures that Person is Logged In.
   * 
   * TODO(arjuns): Add test for this.
   */
  public void checkPersonLoggedIn() {
    if (!isPersonLoggedIn()) {
      throw new PersonLoginRequiredException("");
    }
  }
}
