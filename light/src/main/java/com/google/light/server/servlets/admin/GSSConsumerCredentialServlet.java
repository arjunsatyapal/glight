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
package com.google.light.server.servlets.admin;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderEnum.GSS;
import static com.google.light.server.utils.LightPreconditions.checkPersonIsGaeAdmin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.api.client.googleapis.auth.clientlogin.ClientLogin.ErrorInfo;
import com.google.api.client.googleapis.auth.clientlogin.ClientLoginResponseException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.manager.interfaces.AdminOperationManager;
import com.google.light.server.manager.interfaces.GSSClientLoginTokenManager;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import com.google.light.server.servlets.path.ServletPathEnum;

/**
 * Servlet to feed the system with GSS Consumer Credentials and help
 * to renew the ClientLogin tokens manually if necessary.
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
public class GSSConsumerCredentialServlet extends HttpServlet {

  private Provider<GSSClientLoginTokenManager> tokenManagerProvider;
  private Provider<AdminOperationManager> adminOperationManagerProvider;

  @Inject
  public GSSConsumerCredentialServlet(
      Provider<AdminOperationManager> adminOperationManagerProvider,
      Provider<GSSClientLoginTokenManager> tokenManagerProvider) {
    this.tokenManagerProvider = tokenManagerProvider;
    this.adminOperationManagerProvider = adminOperationManagerProvider;
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    checkPersonIsGaeAdmin();
    super.service(request, response);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    // TODO(waltercacau): Use JSP instead of generating the HTML by hand.

    GSSClientLoginTokenManager tokenManager = tokenManagerProvider.get();

    PrintWriter writer = resp.getWriter();
    appendHeader(writer, "GSS Consumer Credential Admin Page");
    appendSectionTitle(writer, "Current consumer credential");

    OAuth2ConsumerCredentialEntity consumerCredential =
        adminOperationManagerProvider.get().getOAuth2ConsumerCredential(GSS);
    boolean showReplacementForm = true;
    if (consumerCredential == null) {
      appendLine(writer,
          "No consumer credential found for GSS. Please, add one with the form bellow.");
      appendSectionSeparator(writer);
      appendSectionTitle(writer, "Consumer credential form");
      appendForm(writer, "Consumer credential form", null);
      showReplacementForm = false;
    } else {

      appendLine(writer, "Found consumer credential for " + consumerCredential.getClientId());
      String token = tokenManager.getCurrentToken();
      appendLine(writer, "Current token: " + token);

      try {
        tokenManager.validateCurrentToken();
        appendLine(writer, "Current token is valid!");
      } catch (Exception invalidException) {
        appendLine(writer, "Current token is invalid! Trying to reauthenticate.");

        try {
          token = tokenManager.authenticate();
          appendLine(writer, "New token: " + token);
        } catch (ClientLoginResponseException authException) {
          ErrorInfo details = authException.getDetails();
          appendLine(writer,
              "Authentication failed, please, try to authenticate manually with the form bellow");
          appendForm(writer, "Manual Authentication Form", details);
          showReplacementForm = false;
        }
      }
    }

    if (showReplacementForm) {
      appendForm(writer, "Consumer credential replacement form", null);
    }

    // If there is a consumer secret
    // if there is a owner token for it (clientlogin token actually)
    // show
    // else if there is already a user for that consumer secret
    // try to get the owner token (clientlogin token actually)
    // if succeed show
    // else if google ask captcha ask for captcha solution
    // else say it failed and ask to try another consumer secret or check other stuff
    // provide a way to replace consumer secret
    appendFooter(writer);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    GSSClientLoginTokenManager tokenManager = tokenManagerProvider.get();
    String username = checkNotNull(req.getParameter("username"), "username");
    String password = checkNotNull(req.getParameter("password"), "password");
    String captchaToken = req.getParameter("username");
    String captchaAnswer = req.getParameter("captchaAnswer");

    PrintWriter writer = resp.getWriter();
    appendHeader(writer, "GSS Consumer Credential Admin Page");
    try {
      tokenManager.authenticate(username, password, captchaToken, captchaAnswer);
      appendLine(writer, "Authentication succeded!");
      appendLink(writer, ServletPathEnum.GSS.get(), "Click here to go back");
    } catch (ClientLoginResponseException authException) {
      ErrorInfo details = authException.getDetails();
      appendLine(writer,
          "Authentication failed, please, try to authenticate manually with the form bellow");
      appendForm(writer, "Manual Authentication Form", details);
    }
    appendFooter(writer);
  }

  protected static void appendHeader(Writer writer, String title) throws IOException {
    writer.append("<html>");
    writer.append("<head>");
    writer.append("<title>" + StringEscapeUtils.escapeHtml(title) + "</title>");
    writer.append("</head>");
    writer.append("<body>");
  }

  protected static void appendSectionTitle(Writer writer, String title) throws IOException {
    writer.append("<h1>" + StringEscapeUtils.escapeHtml(title) + "</h1><br />");
  }

  protected static void appendLine(Writer writer, String line) throws IOException {
    writer.append(StringEscapeUtils.escapeHtml(line) + "<br />");
  }

  protected static void appendLink(Writer writer, String link, String label) throws IOException {
    writer.append("<a href=\"" + StringEscapeUtils.escapeHtml(link) + "\">"
        + StringEscapeUtils.escapeHtml(label) + "</a><br />");
  }

  protected static void appendSectionSeparator(Writer writer) throws IOException {
    writer.append("<hr />");
  }

  protected static void appendFooter(Writer writer) throws IOException {
    writer.append("</body>");
    writer.append("</html>");
  }

  protected static void appendForm(Writer writer, String title, ErrorInfo errorInfo)
      throws IOException {
    appendSectionSeparator(writer);
    appendSectionTitle(writer, title);
    if (errorInfo != null)
      writer.append("<b>Error: " + StringEscapeUtils.escapeHtml(errorInfo.error) + "</b><br />");
    writer.append("<form method=\"POST\">");
    writer.append("Username: <input type=\"text\" name=\"username\"><br />");
    writer.append("Password: <input type=\"password\" name=\"password\"><br />");
    if (errorInfo != null && errorInfo.captchaToken != null && errorInfo.captchaUrl != null) {
      writer.append("Captcha: <br/><img src=\""
          + StringEscapeUtils.escapeHtml(errorInfo.captchaUrl)
          + "\"><br/>");
      writer.append("<input type=\"text\" name=\"captchaAnswer\" value=\"\"><br />");
      writer.append("<input type=\"hidden\" name=\"captchaToken\" value=\""
          + StringEscapeUtils.escapeHtml(errorInfo.captchaToken) + "\">");
    }
    writer.append("<input type=\"submit\" value=\"Submit\">");
    writer.append("</form>");
  }
}
