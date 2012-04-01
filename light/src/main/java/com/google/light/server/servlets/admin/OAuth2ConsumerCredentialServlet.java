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
package com.google.light.server.servlets.admin;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.RequestParmKeyEnum.CLIENT_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.CLIENT_SECRET;
import static com.google.light.server.constants.RequestParmKeyEnum.OAUTH2_PROVIDER_NAME;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonIsGaeAdmin;

import com.google.inject.Inject;
import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.dto.admin.OAuth2ConsumerCredentialDto;
import com.google.light.server.manager.interfaces.AdminOperationManager;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle configuration for Light.
 * 
 * TODO(arjuns) : Add test for this.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class OAuth2ConsumerCredentialServlet extends HttpServlet {
  private AdminOperationManager adminOperationManager;

  @Inject
  public OAuth2ConsumerCredentialServlet(AdminOperationManager adminOperationManager) {
    this.adminOperationManager = checkNotNull(adminOperationManager);
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    checkPersonIsGaeAdmin();
    super.service(request, response);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      List<OAuth2ConsumerCredentialEntity> list =
          adminOperationManager.getAllOAuth2ConsumerCredentials();

      StringBuilder builder = new StringBuilder("<b> OAuth2 Provider List.</b><p>");

      builder.append("<table border=\"1\">");
      builder.append("<tr>");
      builder.append("<td>Provider Name</td>");
      builder.append("<td>Client Id</td>");
      builder.append("<td>Client Secret</td>");
      builder.append("<tr>");

      for (OAuth2ConsumerCredentialEntity curr : list) {
        builder.append("<tr>");
        builder.append("<td>").append(curr.getOAuth2Provider()).append("</td>");
        builder.append("<td>").append(curr.getClientId()).append("</td>");
        builder.append("<td>").append(curr.getClientSecret()).append("</td>");
        builder.append("</tr>");
      }
      builder.append("</table>");

      response.setContentType(ContentTypeEnum.TEXT_HTML.get());
      response.getWriter().println(builder.toString());
    } catch (IOException e) {
      // TODO(arjuns): Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc} Since HTML Forms do not support PUT, so forwarding the request to PUT. PUT is
   * used in order to follow the semantics that for a given {@link OAuth2ProviderService}, there will
   * be
   * only
   * 
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    doPut(request, response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    String oauth2ProviderKey = checkNotBlank(
        request.getParameter(OAUTH2_PROVIDER_NAME.get()), "providerName");
    OAuth2ProviderEnum provider = OAuth2ProviderEnum.valueOf(oauth2ProviderKey);

    String clientId = checkNotBlank(request.getParameter(CLIENT_ID.get()), "clientId");
    String clientSecret = checkNotBlank(request.getParameter(CLIENT_SECRET.get()), "clientSecret");

    OAuth2ConsumerCredentialDto dto = new OAuth2ConsumerCredentialDto.Builder()
        .provider(provider)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .build();

    adminOperationManager.putOAuth2ConsumerCredential(dto.toPersistenceEntity());
    doGet(request, response);
  }
}
