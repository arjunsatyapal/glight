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

import static com.google.light.server.constants.LightStringConstants.VERSION_LATEST_STR;
import static com.google.light.testingutils.TestingUtils.getResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapperTest;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.XmlUtils;
import com.google.light.testingutils.TestResourcePaths;
import org.junit.Test;

/**
 * Test for {@link Version}
 * 
 * @author Arjun Satyapal
 */
public class VersionTest implements AbstractTypeWrapperTest {
  private Long longNumber = new Long(567890L);
  
  private Version versionWithLong = new Version(longNumber);
  private String expectedVersionWithLongJson = "\"" + longNumber + "\"";
  
  private Version versionWithLatest = new Version(VERSION_LATEST_STR);
  private String expectedVersionWithLatest = "\"-1\"";
  
  private Version noVersion = new Version(Version.NO_VERSION);
  private String expectedNoVersionJson = "\"0\"";
  
  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_getValue() {
    assertEquals(versionWithLong.toString(), longNumber, versionWithLong.getValue());
    assertEquals(versionWithLong.toString(), Version.State.SPECIFIC, versionWithLong.getState());

    assertEquals(versionWithLatest.toString(), new Long(-1L), versionWithLatest.getValue());
    assertEquals(versionWithLatest.toString(), Version.State.LATEST, versionWithLatest.getState());

    assertEquals(noVersion.toString(), new Long(0L), noVersion.getValue());
    assertEquals(noVersion.toString(), Version.State.NO_VERSION, noVersion.getState());

  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_isValid() {
    assertTrue(versionWithLong.toString(), versionWithLong.isValid());
    assertTrue(versionWithLatest.toString(), versionWithLatest.isValid());
    assertTrue(noVersion.toString(), noVersion.isValid());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_toString() {
    assertEquals("version:567890, State:SPECIFIC", versionWithLong.toString());
    assertEquals("version:-1, State:LATEST", versionWithLatest.toString());
    assertEquals("version:0, State:NO_VERSION", noVersion.toString());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_validate() {
    versionWithLong.validate();
    versionWithLatest.validate();
    noVersion.validate();
    // This should pass.
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_toXml() throws Exception {
    String expectedXml = getResourceAsString(TestResourcePaths.VERSION_SPECIFIC_XML.get());
    assertEquals(expectedXml, XmlUtils.toXml(versionWithLong, Version.class));

    expectedXml = getResourceAsString(TestResourcePaths.VERSION_LATEST_XML.get());
    assertEquals(expectedXml, XmlUtils.toXml(versionWithLatest, Version.class));

    expectedXml = getResourceAsString(TestResourcePaths.VERSION_NO_VERSION_XML.get());
    assertEquals(expectedXml, XmlUtils.toXml(noVersion, Version.class));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_fromXml() {
    String expectedXml = getResourceAsString(TestResourcePaths.VERSION_SPECIFIC_XML.get());
    assertEquals(versionWithLong, XmlUtils.getPojo(expectedXml, Version.class));

    expectedXml = getResourceAsString(TestResourcePaths.VERSION_LATEST_XML.get());
    assertEquals(versionWithLatest, XmlUtils.getPojo(expectedXml, Version.class));

    expectedXml = getResourceAsString(TestResourcePaths.VERSION_NO_VERSION_XML.get());
    assertEquals(noVersion, XmlUtils.getPojo(expectedXml, Version.class));
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_toJson() {
    assertEquals(expectedVersionWithLongJson, JsonUtils.toJson(versionWithLong, true));
    assertEquals(expectedVersionWithLatest, JsonUtils.toJson(versionWithLatest, true));
    assertEquals(expectedNoVersionJson, JsonUtils.toJson(noVersion, true));
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_fromJson() {
    assertEquals(versionWithLong, JsonUtils.getPojo(expectedVersionWithLongJson, Version.class));
    assertEquals(versionWithLatest, JsonUtils.getPojo(expectedVersionWithLatest, Version.class));
    assertEquals(noVersion, JsonUtils.getPojo(expectedNoVersionJson, Version.class));
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_constructor() {
    // Positive.
    assertEquals(noVersion, new Version(0L));
    assertEquals(versionWithLatest, new Version(-1L));
    new Version(1L);
    new Version(Long.MAX_VALUE);
    
    
    try {
      new Version(-2L);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    
    try {
      new Version("");
      fail("should have failed.");
    } catch (NumberFormatException e) {
      // Expected
    }
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_constructor_string() {
    doTest_createVersion();
  }
  
  public void doTest_createVersion() {
    // Positive tests.
    assertEquals(noVersion, new Version("0"));
    assertEquals(versionWithLatest, new Version("-1"));
    new Version("1");
    new Version("" + Long.MAX_VALUE);
    
    // Negative : null
    try {
      new Version("");
      fail("should have failed.");
    } catch (NumberFormatException e) {
      // Expected
    }
    
    
    // Negative : negative value
    try {
      new Version("-2");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }
}
