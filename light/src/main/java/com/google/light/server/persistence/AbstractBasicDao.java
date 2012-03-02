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
package com.google.light.server.persistence;

import static com.google.common.base.Preconditions.checkArgument;

import com.googlecode.objectify.NotFoundException;

import com.google.light.server.exception.unchecked.IllegalKeyTypeException;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Each child should register the Entity with Objectify and provide a constructor to bind
 * with Guice.
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractBasicDao<T> {
  
  /**
   * Put an entity on DataStore.
   * TODO(arjuns) : Look into improving transaction.
   * Reference : http://code.google.com/p/objectify-appengine/wiki/BestPractices#Use_Pythonic_Transactions
   * 
   * @param entity
   * @return
   */
  public T put(T entity) {
    return put(null, entity);
  }
  
  /**
   * Put an entity on DataStore.
   * If you want to put an entity within a Txn, then this should be called inside a transaction.
   * And caller of this method is responsible for committing the transaction.
   * 
   * @param txn
   * @param entity
   * @return
   */
  public T put(Objectify txn, T entity) {
    if (txn == null) {
      txn = ObjectifyUtils.nonTransaction();
    } else {
      checkArgument(txn.getTxn().isActive());
    }
    txn.put(entity);
    return entity;
  }

  /**
   * Get a Objectify-Key for an id stored as String.
   * @param id
   * @return
   * @throws IllegalKeyTypeException
   */
  public abstract Key<T> getKey(String id) throws IllegalKeyTypeException;
  
  /**
   * Get a Objectify-Key for an id stored as Long.
   * 
   * @param id
   * @return
   * @throws IllegalKeyTypeException
   */
  public abstract Key<T> getKey(Long id) throws IllegalKeyTypeException;
  
  /**
   * Get an entity from DataStore. If entity is not found, then null will be returned.
   * 
   * @param id Id of object to be fetched.
   * @return
   * @throws IllegalKeyTypeException
   */
  public T get(String id) throws IllegalKeyTypeException {
      return get(null, id);
  }
  
  /**
   * Get an entity, with String Id, from DataStore with String id.
   * If entity is not found, then null will be returned.
   * 
   * @param txn Objectify Transaction.
   * @param id Id of object to be fetched.
   * @return
   * @throws IllegalKeyTypeException
   */
  public T get(Objectify txn, String id) throws IllegalKeyTypeException {
    Key<T> key = getKey(id);
    
    return get(txn, key);
  }

  /**
   * Get an entity, with Long id, from DataStore.
   * If entity is not found, then null will be returned.
   * 
   * @param txn Objectify Transaction.
   * @param id Id of object to be fetched.
   * @return
   * @throws IllegalKeyTypeException
   */
  public T get(Objectify txn, Long id) throws IllegalKeyTypeException {
    Key<T> key = getKey(id);
    
    return get(txn, key);
  }
  
  /**
   * Get an entity via Key from DataStore.
   * 
   * @param txn
   * @param key
   * @return
   */
  private T get(Objectify txn, Key<T> key) {
    if (txn == null) {
      txn = ObjectifyUtils.nonTransaction();
    } else {
      checkArgument(txn.getTxn().isActive());
    }
    
    try {
      return txn.get(key);
    } catch (NotFoundException e) {
      return null;
    }
  }
}
