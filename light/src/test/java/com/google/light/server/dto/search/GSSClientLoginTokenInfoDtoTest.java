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
package com.google.light.server.dto.search;

import static com.google.light.testingutils.TestResourcePaths.GSS_CLIENTLOGIN_TOKEN_INFO_SAMPLE_JSON;
import static com.google.light.testingutils.TestingUtils.getResourceAsString;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.google.light.server.dto.AbstractDtoToPersistenceTest;
import com.google.light.server.exception.unchecked.BlankStringException;

/**
 * Test for {@link GSSClientLoginTokenInfoDto}
 * 
 * @author Walter Cacau
 */
public class GSSClientLoginTokenInfoDtoTest extends AbstractDtoToPersistenceTest {
  private static final Long SAMPLE_EXPIRES = 10L;
  private static final String SAMPLE_TOKEN = "SAMPLE_TOKEN";

  private GSSClientLoginTokenInfoDto.Builder getDtoBuilder() {
    return new GSSClientLoginTokenInfoDto.Builder().token(SAMPLE_TOKEN).expiresInMillis(
        SAMPLE_EXPIRES);
  }

  @Override
  @Test
  public void test_builder() throws Exception {
    // Already tested on validate
  }

  @Override
  @Test
  public void test_toJson() throws Exception {
    assertEquals(
        getResourceAsString(GSS_CLIENTLOGIN_TOKEN_INFO_SAMPLE_JSON.get()),
        getDtoBuilder().build().toJson());
  }

  @Override
  @Test
  public void test_toPersistenceEntity() throws Exception {
    // Not persisted
  }

  @Override
  @Test
  public void test_validate() throws Exception {
    // Positive test
    GSSClientLoginTokenInfoDto dto = getDtoBuilder().build();
    assertNotNull(dto);
    assertEquals(SAMPLE_TOKEN, dto.getToken());
    assertEquals(SAMPLE_EXPIRES, dto.getExpiresInMillis());

    // Negative test: token = null
    try {
      getDtoBuilder().token(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

    // Negative test: token = " "
    try {
      getDtoBuilder().token(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

    // Negative test: expiresInMillis = null
    try {
      getDtoBuilder().expiresInMillis(null).build();
      fail("should have failed.");
    } catch (NullPointerException e) {
      // expected.
    }

    // Negative test: expiresInMillis = 0
    try {
      getDtoBuilder().expiresInMillis(0L).build();
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }
  }

  @Override
  @Test
  public void test_toXml() throws Exception {
    // Not used as XML
    try {
      getDtoBuilder().build().toXml();
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // expected.
    }
  }
}
