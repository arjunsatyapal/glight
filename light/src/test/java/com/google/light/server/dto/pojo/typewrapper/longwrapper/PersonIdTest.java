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
package com.google.light.server.dto.pojo.typewrapper.longwrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapperTest;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.XmlUtils;
import com.google.light.testingutils.TestResourcePaths;
import com.google.light.testingutils.TestingUtils;
import org.junit.Test;

/**
 * Test for {@link PersonId}
 *
 * @author Arjun Satyapal
 */
public class PersonIdTest implements AbstractTypeWrapperTest {
  private Long longNumber = new Long(1234567890L);
  private PersonId wrapperObject = new PersonId(longNumber);
  
  String expectedJson = "\"" + Long.toString(longNumber) + "\""; 
  /** 
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_getValue() {
    assertEquals(wrapperObject.toString(), longNumber, wrapperObject.getValue());
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_isValid() {
    assertTrue(wrapperObject.toString(), wrapperObject.isValid());
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_toString() {
    assertEquals("PersonId:" + longNumber, wrapperObject.toString());
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_validate() {
    wrapperObject.validate();
    // This should pass.
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_toXml() throws Exception {
    String expectedXml = TestingUtils.getResourceAsString(TestResourcePaths.PERSON_ID_XML.get());
    assertEquals(expectedXml, XmlUtils.toXml(wrapperObject, PersonId.class));
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_fromXml() {
    String expectedXml = TestingUtils.getResourceAsString(TestResourcePaths.PERSON_ID_XML.get());
    PersonId temp = XmlUtils.getPojo(expectedXml, PersonId.class);
    assertEquals(wrapperObject, temp);
  }

  /** 
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_toJson() {
    assertEquals(expectedJson, JsonUtils.toJson(wrapperObject, true));
  }

  /** 
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_fromJson() {
    assertEquals(wrapperObject, JsonUtils.getPojo(expectedJson, PersonId.class));
  }

  /** 
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_constructor() {
    assertEquals(wrapperObject, new PersonId(longNumber));
    
  }

  /** 
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_constructor_string() {
    assertEquals(wrapperObject, new PersonId(Long.toString(longNumber)));
  }
}
