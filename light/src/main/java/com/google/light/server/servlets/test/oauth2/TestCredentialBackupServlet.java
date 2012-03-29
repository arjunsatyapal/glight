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
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.servlets.test.CredentialStandardEnum.OAUTH2;
import static com.google.light.server.servlets.test.CredentialUtils.getOwnerTokenInfoFilePath;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkIsEnv;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.dao.OAuth2ConsumerCredentialDao;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.test.CredentialUtils;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightPreconditions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

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
  private Injector injector;
  
  private OAuth2ConsumerCredentialDao consumerCredentialDao;

  private OAuth2OwnerTokenManager googLoginTokenManager;
  private OAuth2OwnerTokenManager googDocTokenManager;
  private final String email = "unit-test1@myopenedu.com";
  private PersonManager personManager;

  @Inject
  public TestCredentialBackupServlet(Injector injector) {
    checkIsEnv(this, LightEnvEnum.DEV_SERVER, LightEnvEnum.UNIT_TEST, LightEnvEnum.QA);
    this.injector = checkNotNull(injector, "injector");
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    if (LightEnvEnum.getLightEnv() == LightEnvEnum.QA) {
      throw new UnsupportedOperationException("This Servlet is not supported in QA");
    }
    
    personManager = getInstance(injector, PersonManager.class);

    consumerCredentialDao = GuiceUtils.getInstance(injector, OAuth2ConsumerCredentialDao.class);
    
    OAuth2OwnerTokenManagerFactory tokenManagerFactory = getInstance(
        injector, OAuth2OwnerTokenManagerFactory.class);
    googLoginTokenManager = tokenManagerFactory.create(GOOGLE_LOGIN);
    googDocTokenManager = tokenManagerFactory.create(GOOGLE_DOC);

    try {
      super.service(request, response);
    } catch (Exception e) {
      // TODO(arjuns): Auto-generated catch block
      throw new RuntimeException(e);
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
    checkArgument(1 == OAuth2ProviderEnum.values().length, 
        "If you add more providers, add code to take backup here.");
    
    checkArgument(2 == OAuth2ProviderService.values().length, 
        "If you add more providers, add code to take backup here.");
        
    try {
      PersonEntity personEntity = personManager.getPersonByEmail(email);
      checkNotNull(personEntity, "Persion should have been created first.");

      String googConsumerCredJsonString = getConsumerCredJsonString();
      String googLoginTokenJsonString = getOwnerTokenJsonString(googLoginTokenManager, personEntity.getId());
      String googDocTokenJsonString = getOwnerTokenJsonString(googDocTokenManager, personEntity.getId());

      response.setContentType("application/zip");
      response.setStatus(HttpServletResponse.SC_OK);

      ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
      try {
        String filePath = getFilePathForConsumer();
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
  }

  private String getConsumerCredJsonString()
      throws JsonGenerationException, JsonMappingException, IOException {
    List<OAuth2ConsumerCredentialEntity> entities = consumerCredentialDao.getAllOAuth2ConsumerCredentials();
    LightPreconditions.checkNonEmptyList(entities);
    return JsonUtils.toJson(entities);
  }
  
  private String getOwnerTokenJsonString(OAuth2OwnerTokenManager tokenManager, long personId)
      throws JsonGenerationException, JsonMappingException, IOException {
    OAuth2OwnerTokenEntity entity = tokenManager.getToken(personId);
    Preconditions.checkNotNull(entity, "entity should not be null");
    // TODO(arjuns): Use entity.toDto().toJson();
    return JsonUtils.toJson(entity);
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

  private String getFilePathForConsumer() {
    return CredentialUtils.getConsumerCredentialDir(OAUTH2);
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
