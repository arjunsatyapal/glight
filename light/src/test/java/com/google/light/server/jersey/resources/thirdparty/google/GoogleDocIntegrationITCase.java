
///*
// * Copyright 2012 Google Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//package com.google.light.server.jersey.resources.thirdparty.google;
//
//import static com.google.common.base.Preconditions.checkNotNull;
//import static com.google.light.server.constants.RequestParamKeyEnum.EMAIL;
//import static com.google.light.server.constants.RequestParamKeyEnum.FULLNAME;
//import static com.google.light.server.constants.RequestParamKeyEnum.PASSWORD;
//import static com.google.light.server.servlets.test.CredentialStandardEnum.OAUTH2;
//import static com.google.light.server.servlets.test.CredentialUtils.getOwnerCredentialPasswdFileAbsPath;
//import static com.google.light.server.utils.LightUtils.getURI;
//
//import com.google.light.server.httclient.LightHttpClient;
//
//
//
//
//import com.google.api.client.http.ByteArrayContent;
//import com.google.api.client.http.HttpContent;
//import com.google.api.client.http.HttpResponse;
//import com.google.common.collect.Lists;
//import com.google.light.server.constants.JerseyConstants;
//import com.google.light.server.constants.LightEnvEnum;
//import com.google.light.server.constants.http.ContentTypeConstants;
//import com.google.light.server.dto.importresource.ExternalIdDto;
//import com.google.light.server.dto.importresource.ExternalIdDtoListWrapperDto;
//import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
//import com.google.light.server.utils.JsonUtils;
//import com.google.light.testingutils.GaeTestingUtils;
//import com.google.light.testingutils.TestingConstants;
//import com.google.light.testingutils.scripts.LoginITCase;
//import com.google.light.testingutils.scripts.LoginITCase.ParallelTests;
//import java.util.Properties;
//import java.util.Set;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.openqa.selenium.Cookie;
//
///**
// * 
// * 
// * TODO(arjuns): Add test for this class.
// * 
// * @author Arjun Satyapal
// */
//// TODO(arjuns): Complete this test
//@Ignore
//public class GoogleDocIntegrationITCase {
//  private String serverUrl = TestingConstants.SERVER_URL;
//  private String email = "unit-test1@myopenedu.com";
//  private LightEnvEnum defaultEnv = LightEnvEnum.UNIT_TEST;
//
//  @Before
//  public void setUp() {
//    GaeTestingUtils.cheapEnvSwitch(defaultEnv);
//  }
//
//  @Test
//  public void test_importGoogleDocs() throws Exception {
//    Properties ownerCredentials = LoginITCase.loadProperties(
//        getOwnerCredentialPasswdFileAbsPath(OAUTH2, email));
//    LoginITCase.validatePropertiesFile(ownerCredentials,
//        Lists.newArrayList(FULLNAME.get(), EMAIL.get(), PASSWORD.get()));
//
//    LoginITCase.ParallelTests seleniumTest = new ParallelTests(serverUrl, email);
//
//    seleniumTest.loginToGoogleAndRevokeAccess();
//    seleniumTest.getDriver().get(serverUrl + "/login/google");
//    seleniumTest.approveAccess();
//    seleniumTest.provideGoogleDocOAuth2OwnerToken();
//
//    Cookie requiredCookie = null;
//    // now main thing.
//    Set<Cookie> cookies = seleniumTest.getDriver().manage().getCookies();
//    for (Cookie currCookie : cookies) {
//      if (currCookie.getDomain().contains("localhost")) {
//        requiredCookie = currCookie;
//      }
//    }
//
//    checkNotNull(requiredCookie, "requiredCookie should not be null here.");
//    System.out.println(requiredCookie.toString());
//
//    LightHttpClient lightClient = new LightHttpClient(requiredCookie);
//
//    ExternalIdDtoListWrapperDto list = new ExternalIdDtoListWrapperDto();
//
//    ExternalIdDto dto =
//        new ExternalIdDto.Builder()
//            .externalId(new ExternalId(
//                    "https://docs.google.com/a/myopenedu.com/document/d/1mXX53OtXIhq2XbdQkk-utxO9pHdQ_dQsSsPE_HNtN_s/edit"))
//            .build();
//    list.addExternalIdDto(dto);
//
//    HttpContent httpContent = new ByteArrayContent(ContentTypeConstants.APPLICATION_JSON,
//        JsonUtils.toJson(list).getBytes());
//
//    HttpResponse response = lightClient.post(
//        getURI(serverUrl + JerseyConstants.URI_GOOGLE_DOC_IMPORT_POST), httpContent);
//
//    seleniumTest.getDriver().quit();
//  }
//}

