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
package com.google.light.server.servlets.test;

import static com.google.light.server.constants.RequestParmKeyEnum.CLIENT_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.CLIENT_SECRET;
import static com.google.light.server.utils.LightPreconditions.checkIsEnv;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.exception.unchecked.ServerConfigurationException;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Test implementation for {@link OAuth2ConsumerCredentialManager}.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class TestGoogleOAuth2ConsumerCredentialManagerImpl implements
    OAuth2ConsumerCredentialManager {
  private static final Logger logger = Logger.getLogger(
      TestGoogleOAuth2ConsumerCredentialManagerImpl.class.getName());

  
  // TODO(arjuns): See if this needs to be Guice Injected eventually.
  private OAuth2Provider oAuth2Provider = OAuth2Provider.GOOGLE_LOGIN;
  private String clientId;
  private String clientSecret;

  @Inject
  public TestGoogleOAuth2ConsumerCredentialManagerImpl() {
    checkIsEnv(this, LightEnvEnum.DEV_SERVER, LightEnvEnum.UNIT_TEST);
    validateOrCreateConsumerCredentials(oAuth2Provider);
  }

  /**
   * All resource consumer credentials should be stored at
   * {@link #CREDENTIALS_DIR}/oauth2/consumer/<provider-name>.
   * 
   * e.g. For Google Provider, it will look like : {@link #CREDENTIALS_DIR}/oauth2/consumer/google
   * 
   * @throws IOException
   */
  protected void validateOrCreateConsumerCredentials(OAuth2Provider oauth2Provider) {
    try {
      // TODO(arjuns) : Refactor this part when more OAuthConsumer targets are added.
      String consumerCredentialPath = CredentialUtils.getConsumerCredentialDir(
          LightEnvEnum.getLightEnv(),
          CredentialStandardEnum.OAUTH2,
          OAuth2Provider.GOOGLE_LOGIN);
      File consumerCredentialFile = new File(consumerCredentialPath);

      boolean fileIsValid = false;
      if (consumerCredentialFile.exists()) {
        if (consumerCredentialFile.isDirectory()) {
          throw new IllegalStateException(consumerCredentialPath
              + " should be a file, but found a directory.");
        } else {
          try {
            fileIsValid = validateFile(consumerCredentialPath);
          } catch (Exception e) {
            fileIsValid = false;
          }
        }
      }

      if (fileIsValid) {
        return;
      }
      logger.info("Either " + consumerCredentialPath + " does not exist, or is invalid. " +
          "Creating it again.");

      // Since file is invalid, force its recreation.
      Files.createParentDirs(consumerCredentialFile);
      createFile(oauth2Provider, consumerCredentialPath);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Create File with OAuth2 credentials for the Consumer.
   * 
   * @param oauth2Provider2
   * 
   * @param consumerCredentialPath
   * @throws IOException
   */
  private void createFile(OAuth2Provider oauth2Provider2, String consumerCredentialPath)
      throws IOException {
    logger.info("Creating credential file for : " + oauth2Provider2.getProviderName());
    // TODO(arjuns): Fix this eventually to do validation.
    StringBuilder builder =
        new StringBuilder("Appengine does not allow creating of files. So create a file at \n"
            + consumerCredentialPath + "\n and put following content into it : "
            + "\n" + CLIENT_ID.get() + "=<put your clientId here>"
            + "\n" + CLIENT_SECRET.get() + "=<put your clientSecret here.>");
    throw new ServerConfigurationException(builder.toString());
  }

  /**
   * File will be considered valid if is in java.properties file format. <br>
   * Valid file will look like :
   * <p>
   * <br>
   * client_id="foo" <br>
   * client_secret="bar"
   * 
   * @param consumerCredentialPath
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   */
  private boolean validateFile(String consumerCredentialPath) throws FileNotFoundException,
      IOException {
    Properties properties = new Properties();
    properties.load(new FileInputStream(consumerCredentialPath));

    /*
     * After fetching values from file, we are trimming it in order to ensure there were no extra 
     * spaces introduced while creating the credential file.
     */
    this.clientId = checkNotBlank(properties.getProperty(CLIENT_ID.get())).trim();
    this.clientSecret = checkNotBlank(properties.getProperty(CLIENT_SECRET.get())).trim();
    return true;
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
}
