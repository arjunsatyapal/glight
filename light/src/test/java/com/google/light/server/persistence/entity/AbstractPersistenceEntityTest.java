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

import com.google.light.server.AbstractGAETest;

/**
 * Test for Entities of type {@link PersistenceToDtoInterface}
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractPersistenceEntityTest extends AbstractGAETest {
  /**
   * Test for constructor for each entity.
   */
  public abstract void test_builder_with_constructor();
  

  /**
   * Test for {@link PersistenceToDtoInterface#getKey()}.
   */
  public abstract void test_getKey();
  
  /**
   * This test is not present in the {@link PersistenceToDtoInterface} but should be present
   * in all the entities.
   */
  public abstract void test_generateKey();
  
  
  /**
   * Test for {@link PersistenceToDtoInterface#toDto()}
   */
  public abstract void test_toDto();
}
