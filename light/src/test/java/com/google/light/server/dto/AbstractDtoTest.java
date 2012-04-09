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
package com.google.light.server.dto;

import org.junit.Test;

import com.google.light.server.AbstractGAETest;

/**
 * Test for classes which implement {@link DtoInterface}.
 * 
 * @author Walter Cacau
 */
public abstract class AbstractDtoTest extends AbstractGAETest {

  /**
   * Test for constructor for each entity.
   */
  @Test
  public abstract void test_builder() throws Exception;

  /**
   * Test for {@link DtoToPersistenceInterface#toJson()}. For testing, we will read XML from file,
   * convert it to DTO, then convert it to JSON and then compare with a JSON stored in file.
   * 
   * @throws Exception
   */
  @Test
  public abstract void test_toJson() throws Exception;

  /**
   * Test for {@link DtoToPersistenceInterface#validate()}.
   */
  @Test
  public abstract void test_validate() throws Exception;

  /**
   * Test for {@link DtoToPersistenceInterface#toXml()}. For testing, we will read JSON from file,
   * convert it to DTO, then convert it to XML and then compare with a XML stored in file.
   */
  @Test
  public abstract void test_toXml() throws Exception;

}
