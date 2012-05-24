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
package com.google.light.testingutils.scripts;

import static com.google.light.server.constants.OAuth2ProviderEnum.GSS;
import static com.google.light.server.constants.RequestParamKeyEnum.CLIENT_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.CLIENT_SECRET;
import static com.google.light.server.servlets.test.CredentialStandardEnum.CLIENT_LOGIN;
import static com.google.light.testingutils.TestingUtils.getInjectorByEnv;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.dto.search.OnDemandIndexingRequest;
import com.google.light.server.exception.checked.FailedToIndexException;
import com.google.light.server.manager.implementation.SearchManagerGSSImpl;
import com.google.light.server.persistence.dao.OAuth2ConsumerCredentialDao;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import com.google.light.server.servlets.test.CredentialUtils;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.testingutils.TestingUtils;
import java.io.IOException;
import java.util.Properties;

/**
 * Small test script to send On Demand Indexing Request to GSS/CSE
 * 
 * @author Walter Cacau
 * 
 */
public class ControlGSSTestScript {

  public static void main(String[] args) throws IOException, FailedToIndexException {
    
    // Initializing
    TestingUtils.gaeSetup(LightEnvEnum.DEV_SERVER);
    Injector injector = getInjectorByEnv(LightEnvEnum.DEV_SERVER);
    GuiceUtils.setInjector(injector);
    SearchManagerGSSImpl searchManager = injector.getInstance(SearchManagerGSSImpl.class);
    OAuth2ConsumerCredentialDao consumerCredentialDao = injector.getInstance(OAuth2ConsumerCredentialDao.class);
    
    Properties consumerCredentials =
        TestingUtils.loadProperties(CredentialUtils.getConsumerCredentialFileAbsPath(CLIENT_LOGIN,
            GSS));
    TestingUtils.validatePropertiesFile(consumerCredentials, Lists.newArrayList(
        CLIENT_ID.get(), CLIENT_SECRET.get()));
    
    consumerCredentialDao.put(null,
        new OAuth2ConsumerCredentialEntity.Builder()
          .providerName(GSS.name())
          .clientId(consumerCredentials.getProperty(CLIENT_ID.get()))
          .clientSecret(consumerCredentials.getProperty(CLIENT_SECRET.get()))
          .build());
    
    searchManager.index(new OnDemandIndexingRequest()
        .add("http://light-qa.appspot.com/"));
    
    /*GSSClientLoginTokenManagerImpl tokenManager =
        injector.getInstance(GSSClientLoginTokenManagerImpl.class);
    HttpTransport transport = injector.getInstance(HttpTransport.class);

    // Authorizing
    tokenManager.authenticate(consumerCredentials.getProperty(CLIENT_ID.get()),
            consumerCredentials.getProperty(CLIENT_SECRET.get()), null, null);
    
    String contentString = new OnDemandIndexingRequest()
        .add("http://light-demo.appspot.com/rest/content/general/module/25005/latest/")
        .toXmlString();
    InputStream stream = new ByteArrayInputStream(contentString.getBytes("UTF-8"));
    HttpContent content = new InputStreamContent("application/xml; charset=utf-8", stream);
    HttpRequest request =
        transport.createRequestFactory().buildPostRequest(
            new GenericUrl("https://www.google.com/cse/api/" + CSE_OWNER_ID + "/index/" + CSE_ID),
            content);
    request.setEnableGZipContent(false);
    request.getHeaders().setAuthorization(authorizationToken);
    request.getHeaders().setAcceptEncoding("utf-8");
    System.out.println("POST to " + request.getUrl().toString());
    System.out.println("Request headers:");
    for (Entry<String, Object> entry : request.getHeaders().entrySet()) {
      System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
    }
    System.out.println("Request Content:");
    System.out.println(contentString.toString());

    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();

    System.out.println("Status: " + response.getStatusCode() + " " + response.getStatusMessage());
    System.out.println("Response headers:");
    for (Entry<String, Object> entry : response.getHeaders().entrySet()) {
      System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
    }
    System.out.println("Response Content:");
    InputStream contentStream = response.getContent();
    // if(response.getContentEncoding().equals("gzip")) {
    // contentStream = new GZIPInputStream(contentStream);
    // }
    Document doc = new SAXBuilder().build(contentStream);
    System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(doc));
    Element root = doc.getRootElement();
    Assert.assertEquals("SUCCESS", root.getAttribute("refresh_status").getValue());*/
    
    
  }
}
