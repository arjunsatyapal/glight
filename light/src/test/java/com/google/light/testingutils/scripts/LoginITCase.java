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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderEnum.GOOGLE;
import static com.google.light.server.constants.RequestParamKeyEnum.CLIENT_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.CLIENT_SECRET;
import static com.google.light.server.constants.RequestParamKeyEnum.EMAIL;
import static com.google.light.server.constants.RequestParamKeyEnum.FULLNAME;
import static com.google.light.server.constants.RequestParamKeyEnum.PASSWORD;
import static com.google.light.server.servlets.path.ServletPathEnum.TEST_CREDENTIAL_BACKUP_SERVLET;
import static com.google.light.server.servlets.test.CredentialStandardEnum.OAUTH2;
import static com.google.light.server.servlets.test.CredentialUtils.getConsumerCredentialFileAbsPath;
import static com.google.light.server.servlets.test.CredentialUtils.getCredentialZipFilePath;
import static com.google.light.server.servlets.test.CredentialUtils.getOwnerCredentialPasswdFileAbsPath;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.testingutils.SeleniumUtils.clickAtCenter;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.light.server.constants.HtmlPathEnum;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.server.utils.LightPreconditions;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.SeleniumUtils;
import com.google.light.testingutils.TestingConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;


public class LoginITCase {
  static final Logger logger = Logger.getLogger(LoginITCase.class.getName());
  private static List<WebDriver> listOfDrivers = Lists.newArrayList();

  private StringBuffer verificationErrors = new StringBuffer();

  private LightEnvEnum defaultEnv = LightEnvEnum.DEV_SERVER;

  private List<String> listOfEmails = Lists.newArrayList(
      "func-test1@myopenedu.com",
      "light-bot@myopenedu.com");
  private Map<OAuth2ProviderEnum, Properties> consumerCredentialsMap = Maps.newHashMap();

  private final String serverUrl = TestingConstants.SERVER_URL;

  @Before
  public void seleniumSetup() throws Exception {
    GaeTestingUtils.cheapEnvSwitch(defaultEnv);

    for (OAuth2ProviderEnum currProvider : OAuth2ProviderEnum.values()) {
      logger.info(getConsumerCredentialFileAbsPath(OAUTH2, currProvider));
      Properties consumerCredentials = loadProperties(
          getConsumerCredentialFileAbsPath(OAUTH2, GOOGLE));

      validatePropertiesFile(consumerCredentials, Lists.newArrayList(
          CLIENT_ID.get(), CLIENT_SECRET.get()));
      consumerCredentialsMap.put(currProvider, consumerCredentials);
    }
    assertEquals(OAuth2ProviderEnum.values().length, consumerCredentialsMap.keySet().size());
  }

  public static Properties loadProperties(String filePath) throws FileNotFoundException,
      IOException {
    Properties properties = new Properties();
    properties.load(new FileInputStream(new File(filePath)));

    return properties;
  }

  public static String getEmail(Properties properties) {
    return getPropertyValue(properties, EMAIL.get());
  }

  public static String getFullName(Properties properties) {
    return getPropertyValue(properties, FULLNAME.get());
  }

  public static String getPassword(Properties properties) {
    return getPropertyValue(properties, PASSWORD.get());
  }

  static String getPropertyValue(Properties properties, String key) {
    checkNotNull(properties, "properties");
    return checkNotBlank((String) properties.get(key), key);
  }

  static WebDriver getSeleniumDriver() {
    WebDriver driver = SeleniumUtils.getWebDriver();
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    listOfDrivers.add(driver);
    return driver;
  }

  public static void validatePropertiesFile(Properties properties, List<String> keys) {
    for (String currKey : keys) {
      checkNotBlank(properties.getProperty(currKey), "missing : " + currKey);
    }
  }

  @Test
  public void test_missingThings() {
    assertEquals("If you add any new enum, then add code for populating the credentials in " +
        "testLogin.", 1, OAuth2ProviderEnum.values().length);

    assertEquals("If you add any new enum, then add code for populating the credentials in " +
        "testLogin.", 2, OAuth2ProviderService.values().length);
  }

