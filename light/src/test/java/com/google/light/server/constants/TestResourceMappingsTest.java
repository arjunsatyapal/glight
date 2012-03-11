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
package com.google.light.server.constants;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Test for {@link TestResourceMappings}.
 * 
 * This test will ensure that testing resources do exist, else other tests will fail.
 * This will ensure that other tests dont fail due to absence of required resources.
 * 
 * @author Arjun Satyapal
 */
public class TestResourceMappingsTest {
  @Test 
  public void test_checkResourcesExist() {
    for (TestResourceMappings curr : TestResourceMappings.values()) {
      assertNotNull(curr.getJsonResPath() + " does not exist.",
          getClass().getResourceAsStream(curr.getJsonResPath()));
      assertNotNull(curr.getXmlResPath()+ " does not exist.",
          getClass().getResourceAsStream(curr.getXmlResPath()));
    }
  }
}
