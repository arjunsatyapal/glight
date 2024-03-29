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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.guice.modules.ProdModule;
import com.google.light.server.utils.GaeUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link DevServerModule}.
 *
 * @author Arjun Satyapal
 */
public class DevServerModuleTest extends AbstractModuleTest {
  /** 
   * {@inheritDoc}
   */
  @Before
  @Override
  public void setUp() {
    super.gaeSetUp(LightEnvEnum.DEV_SERVER);
  }
  
  /**
   * {@inheritDoc}
   * @return 
   */
  @Override
  public void tearDown() {
    super.gaeTearDown();
  }

  
  /** 
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_ModuleInstantiation() {
    assertTrue(GaeUtils.isDevServer());
    assertNotNull(new ProdModule());
  }
}