  @Test
  public void test_Login() throws Exception {
    prepareServerForTest();

    List<Thread> listOfThread = Lists.newArrayList();
    for (String email : listOfEmails) {
      logger.info("Working on email : " + email);

      Thread thread = new Thread(new ParallelTests(serverUrl, email));
      thread.start();
      thread.join();
      listOfThread.add(thread);
    }

    for (Thread currThread : listOfThread) {
      currThread.join();
    }
    System.out.println("Done!");
  }

  public static class ParallelTests implements Runnable {
    private String serverUrl;
    private String email;
    private WebDriver driver;
    private Properties ownerCredentials;
    
    public WebDriver getDriver() {
      return driver;
    }

    public ParallelTests(String serverUrl, String email) throws FileNotFoundException, IOException {
      this.serverUrl = checkNotBlank(serverUrl, "serverUrl");
      this.driver = getSeleniumDriver();
      this.email = LightPreconditions.checkNotBlank(email, "email");
      logger.info(getOwnerCredentialPasswdFileAbsPath(OAUTH2, email));

      ownerCredentials = loadProperties(
          getOwnerCredentialPasswdFileAbsPath(OAUTH2, email));

      validatePropertiesFile(ownerCredentials,
          Lists.newArrayList(FULLNAME.get(), EMAIL.get(), PASSWORD.get()));
      getEmail(ownerCredentials);
      getFullName(ownerCredentials);
      getPassword(ownerCredentials);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      try {
        // Loging to Google Accounts and revoke all Access.
        loginToGoogleAndRevokeAccess();

        // Create Person on Light and finish Signup Flow.
        completeSignupFlow();

        // Now trigger the GOOGLE_DOC OAuth2 flow for Owner.
        provideGoogleDocOAuth2OwnerToken();

        // Logout from Light.
        logoutFromLight();

        // Logout from Google.
        logoutFromGoogle();

        /*
         * Now all credentials are populated on DataStore of Dev Server. Now download the Zip file
         * for functional tests.
         */
        downloadCredentialsFromServerAsZip();
        driver.quit();
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }

    public void loginToGoogleAndRevokeAccess() {
      loginToGoogle();

      // SeleniumUtils.clickIfExists(driver, By.id("choose-account-" + accountNumber));

      // Now revoking access
      driver.get("https://accounts.google.com/b/0/IssuedAuthSubTokens?hl=en");
//      driver.findElement(By.cssSelector("#nav-security > div.IurIzb")).click();
//      driver.findElement(By.xpath("//div[5]/div[2]/a/div")).click();
      while(driver.getPageSource().contains("Revoke Access")) {
        if (!SeleniumUtils.clickIfExists(driver, By.linkText("Revoke Access")))
          break;
      }
    }



    public void loginToGoogle() {
      String password = getPassword(ownerCredentials);
      
      driver
          .get("https://accounts.google.com/ServiceLogin?passive=1209600&continue=https%3A%2F%2Faccounts.google.com%2FManageAccount&followup=https%3A%2F%2Faccounts.google.com%2FManageAccount");
      driver.findElement(By.id("Email")).clear();
      driver.findElement(By.id("Email")).sendKeys(email);
      driver.findElement(By.id("Passwd")).clear();
      driver.findElement(By.id("Passwd")).sendKeys(password);
      driver.findElement(By.id("signIn")).click();
      
      if (driver.getPageSource().contains("skip")) {
        driver.findElement(By.id("skip")).click();
      }
    }


    public void completeSignupFlow() throws Exception {
      String fullName = getFullName(ownerCredentials);
      

      // Now trigger open a light page (eg. SEARCH_PAGE) and start GOOGLE_LOGIN OAuth2 flow for
      // Owner.
      driver.get(serverUrl + ServletPathEnum.SEARCH_PAGE.get());
      clickAtCenter(driver, By.id("loginToolbar_loginButton"));
      clickAtCenter(driver, By.id("loginToolbar_googleLoginProviderButton"));
      // Accepting
      approveAccess();

      // Asserting the User is logged in
      assertTrue(driver.getCurrentUrl().startsWith(serverUrl + ServletPathEnum.REGISTER_PAGE.get()));
      clickAtCenter(driver, By.id("registerForm_tosCheckbox"));
      clickAtCenter(driver, By.id("registerForm_submitButton"));

      // javascript redirects are making selenium to have some race conditions internally.
      // for now correcting it with a simple sleep
      // TODO(waltercacau):
      Thread.sleep(2000);
      assertTrue(driver.getCurrentUrl().startsWith(serverUrl + ServletPathEnum.MYDASH_PAGE.get()));
      assertEquals(fullName, driver.findElement(By.id("loginToolbar_personNameSpan")).getText());
    }

    public void approveAccess() {
      driver.findElement(By.id("submit_approve_access")).click();
    }
    
    public void logoutFromLight() {
      driver.get(serverUrl + ServletPathEnum.SEARCH_PAGE.get());
      clickAtCenter(driver, By.id("loginToolbar_logoutButton"));
    }

    public void logoutFromGoogle() {
      driver.get("https://accounts.google.com");
      clickAtCenter(driver, By.id("gbi4m1"));
      clickAtCenter(driver, By.id("gb_71"));
    }

    public void provideGoogleDocOAuth2OwnerToken() {
      driver.get(serverUrl + ServletPathEnum.OAUTH2_GOOGLE_DOC_AUTH.get());
      approveAccess();
    }

    private void downloadCredentialsFromServerAsZip() throws IOException,
        FileNotFoundException {
      GenericUrl zipUrl = new GenericUrl(serverUrl + TEST_CREDENTIAL_BACKUP_SERVLET.get());
      zipUrl.put(EMAIL.get(), email);
      HttpTransport httpTransport = new NetHttpTransport();
      HttpRequest request = httpTransport.createRequestFactory().buildGetRequest(zipUrl);
      HttpResponse response = request.execute();

      String outputPath = getCredentialZipFilePath(email);
      File file = new File(outputPath);
      if (file.exists()) {
        file.delete();
      }

      ByteStreams.copy(response.getContent(), new FileOutputStream(new File(outputPath)));
    }

  }

