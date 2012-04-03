/*
 * Copyright 2012 Google Inc.
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
package com.google.light.server.manager.implementation.oauth2.consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderEnum.GOOGLE;
import static com.google.light.server.servlets.test.CredentialStandardEnum.OAUTH2;
import static com.google.light.server.servlets.test.CredentialUtils.getConsumerCredentialFilePath;
import static com.google.light.server.utils.LightUtils.getInputStreamAsString;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.inject.Inject;
import com.google.light.server.dto.admin.OAuth2ConsumerCredentialDto;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.servlets.test.CredentialUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.testingutils.scripts.LoginITCase;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Implementation for {@link OAuth2ConsumerCredentialManager} for {@link LightEnvEnum#UNIT_TEST}
 * 
 * @author Arjun Satyapal
 */
public class TestOAuth2ConsumerCredentialManagerImpl implements OAuth2ConsumerCredentialManager {
  private String clientId;
  private String clientSecret;
  private ClientParametersAuthentication clientParamAuth;
  
  @Inject
  public TestOAuth2ConsumerCredentialManagerImpl() throws ZipException, IOException {
    // Ensure that zip file exists.
    File file = new File(CredentialUtils.getCredentialZipFilePath());
    checkArgument(file.exists(), "Run " + LoginITCase.class.getName());
    ZipFile zipFile = new ZipFile(file);
    
    ZipEntry zipEntry = new ZipEntry(getConsumerCredentialFilePath(OAUTH2, GOOGLE));
    InputStream is = zipFile.getInputStream(zipEntry);
    checkNotNull(is);
    String consumerJsonString = getInputStreamAsString(is);
    
    OAuth2ConsumerCredentialDto consumerCredDto = JsonUtils.getDto(consumerJsonString, 
        OAuth2ConsumerCredentialDto.class);
    clientId = consumerCredDto.getClientId();
    clientSecret = consumerCredDto.getClientSecret();
    clientParamAuth = new ClientParametersAuthentication(clientId, clientSecret);
  }
  
  /** 
   * {@inheritDoc}
   */
  @Override
  public String getClientId() {
    return clientId;
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public String getClientSecret() {
    return clientSecret;
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public ClientParametersAuthentication getClientAuthentication() {
    return clientParamAuth;
  }
}
