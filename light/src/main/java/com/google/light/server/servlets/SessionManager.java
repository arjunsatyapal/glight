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
import static com.google.light.server.constants.RequestParamKeyEnum.DEFAULT_EMAIL;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_USER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.PERSON_ID;
import static com.google.light.server.utils.GuiceUtils.seedEntityInRequestScope;
import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;
import static com.google.light.server.utils.LightPreconditions.checkPersonKey;
import static com.google.light.server.utils.ServletUtils.getRequestHeaderValue;
import static org.apache.commons.lang.StringUtils.isBlank;

import com.google.inject.Inject;
import com.google.light.server.annotations.AnotActor;
import com.google.light.server.annotations.AnotHttpSession;
import com.google.light.server.annotations.AnotOwner;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.pojo.PersonId;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.googlecode.objectify.Key;
import javax.servlet.http.HttpServletRequest;
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
  private PersonEntity personEntity;
  
  @Inject
  public SessionManager(@AnotHttpSession HttpSession session) {
    this.session = session;
  }
  
  /**
   * Get Person's Login Provider from Session.
   * 
   * @return
   */
  @Deprecated
  public OAuth2ProviderService getLoginProviderService() {
    if (session != null) {
      String loginProviderStr = (String) session.getAttribute(LOGIN_PROVIDER_ID.get());
      return OAuth2ProviderService.valueOf(loginProviderStr);
    } else {
      return null;
    }
  }

  /**
   * Store Person's Login Provider in session.
   */
  @Deprecated
  public void setLoginProviderService(OAuth2ProviderService providerService) {
    session.setAttribute(LOGIN_PROVIDER_ID.get(), providerService.name());
  }
  
  /**
   * Get Person's Login Provider User Id.
   * TODO(arjuns): Add test.
   * 
   * @return
   */
  @Deprecated
  public String getLoginProviderUserId() {
    if (session != null) {
      String providerUserId = (String) session.getAttribute(LOGIN_PROVIDER_USER_ID.get());
      return checkNotBlank(providerUserId, "providerUserId");
    } else {
      return null;
    }
  }

  /**
   * Store Person's Login Provider in session.
   * TODO(arjuns): Add test
   */
  @Deprecated
  public void setLoginProviderUserId(String providerUserId) {
    session.setAttribute(LOGIN_PROVIDER_USER_ID.get(), 
        checkNotBlank(providerUserId, "providerUserId"));
  }

  /**
   * Get Person's email provided by LoginProvider from Session.
   * 
   * @return
   */
  @Deprecated
  public String getEmail() {
    if (session != null) {
      String email = (String) session.getAttribute(DEFAULT_EMAIL.get());
      return checkEmail(email);
    } else {
      return personEntity.getEmail();
    }
  }

  /**
   * Store Person's email in sessin.
   * 
   * @return
   */
  @Deprecated
  public void setProviderUserEmail(String email) {
    session.setAttribute(DEFAULT_EMAIL.get(), checkEmail(email));
  }

//  /**
//   * Store Person's UserId provided by LoginProvider in session.
//   * 
//   * @return
//   */
//  @Deprecated
//  public void setPersonId(Long id) {
//    session.setAttribute(PERSON_ID.get(), checkPersonId(id));
//  }

  /**
   * Get PersonId from Session.
   * 
   * @return
   */

  public PersonId getPersonId() {
    if (session != null) {
      checkPersonLoggedIn();
      PersonId personId = (PersonId) session.getAttribute(PERSON_ID.get());
      return checkPersonId(personId);
    } else {
      return personEntity.getPersonId();
    }
  }
  
  @Deprecated
  public Key<PersonEntity> getPersonKey() {
    Key<PersonEntity> personKey = PersonEntity.generateKey(getPersonId());
    return checkPersonKey(personKey);
  }

  /**
   * Returns true if Person is logged in. In order to determine if User is logged in or not, we
   * depend on whether Session is null or not.
   * 
   * TODO(arjuns): Add test.
   * @return
   */
  @Deprecated
  public boolean isPersonLoggedIn() {
    if (session == null) {
      return false;
    }
    
    try {
      return !isBlank(getLoginProviderUserId());
    } catch (BlankStringException e) {
      return false;
    }
  }

  /**
   * Ensures that Person is Logged In.
   * 
   * TODO(arjuns): Add test for this.
   */
  @Deprecated
  public void checkPersonLoggedIn() {
    if (!isPersonLoggedIn()) {
      throw new PersonLoginRequiredException("");
    }
  }
  

  // TODO(arjuns): Add tests.
  /**
   * Returns true if session is valid.
   * 
   * @return
   */
  @Deprecated
  public boolean isValidSession() {
    try {
      if (session != null) {
        checkNotNull(getLoginProviderService(), "providerService");
        checkNotBlank(getLoginProviderUserId(), "providerUserId");
      }
      
      checkNotBlank(getEmail(), "email");
      checkNotNull(getPersonId(), "personId");
    } catch (Exception e) {
      return false;
    }

    return true;
  }
  
  public void seedPersonIds(HttpServletRequest request) {
    if (isPersonLoggedIn()) {
      PersonId performerId = getPersonId();
      seedEntityInRequestScope(request, PersonId.class, AnotOwner.class, performerId);
      
      String watcherIdStr = getRequestHeaderValue(request, HttpHeaderEnum.LIGHT_WATCHER_HEADER);
      if (watcherIdStr != null) {
        seedEntityInRequestScope(request, PersonId.class, AnotActor.class, new PersonId(watcherIdStr));
      } else {
        // Forcing both watcher and performer to be same.
        seedEntityInRequestScope(request, PersonId.class, AnotActor.class, performerId);
      }
    }
  }
}
