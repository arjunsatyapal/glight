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
package com.google.light.server.persistence.dao;

import com.google.light.server.persistence.PersistenceToDtoInterface;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;

/**
 * A generic DAO for manipulating entities during tests when their
 * respective DAO's don't offer the operation you need. 
 * 
 * @author Walter Cacau
 */
public class TestDao {
  
  /**
   * Deletes the given entity.
   * 
   * If entity is null it does nothing.
   * 
   * @param entity
   */
  public <P, D> void delete(PersistenceToDtoInterface<P, D> entity) {
    if(entity != null) {
      Objectify ofy = ObjectifyUtils.nonTransaction();
      ofy.delete(entity);
    }
  }
  
  /**
   * Creates/updates the given entity 
   * 
   * @param entity
   */
  public <P, D> void put(PersistenceToDtoInterface<P, D> entity) {
    Objectify ofy = ObjectifyUtils.nonTransaction();
    ofy.put(entity);
  }
  
  /**
   * Deletes or updates the given entity
   * @param clazz
   * @param id
   */
  public <P, D> PersistenceToDtoInterface<P, D> get(Class<? extends PersistenceToDtoInterface<P, D>> clazz, Long id) {
    Objectify ofy = ObjectifyUtils.nonTransaction();
    return ofy.find(clazz, id);
  }
}
