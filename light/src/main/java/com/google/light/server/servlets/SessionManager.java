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

import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_USER_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_USER_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.PERSON_ID;
import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import com.google.inject.Inject;
import com.google.light.server.annotations.AnotHttpSession;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;
import javax.servlet.http.HttpSession;

/**
 * Class to wrap functions around sessions. This class should be injected in LightScope.
 * 
 * TODO(arjuns): Update tests.
 * 
 * @author Arjun Satyapal
 */
public class SessionManager {
  private HttpSession session;

  @Inject
  public SessionManager(@AnotHttpSession HttpSession session) {
    this.session = session;
  }

  /**
   * Get Person's Login Provider from Session.
   * 
   * @return
   */
  public OAuth2Provider getLoginProvider() {
    String loginProviderStr = (String) session.getAttribute(LOGIN_PROVIDER_ID.get());
    return OAuth2Provider.valueOf(loginProviderStr);
  }
  
  /**
   * Store Person's Login Provider in session.
   */
  public void setLoginProvider(OAuth2Provider provider) {
    session.setAttribute(LOGIN_PROVIDER_ID.get(), provider.name());
  }

  /**
   * Get Person's email provided by LoginProvider from Session.
   * 
   * @return
   */
  public String getLoginProviderUserEmail() {
    String email = (String) session.getAttribute(LOGIN_PROVIDER_USER_EMAIL.get());
    return checkEmail(email);
  }

  /**
   * Store Person's email in sessin.
   * 
   * @return
   */
  public void setProviderUserEmail(String email) {
    session.setAttribute(LOGIN_PROVIDER_USER_EMAIL.get(), checkEmail(email));
  }

  /**
   * Get Person's UserId provided by LoginProvider from Session.
   * 
   * @return
   */
  public String getLoginProviderUserId() {
    String userId = (String) session.getAttribute(LOGIN_PROVIDER_USER_ID.get());
    return checkNotBlank(userId);
  }

  /**
   * Store Person's UserId provided by LoginProvider in session.
   * 
   * @return
   */
  public void setLoginProviderUserId(String id) {
    session.setAttribute(LOGIN_PROVIDER_USER_ID.get(), checkNotBlank(id));
  }

  /**
   * Get PersonId from Session.
   * 
   * @return
   */
  public Long getPersonId() {
    checkPersonLoggedIn();
    Long personId = (Long) session.getAttribute(PERSON_ID.get());
    return checkPersonId(personId);
  }

  /**
   * Store PersonId in Session.
   * 
   * @return
   */
  public void setPersonId(Long personId) {
    session.setAttribute(PERSON_ID.get(), checkPersonId(personId));
  }

  /**
   * Returns true if Person is logged in. In order to determine if User is logged in or not, 
   * we depend on whether Session is null or not.
   * 
   * @return
   */
  public boolean isPersonLoggedIn() {
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
