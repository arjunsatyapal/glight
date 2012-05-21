/*
 * Copyright (C) Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.light.server.servlets.path;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;

import com.google.common.collect.Lists;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.servlets.LightGenericJSPServlet;
import com.google.light.server.servlets.admin.GSSConsumerCredentialServlet;
import com.google.light.server.servlets.admin.OAuth2ConsumerCredentialServlet;
import com.google.light.server.servlets.filters.FilterPathEnum;
import com.google.light.server.servlets.login.LoginServlet;
import com.google.light.server.servlets.login.LogoutServlet;
import com.google.light.server.servlets.oauth2.google.gdoc.GoogleDocAuthCallbackServlet;
import com.google.light.server.servlets.oauth2.google.gdoc.GoogleDocAuthServlet;
import com.google.light.server.servlets.oauth2.google.login.GoogleLoginCallbackServlet;
import com.google.light.server.servlets.oauth2.google.login.GoogleLoginServlet;
import com.google.light.server.servlets.person.PersonServlet;
import com.google.light.server.servlets.search.SearchServlet;
import com.google.light.server.servlets.test.TestCleanUpDatastore;
import com.google.light.server.servlets.test.TestHeaders;
import com.google.light.server.servlets.test.TestLogin;
import com.google.light.server.servlets.test.TestServlet;
import com.google.light.server.servlets.test.oauth2.TestCredentialBackupServlet;
import com.google.light.server.servlets.test.oauth2.login.FakeLoginServlet;
import java.util.List;
import javax.servlet.http.HttpServlet;

/**
 * Enum to Map Servlets with their Paths and URL Patterns.
 * 
 * TODO(arjuns) : Add tests for this.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("deprecation")
public enum ServletPathEnum {
  // TODO(arjuns): Add regex checks here.
  // TODO(arjuns) : Find a way to end URLs without /.

  // Admin
  GSS(GSSConsumerCredentialServlet.class,
      "/admin/gss_consumer_credential",
      true, true, true, true,
      Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  PERSON(PersonServlet.class, "/api/person",
         true, false, true, true,
         Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),
  // TODO(waltercacau): Fix this hack
  PERSONME(PersonServlet.class, "/api/person/me",
           true, false, true, true,
           Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  LOGIN(LoginServlet.class, "/login",
        false, false, true, true,
        Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),
  LOGOUT(LogoutServlet.class, "/logout",
         false, false, true, true,
         Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  // Third party integrations.
  SEARCH(SearchServlet.class, "/api/search",
         false, false, true, true,
         Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  // Client Side pages
  HOME_PAGE(LightGenericJSPServlet.class, "/",
            false, false, true, true,
            Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),
  REGISTER_PAGE(LightGenericJSPServlet.class, "/register",
                false, false, true, true,
                Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),
  TEST_PAGE(LightGenericJSPServlet.class, "/test",
            false, false, true, true,
            Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  // OAuth2 Related Servlets.
  OAUTH2_GOOGLE_LOGIN(GoogleLoginServlet.class, "/login/google",
                      false, false, true, true,
                      Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  OAUTH2_GOOGLE_LOGIN_CB(GoogleLoginCallbackServlet.class, "/login/google_login_callback",
                         false, false, true, true,
                         Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  OAUTH2_GOOGLE_DOC_AUTH(GoogleDocAuthServlet.class, "/oauth2/google_doc",
                         true, false, true, true,
                         Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  OAUTH2_GOOGLE_DOC_AUTH_CB(GoogleDocAuthCallbackServlet.class, "/oauth2/google_doc_cb",
                            true, false, true, true,
                            Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  // TODO(arjuns): Move this to jersey resource.
  OAUTH2_CONSUMER_CRENDENTIAL(OAuth2ConsumerCredentialServlet.class,
                              "/admin/oauth2_consumer_credential",
                              true, true, true, true,
                              Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),
  // Some test servlets.
  FAKE_LOGIN(FakeLoginServlet.class, "/test/fakelogin",
             false, false, false, true,
             Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  TEST_CREDENTIAL_BACKUP_SERVLET(TestCredentialBackupServlet.class,
                                 "/test/admin/test_credential_backup",
                                 false, false, false, true,
                                 Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  TEST_SERVLET(TestServlet.class, "/test/test",
               false, false, false, true,
               Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),
  TEST_CLEAN_UP_DATASTORE(TestCleanUpDatastore.class, "/test/admin/cleanupds",
                          true, true, false, true,
                          Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),

  TEST_HEADER(TestHeaders.class, "/test/testheader",
              false, false, false, true,
              Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST)),
  TEST_LOGIN(TestLogin.class, "/test/testlogin",
             true, false, false, true,
             Lists.newArrayList(FilterPathEnum.API, FilterPathEnum.TEST));

  private Class<? extends HttpServlet> clazz;
  private String servletPath;
  // Path when this servlet acts as root for others.
  private String servletRoot;

  private boolean requiresLogin;
  private boolean requiresAdminPrivilege;
  private boolean allowedInProd;
  private boolean allowedInNonProd;
  private List<FilterPathEnum> listOfFilters;

  private ServletPathEnum(Class<? extends HttpServlet> clazz, String servletPath,
      boolean requireLogin, boolean requireAdminPrivilege, boolean allowedInProd,
      boolean allowedInNonProd, List<FilterPathEnum> listOfFilters) {
    this.clazz = clazz;
    this.servletPath = checkNotBlank(servletPath, "servletPath");
    if ("/".equals(servletPath)) {
      this.servletRoot = "/";
    } else {
      checkArgument(!servletPath.endsWith("/"));
      this.servletRoot = checkNotBlank(servletPath + "/", "servletPath/");
    }
    this.requiresLogin = requireLogin;

    if (!requireLogin && requireAdminPrivilege) {
      throw new IllegalStateException("For AdminPrivileges, Login is mandatory.");
    }
    this.requiresAdminPrivilege = requireAdminPrivilege;

    this.allowedInProd = allowedInProd;
    this.allowedInNonProd = allowedInNonProd;
    this.listOfFilters = checkNotEmptyCollection(listOfFilters, "listOfFilters.");
  }

  public Class<? extends HttpServlet> getClazz() {
    return clazz;
  }

  public boolean isRequiredLogin() {
    return requiresLogin;
  }

  public boolean isRequiredAdminPrivilege() {
    return requiresAdminPrivilege;
  }

  public boolean isAllowedInCurrEnv() {
    LightEnvEnum env = LightEnvEnum.getLightEnv();

    switch (env) {
      case PROD:
        return allowedInProd;

      case DEV_SERVER:
        //$FALL-THROUGH$
      case QA:
        //$FALL-THROUGH$
      case UNIT_TEST:
        return allowedInNonProd;

      default:
        throw new IllegalStateException("Unsupported env : " + env);
    }

  }

  public String get() {
    return servletPath;
  }

  public String getRoot() {
    return servletRoot;
  }

  public List<FilterPathEnum> getListOfFilters() {
    return listOfFilters;
  }

  // TODO(arjuns): Add test for this.
  public static ServletPathEnum getServletPathEnum(String requestUri) {
    String uri =
        requestUri.endsWith("/") && !"/".equals(requestUri) ? requestUri.substring(0,
            requestUri.length() - 1)
            : requestUri;

    for (ServletPathEnum currServletPath : ServletPathEnum.values()) {
      if (currServletPath.get().equals(uri)) {
        return currServletPath;
      }
    }

    throw new NotFoundException("ServletPath not found for servletPath[" + requestUri
        + "].");
  }
}
