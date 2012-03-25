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
package com.google.light.server.persistence.entity;

import com.google.light.server.persistence.PersistenceToDtoInterface;
import org.junit.Test;

/**
 * Test for Entities of type {@link PersistenceToDtoInterface}
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractPersistenceEntityTest {
  /**
   * Test for constructor for each entity.
   */
  @Test
  public abstract void test_builder_with_constructor();
  
  /**
   * Test for {@link PersistenceToDtoInterface#toDto()}
   */
  @Test
  public abstract void test_toDto();
}