  private void prepareServerForTest() {
    WebDriver driver = getSeleniumDriver();
    // First login as Admin on Dev Server.
    loginAsAdminOnDevServer(driver);

    // Cleaning up the datastore
    driver.get(serverUrl + ServletPathEnum.TEST_CLEAN_UP_DATASTORE.get());

    // Now Provide OAuth2 Credentials
    provideLightCredentials(driver);
    driver.quit();
  }

  /**
  *
  */
  private void provideLightCredentials(WebDriver driver) {
    // Now PUT_OAUTH2_CONSUMER_CREDENTIAL Page.
    for (OAuth2ProviderEnum currProvider : OAuth2ProviderEnum.values()) {
      Properties consumerCredentials = consumerCredentialsMap.get(currProvider);

      driver.get(serverUrl + HtmlPathEnum.PUT_OAUTH2_CONSUMER_CREDENTIAL.get());

      String currElement = RequestParamKeyEnum.OAUTH2_PROVIDER_NAME.get();
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
    }
  }

  private void loginAsAdminOnDevServer(WebDriver driver) {
    driver.get(serverUrl + "/_ah/login");
    driver.findElement(By.id("isAdmin")).click();
    driver.findElement(By.name("action")).click();
  }

  @After
  public void tearDown() throws Exception {
    for (WebDriver currDriver : listOfDrivers) {
      try {
        currDriver.quit();
      } catch (Exception e) {
        // If fails, ignore exception and try to quit others.
      }
    }

    String verificationErrorString = verificationErrors.toString();
    if (!"".equals(verificationErrorString)) {
      fail(verificationErrorString);
    }
  }

  @SuppressWarnings("unused")
  private boolean isElementPresent(WebDriver driver, By by) {
    try {
      driver.findElement(by);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }
}
