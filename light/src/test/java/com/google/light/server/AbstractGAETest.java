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

import com.google.inject.Injector;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.TestingUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base class for tests that require basic GAE env.
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractGAETest {
  protected static LightEnvEnum defaultEnv = LightEnvEnum.UNIT_TEST;

  protected Injector injector;
  protected static GaeTestingUtils gaeTestingUtils = null;

  @BeforeClass
  public static void gaeSetup() {
    gaeTestingUtils = TestingUtils.gaeSetup(defaultEnv);
  }

  @AfterClass
  public static void gaeTearDown() {
    gaeTestingUtils.tearDown();
  }
}
