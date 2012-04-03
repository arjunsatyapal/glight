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

package com.google.light.testingutils.scripts;

import static com.google.light.server.constants.HtmlPathEnum.PUT_OAUTH2_CONSUMER_CREDENTIAL;
import static com.google.light.server.constants.OAuth2ProviderEnum.GOOGLE;
import static com.google.light.server.constants.RequestParamKeyEnum.CLIENT_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.CLIENT_SECRET;
import static com.google.light.server.constants.RequestParamKeyEnum.PASSWORD;
import static com.google.light.server.servlets.path.ServletPathEnum.OAUTH2_GOOGLE_DOC_AUTH;
import static com.google.light.server.servlets.path.ServletPathEnum.OAUTH2_GOOGLE_LOGIN;
import static com.google.light.server.servlets.path.ServletPathEnum.TEST_CREDENTIAL_BACKUP_SERVLET;
import static com.google.light.server.servlets.test.CredentialStandardEnum.OAUTH2;
import static com.google.light.server.servlets.test.CredentialUtils.getConsumerCredentialFileAbsPath;
import static com.google.light.server.servlets.test.CredentialUtils.getCredentialZipFilePath;
import static com.google.light.server.servlets.test.CredentialUtils.getOwnerCredentialPasswdFileAbsPath;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;

import java.net.URL;

import java.io.FileOutputStream;

import com.google.common.io.ByteStreams;

import com.google.api.client.http.HttpResponse;

import com.google.api.client.http.GenericUrl;

import com.google.api.client.http.HttpRequest;

import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.http.HttpTransport;

import com.google.common.collect.Lists;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.SeleniumUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

public class LoginITCase {
  private WebDriver driver;
  private StringBuffer verificationErrors = new StringBuffer();
  private Properties ownerCredentials;
  private Properties consumerCredentials;

  private LightEnvEnum defaultEnv = LightEnvEnum.DEV_SERVER;
  private String userName = "unit-test1@myopenedu.com";
  private String password;

  private final String serverUrl = "http://localhost:8080";

  @Before
  public void seleniumSetup() throws Exception {
    GaeTestingUtils.cheapEnvSwitch(defaultEnv);
    
    System.out.println(getOwnerCredentialPasswdFileAbsPath(OAUTH2));
    ownerCredentials = loadProperties(getOwnerCredentialPasswdFileAbsPath(OAUTH2));
    validatePropertiesFile(ownerCredentials, Lists.newArrayList(PASSWORD.get()));
    password = ownerCredentials.getProperty(PASSWORD.get());

    System.out.println(getConsumerCredentialFileAbsPath(OAUTH2, GOOGLE));
    consumerCredentials = loadProperties(getConsumerCredentialFileAbsPath(OAUTH2, GOOGLE));
    validatePropertiesFile(consumerCredentials, Lists.newArrayList(
        CLIENT_ID.get(), CLIENT_SECRET.get()));
  }

  /**
   * @return 
   * 
   */
  private WebDriver getSeleniumDriver() {
    WebDriver driver = SeleniumUtils.getWebDriver();
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    return driver;
  }

  private static Properties loadProperties(String filePath) throws FileNotFoundException,
      IOException {
    Properties properties = new Properties();
    properties.load(new FileInputStream(new File(filePath)));

    return properties;
  }

  private static void validatePropertiesFile(Properties properties, List<String> keys) {
    for (String currKey : keys) {
      checkNotBlank(properties.getProperty(currKey), "missing : " + currKey);
    }
  }

  @Test
  public void test_missingThings() {
    assertEquals(
        "If you add any new enum, then add code for populating the credentials in testLogin.",
        1, OAuth2ProviderEnum.values().length);

    assertEquals(
        "If you add any new enum, then add code for populating the credentials in testLogin.",
        2, OAuth2ProviderService.values().length);
  }

