/*
 * Copyright 2012 Google Inc.
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
package com.google.light.server.servlets.test.oauth2;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderEnum.GOOGLE;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.servlets.test.CredentialStandardEnum.OAUTH2;
import static com.google.light.server.servlets.test.CredentialUtils.getOwnerTokenInfoFilePath;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkIsNotEnv;
import static com.google.light.server.utils.ServletUtils.prepareSession;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.dao.OAuth2ConsumerCredentialDao;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.test.CredentialUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.ServletUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Fake Servlet to Simulate Google Login.
 * 
 * Use POST to repopulate OAuth2 Login Credentials for unit-test1.
 * Use GET to fetch OAuth2 Login Credentials for unit-test1.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class TestCredentialBackupServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(TestCredentialBackupServlet.class.getName());
  
  private OAuth2ConsumerCredentialDao consumerCredentialDao;

  private OAuth2OwnerTokenManager googLoginTokenManager;
  private OAuth2OwnerTokenManager googDocTokenManager;
  private final String providerUserId = "115639870677665060321";
  private PersonManager personManager;

  @Inject
  public TestCredentialBackupServlet(Injector injector) {
    checkIsNotEnv(this, LightEnvEnum.PROD);
    checkNotNull(injector, "injector");
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    String email = checkNotNull(request.getParameter(RequestParamKeyEnum.EMAIL.get()));
    if (LightEnvEnum.getLightEnv() != LightEnvEnum.DEV_SERVER) {
      /*
       *  Even though we allow instantiation of this servlet for Test, we dont allow
       *  this to be called in non DEV_SERVER environments for safety reasons.
       */
      throw new UnsupportedOperationException("This Servlet is not supported in QA");
    }
    
    // Hack so that user does not have to login.
    prepareSession(request, GOOGLE_LOGIN, null, providerUserId, email);
    
    personManager = getInstance(PersonManager.class);

    PersonEntity person = personManager.findByEmail(email);
    prepareSession(request, GOOGLE_LOGIN, person.getPersonId(), providerUserId, email);
    
    consumerCredentialDao = getInstance(OAuth2ConsumerCredentialDao.class);
    
    OAuth2OwnerTokenManagerFactory tokenManagerFactory = getInstance(
        OAuth2OwnerTokenManagerFactory.class);
    googLoginTokenManager = tokenManagerFactory.create(GOOGLE_LOGIN);
    googDocTokenManager = tokenManagerFactory.create(GOOGLE_DOC);

    try {
      super.service(request, response);
    } catch (Exception e) {
      // TODO(arjuns): Auto-generated catch block
      throw new RuntimeException(e);
    } finally {
      // Forcing next request to create new session.
      ServletUtils.invalidateSession(request);
    }
  }
  
  /**
   * This method will export following things for Unit-test1 account on Dev_Server. <br>
   * 1. {@link OAuth2OwnerTokenDto} for {@link OAuth2ProviderService#GOOGLE_LOGIN}. <br>
   * 2. {@link OAuth2OwnerTokenDto} for {@link OAuth2ProviderService#GOOGLE_DOC}.
   * 
   * TODO(arjuns) : Add more things here as required for testing.
   * 
   * {@inheritDoc}
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    String email = checkNotNull(request.getParameter(RequestParamKeyEnum.EMAIL.get()));
    checkArgument(1 == OAuth2ProviderEnum.values().length, 
        "If you add more providers, add code to take backup here.");
    
    checkArgument(2 == OAuth2ProviderService.values().length, 
        "If you add more providers, add code to take backup here.");
        
    try {
      PersonEntity personEntity = personManager.findByEmail(email);
      checkNotNull(personEntity, "Person should have been created first.");

      String googConsumerCredJsonString = getConsumerCredJsonString(GOOGLE);
      String googLoginTokenJsonString = getOwnerTokenJsonString(googLoginTokenManager);
      String googDocTokenJsonString = getOwnerTokenJsonString(googDocTokenManager);

      response.setContentType(ContentTypeEnum.APPLICATION_ZIP.get());
      response.setHeader("Content-Disposition", "attachment; filename=credential.zip;");
      response.setStatus(HttpServletResponse.SC_OK);

      ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
      try {
        String filePath = getFilePathForConsumer(GOOGLE);
        addFileToZip(zipOut, filePath, googConsumerCredJsonString);
        
        filePath = getFilePathForOwner(GOOGLE_LOGIN);
        addFileToZip(zipOut, filePath, googLoginTokenJsonString);

        filePath = getFilePathForOwner(GOOGLE_DOC);
        addFileToZip(zipOut, filePath, googDocTokenJsonString);
        
        
      } finally {
        zipOut.close();
      }

    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException(e);
    }
    
    logger.info("Served file.");

  }

  /**
   * Get JSON representation for Consumer Credentials.
   * 
   * TODO(arjuns): Add test for this.
   */
  private String getConsumerCredJsonString(OAuth2ProviderEnum provider) {
    OAuth2ConsumerCredentialEntity entity = consumerCredentialDao.get(provider);
    
    return entity.toDto().toJson();
  }
  
  /**
   * Get JSON Representation for OwnerToken.
   * 
   * TODO(arjuns): Add test for this.
   */
  private String getOwnerTokenJsonString(OAuth2OwnerTokenManager tokenManager) {
    OAuth2OwnerTokenEntity entity = tokenManager.get();
    Preconditions.checkNotNull(entity, "entity should not be null");
    // TODO(arjuns): Use entity.toDto().toJson();
    return JsonUtils.toJson(entity.toDto());
  }

  private void addFileToZip(ZipOutputStream zipOut, String compFilePath, String content)
      throws IOException {
    byte[] buffer = new byte[1024 * 32];

    zipOut.putNextEntry(new ZipEntry(compFilePath));

    InputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
    try {

      int length = 0;
      while ((length = in.read(buffer)) > 0) {
        zipOut.write(buffer, 0, length);
      }

      zipOut.closeEntry();
    } finally {
      in.close();
    }
  }

  private String getFilePathForConsumer(OAuth2ProviderEnum provider) {
    return CredentialUtils.getConsumerCredentialFilePath(OAUTH2, provider);
  }
  
  private String getFilePathForOwner(OAuth2ProviderService providerService) {
    return getOwnerTokenInfoFilePath( OAUTH2, providerService);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

}
