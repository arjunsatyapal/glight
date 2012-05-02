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
package com.google.light.server.utils;

import static com.google.inject.Guice.createInjector;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.GuiceUtils.getKeyForScopeSeed;
import static com.google.light.server.utils.GuiceUtils.isExpectedException;
import static com.google.light.testingutils.GaeTestingUtils.cheapEnvSwitch;
import static com.google.light.testingutils.TestingUtils.getInjectorByEnv;
import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
import static com.google.light.testingutils.TestingUtils.getRandomProviderUserId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.light.server.dto.pojo.longwrapper.PersonId;



import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.exception.unchecked.EmailInUseException;
import com.google.light.server.exception.unchecked.LightRuntimeException;
import com.google.light.server.guice.provider.TestRequestScopedValuesProvider;
import com.google.light.server.persistence.dao.PersonDao;
import com.google.light.testingutils.TestingUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link GuiceUtils}
 *
 * @author Arjun Satyapal
 */
public class GuiceUtilsTest {
  private HttpSession session;
  private TestRequestScopedValuesProvider testRequestScopedValueProvider;
  private PersonId personId;
  private String email;
  
  private OAuth2ProviderService defaultProviderService = GOOGLE_LOGIN;
  private String providerUserId;
  private LightEnvEnum defaultEnv = LightEnvEnum.PROD;
  @Before
  public void setUp() {
    this.providerUserId = getRandomProviderUserId();
    this.personId = TestingUtils.getRandomPersonId();
    this.email = TestingUtils.getRandomEmail();
    cheapEnvSwitch(defaultEnv);
    
    this.session = getMockSessionForTesting(defaultEnv, defaultProviderService, providerUserId, 
        personId, email);
    this.testRequestScopedValueProvider = TestingUtils.getRequestScopedValueProvider(personId, personId);
    Injector injector = getInjectorByEnv(defaultEnv, testRequestScopedValueProvider, session);
    GuiceUtils.setInjector(injector);
  }
  /**
   * Test for {@link GuiceUtils#getInstance(Injector, Class)}.
   */
  @Test
  public void test_getInstance_class() throws Exception {
    assertNotNull(getInstance(PersonDao.class));
  }
  
  /**
   * Test for {@link GuiceUtils#getInstance(Injector, Class, Class))}.
   */
  @Test
  public void test_get_instance_class_anot() throws Exception {
    Module module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(TestInterface.class)
          .annotatedWith(AnotTest.class)
          .to(TestClass.class);
      }
    };
    
    Injector testInjector = createInjector(module);
    TestInterface testInstance = testInjector.getInstance(
        getKeyForScopeSeed(TestInterface.class, AnotTest.class));
    assertNotNull(testInstance);
    assertEquals(TestClass.class.getName(), testInstance.getClass().getName());
  }
  
  /**
   * Test for {@link GuiceUtils#getKeyForScopeSeed(Class, Class)}.
   */
  @Test
  public void test_getKeyForScopeSeed() {
    Key<TestInterface> key = GuiceUtils.getKeyForScopeSeed(TestInterface.class, AnotTest.class);
    assertNotNull(key);
    assertEquals(AnotTest.class, key.getAnnotationType());
    assertEquals(TestInterface.class.getName(), key.getTypeLiteral().toString());
  }
  
  /**
   * Test for {@link GuiceUtils#isExpectedException(ProvisionException, Class)} 
   */
  @Test
  public void test_isExpectedException() {
    ProvisionException expected = new ProvisionException("", new LightRuntimeException());
    assertTrue(isExpectedException(expected, LightRuntimeException.class));
    assertFalse(isExpectedException(expected, EmailInUseException.class));
  }
  
  /**
   * Test for {@link GuiceUtils#seedEntityInRequestScope(HttpServletRequest, Class, Class, Object)}.
   */
  @Test
  public void test_seedEntityInRequestScope() {
    // This cannot be easily tested in unit-tests as it needs servlet environment.
  }

  
  @BindingAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  private static @interface AnotTest{
  }
  
  private static interface TestInterface {
  }
  
  private static class TestClass implements TestInterface {
  }
}