  @Test
  public void testLogin() throws Exception {
    driver = getSeleniumDriver();
    
    // First login as Admin on Dev Server.
    driver.get(serverUrl + "/_ah/login");
    driver.findElement(By.id("isAdmin")).click();
    driver.findElement(By.name("action")).click();
    
    
    // First signin to Google Account.
    driver
        .get("https://accounts.google.com/ServiceLogin?passive=1209600&continue=https%3A%2F%2Faccounts.google.com%2FManageAccount&followup=https%3A%2F%2Faccounts.google.com%2FManageAccount");
    driver.findElement(By.id("Email")).clear();
    driver.findElement(By.id("Email")).sendKeys(userName);
    driver.findElement(By.id("Passwd")).clear();
    driver.findElement(By.id("Passwd")).sendKeys(password);
    driver.findElement(By.id("signIn")).click();
    
    // Now revoking access
    driver.findElement(By.cssSelector("#nav-security > div.IurIzb")).click();
    driver.findElement(By.xpath("//div[5]/div[2]/a/div")).click();
    driver.findElement(By.linkText("Revoke Access")).click();
    driver.findElement(By.linkText("Revoke Access")).click();

    // Now Load PUT_OAUTH2_CONSUMER_CREDENTIAL Page.
    driver.get(serverUrl + ServletPathEnum.TEST_WORKFLOW_SERVLETS.get());
    String currElement = PUT_OAUTH2_CONSUMER_CREDENTIAL.name();
    driver.findElement(By.id(currElement)).click();

    // Now Provide OAuth2 Credentials
    currElement = RequestParamKeyEnum.OAUTH2_PROVIDER_NAME.get();
    driver.findElement(By.name(currElement)).clear();
    driver.findElement(By.name(currElement)).sendKeys(GOOGLE.name());

    currElement = CLIENT_ID.get();
    driver.findElement(By.name(currElement)).clear();
    driver.findElement(By.name(currElement)).sendKeys(
        consumerCredentials.getProperty(CLIENT_ID.get()));

    currElement = CLIENT_SECRET.get();
    driver.findElement(By.name(currElement)).clear();
    driver.findElement(By.name(currElement)).sendKeys(
        consumerCredentials.getProperty(CLIENT_SECRET.get()));
    driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();

    // Google OAuth2 consumer credentials are now saved.

    // Now trigger GOOGLE_LOGIN OAuth2 flow for Owner.
    driver.get(serverUrl + ServletPathEnum.TEST_WORKFLOW_SERVLETS.get());
    currElement = OAUTH2_GOOGLE_LOGIN.name();
    driver.findElement(By.id(currElement)).click();
    driver.findElement(By.id("submit_approve_access")).click();

    // Now trigger the GOOGLE_DOC OAuth2 flow for Owner.
    driver.get(serverUrl + ServletPathEnum.TEST_WORKFLOW_SERVLETS.get());
    currElement = OAUTH2_GOOGLE_DOC_AUTH.name();
    driver.findElement(By.id(currElement)).click();
    driver.findElement(By.id("submit_approve_access")).click();
    
    /*
     * Now all credentials are populated on DataStore of Dev Server. Now download the Zip file 
     * for functional tests.
     */
    GenericUrl zipUrl = new GenericUrl(serverUrl + TEST_CREDENTIAL_BACKUP_SERVLET.get());
    HttpTransport httpTransport = new NetHttpTransport();
    HttpRequest request = httpTransport.createRequestFactory().buildGetRequest(zipUrl);
    HttpResponse response = request.execute();
    
    String outputPath = getCredentialZipFilePath();
    File file = new File(outputPath);
    if (file.exists()) {
      file.delete();
    }
    
    ByteStreams.copy(response.getContent(), new FileOutputStream(new File(outputPath)));
  }

  @After
  public void tearDown() throws Exception {
    if (driver == null) {
      return;
    }
    
    driver.quit();
    String verificationErrorString = verificationErrors.toString();
    if (!"".equals(verificationErrorString)) {
      fail(verificationErrorString);
    }
  }

  @SuppressWarnings("unused")
  private boolean isElementPresent(By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }
}
