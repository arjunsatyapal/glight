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

import com.googlecode.objectify.Key;

import com.google.light.server.AbstractLightServerTest;

import com.googlecode.objectify.Objectify;
import org.junit.Test;

/**
 * This lists all the methods implemented in {@link AbstractBasicDao}.
 * All the tests are listed in sorted order as mentioned in Eclipse Outline for the class.
 * 
 * All the Tests for any DAO should inherit from this class. And if any child of 
 * {@link AbstractBasicDao} adds additional methods,say FooDao.java, then test for those
 * additional methods should be added in corresponding FooDaoTest.java.
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractBasicDaoTest extends AbstractLightServerTest {
  /**
   * Test for {@link AbstractBasicDao#get(Long)}
   */
  @Test
  public abstract void test_get_longId();

  /**
   * Test for {@link AbstractBasicDao#get(Objectify, Key)} 
   */
  public abstract void test_get_txn_key();
  
  /**
   * Test for {@link AbstractBasicDao#getKey(Long)}
   */
  @Test
  public abstract void test_get_txn_longId();
  
  /**
   * Test for {@link AbstractBasicDao#getKey(String)}
   */
  @Test
  public abstract void test_get_txn_stringId();

  /**
   * Test for {@link AbstractBasicDao#get(String)}
   */
  @Test
  public abstract void test_get_stringId();

  /**
   * Test for {@link AbstractBasicDao#getKey(Long)}
   */
  @Test
  public abstract void test_getKey_long();
  
  /**
   * Test for {@link AbstractBasicDao#getKey(String)}
   */
  @Test
  public abstract void test_getKey_string();
  

  /**
   * Test for {@link AbstractBasicDao#put(Objectify, Object)}
   */
  @Test
  public abstract void test_put_txn();

  
  /**
   * Test for {@link AbstractBasicDao#put(Object)}
   */
  @Test
  public abstract void test_put();
}
