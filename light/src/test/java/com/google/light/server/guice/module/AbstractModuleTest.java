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
package com.google.light.server.guice.module;

import com.google.light.server.constants.LightEnvEnum;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.TestingUtils;
import org.junit.Test;

/**
 * Tests for Modules should inherit from this.
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractModuleTest {
  protected GaeTestingUtils gaeTestingUtils = null;

  public void gaeSetUp(LightEnvEnum env) {
    gaeTestingUtils = TestingUtils.gaeSetup(env);
  }

  public void gaeTearDown() {
    gaeTestingUtils.tearDown();
  }

  /**
   * Inheriting classes should override this and then call gaeSetUp with corresponding env.
   */
  public abstract void setUp();
  
  /**
   * Inheriting classes should override this and then call gaeTearDown.
   */
  public abstract void tearDown();
  

  /**
   * Test to ensure that Module can be instantiated and all the required bindings are really
   * defined.
   */
  @Test
  public abstract void test_ModuleInstantiation();
}
