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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;

import com.google.light.server.AbstractLightServerTest;
import com.google.light.server.persistence.PersistenceToDtoInterface;
import org.junit.Test;

/**
 * This lists all the methods implemented in {@link AbstractBasicDao}. All the tests are listed in
 * sorted order as mentioned in Eclipse Outline for the class.
 * 
 * All the Tests for any DAO should inherit from this class. And if any child of
 * {@link AbstractBasicDao} adds additional methods,say FooDao.java, then test for those additional
 * methods should be added in corresponding FooDaoTest.java.
 * 
 * P : Persistence Entity Type. <br>
 * I : Id Type.
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractBasicDaoTest<D, P extends PersistenceToDtoInterface<P, D>> extends
    AbstractLightServerTest {
  private final Class<P> entityClazz;

  protected AbstractBasicDaoTest(Class<P> clazz) {
    this.entityClazz = checkNotNull(clazz);
  }

  /**
   * Test for Constructor {@link AbstractBasicDao#AbstractBasicDao(Class, Class)}
   */
  @Test
  public void test_constructor() throws Exception {
    /*
     * Since AbstractBasicDao is an abstract class, extending it so that we can create a instance of
     * desired type.
     */
    class AbstractTest extends AbstractBasicDao<D, P> {
      /*
       * Deliberately not putting types to testEntityClazz & testIdTypeClazz so that we can do
       * negative testing.
       */

      @SuppressWarnings({ "unchecked", "rawtypes" })
      public AbstractTest(Class testEntityClazz) {
        super(testEntityClazz);
      }
    }
    AbstractTest object = new AbstractTest(this.entityClazz);
    assertEquals(entityClazz.getClass(), object.getEntityClazz().getClass());
  }

  /**
   * Test for {@link AbstractBasicDao#get(Objectify, Key)}
   * Test to get Entity from Datastore within a Objectify Transaction for provided <Key>.
   */
  @Test
  public abstract void test_get_by_key_in_txn();

  /**
   * Test for {@link AbstractBasicDao#get(Objectify, Object)}.
   * Test to get Entity from datastore without Transaction for provided <Key>.
   * 
   */
  public abstract void test_get_by_key();

  /**
   * Test for {@link AbstractBasicDao#put(Objectify, Object)}
   * 
   * Test to put Entity using a Objectify Txn.
   */
  @Test
  public abstract void test_put_txn();

  /**
   * Test for {@link AbstractBasicDao#put(Object)}
   */
  @Test
  public abstract void test_put();
}
