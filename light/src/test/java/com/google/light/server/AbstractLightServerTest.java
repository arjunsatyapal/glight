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
package com.google.light.server;

import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.utils.LightPreconditions.checkValidSession;
import static com.google.light.testingutils.TestingUtils.createRandomPerson;
import static com.google.light.testingutils.TestingUtils.getInjectorByEnv;
import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
import static com.google.light.testingutils.TestingUtils.getRequestScopedValueProvider;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomProviderUserId;
import static com.google.light.testingutils.TestingUtils.getRandomString;

import com.google.light.server.dto.pojo.PersonId;


import com.google.light.server.guice.provider.TestRequestScopedValuesProvider;

import com.google.inject.Injector;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.utils.GuiceUtils;
import javax.servlet.http.HttpSession;
import org.junit.After;
import org.junit.Before;

/**
 * Base class for all the Light Server Tests.
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractLightServerTest extends AbstractGAETest {
  protected static LightEnvEnum defaultEnv = LightEnvEnum.UNIT_TEST;

  protected static OAuth2ProviderService defaultProviderService = GOOGLE_LOGIN;
  protected TestRequestScopedValuesProvider testRequestScopedValueProvider;
  protected HttpSession testSession;
  protected PersonEntity testPerson;
  protected PersonId testPersonId;
  protected String testProviderUserId;
  protected String testEmail;
  protected String testFirstName;
  protected String testLastName;
  
  private Injector injector;

  @Before
  public void setUp() {
    testProviderUserId = getRandomProviderUserId();
    testEmail = getRandomEmail();
    
    testFirstName = getRandomString();
    testLastName = getRandomString();

    // Now re-create session with PersonId using Person created in previous step.
    testSession = getMockSessionForTesting(defaultEnv, defaultProviderService,
        testProviderUserId, testPersonId, testEmail);
    testRequestScopedValueProvider = getRequestScopedValueProvider(new PersonId(1L), new PersonId(1L));
    
    injector = getInjectorByEnv(defaultEnv, testRequestScopedValueProvider, testSession);
    GuiceUtils.setInjector(injector);
    
    // Now initializing TestRequestScope

    testPerson = createRandomPerson(defaultEnv, testRequestScopedValueProvider, testSession);
    testPersonId = testPerson.getPersonId();
    
    testRequestScopedValueProvider.getParticipants().updateBoth(testPersonId);

    // Now reinitializing session with this personid. And updating Injector.
    testSession = getMockSessionForTesting(defaultEnv, defaultProviderService,
        testProviderUserId, testPersonId, testEmail);

    injector = getInjectorByEnv(defaultEnv, testRequestScopedValueProvider,  testSession);
    GuiceUtils.setInjector(injector);
    
    SessionManager sessionManager = GuiceUtils.getInstance(SessionManager.class);
    checkValidSession(sessionManager);
  }

  @After
  public void tearDown() {
  }
}
