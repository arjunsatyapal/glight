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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.light.server.exception.unchecked.IllegalKeyTypeException;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Each child should register the Entity with Objectify and provide a constructor to bind with
 * Guice.
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractBasicDao<T> {

  /**
   * Put an entity on DataStore. TODO(arjuns) : Look into improving transaction. Reference :
   * http://code.google.com/p/objectify-appengine/wiki/BestPractices#Use_Pythonic_Transactions
   * 
   * @param entity
   * @return
   */
  public final T put(T entity) {
    return put(null, entity);
  }

  /**
   * Put an entity on DataStore. If you want to put an entity within a Txn, then this should be
   * called inside a transaction. And caller of this method is responsible for committing the
   * transaction.
   * 
   * @param ofy
   * @param entity
   * @return
   */
  public T put(Objectify ofy, T entity) {
    if (ofy == null) {
      ofy = ObjectifyUtils.nonTransaction();
    } else {
      checkArgument(ofy.getTxn().isActive());
    }
    ofy.put(entity);
    return entity;
  }

  /**
   * Get a Objectify-Key for an id stored as String.
   * 
   * @param id
   * @return
   * @throws IllegalKeyTypeException
   */
  protected abstract Key<T> getKey(String id) throws IllegalKeyTypeException;

  /**
   * Get a Objectify-Key for an id stored as Long.
   * 
   * @param id
   * @return
   * @throws IllegalKeyTypeException
   */
  protected abstract Key<T> getKey(Long id) throws IllegalKeyTypeException;

  /**
   * Get Entity of Type<T> from DataStore which has Id of type <String>. If some overridden behavior
   * is required, override {@link #get(Objectify, Key)}
   * 
   * @param id Id of object to be fetched.
   * @return
   * @throws IllegalKeyTypeException
   */
  public final T get(String id) throws IllegalKeyTypeException {
    return get(null, id);
  }

  /**
   * Get Entity of Type<T> from DataStore which has Long Id. If some overridden behavior is
   * required, override {@link #get(Objectify, Key)}
   * 
   * @param id Id of object to be fetched.
   * @return
   * @throws IllegalKeyTypeException
   */
  public final T get(Long id) throws IllegalKeyTypeException {
    return get(null, id);
  }

  /**
   * Get Entity of Type<T> from DataStore which has Id of type <String> in a Transaction. If some
   * overridden behavior is required, override {@link #get(Objectify, Key)}
   * 
   * @param ofy Objectify Transaction.
   * @param id Id of object to be fetched.
   * @return
   * @throws IllegalKeyTypeException
   */
  public final T get(Objectify ofy, String id) throws IllegalKeyTypeException {
    Key<T> key = getKey(id);

    return get(ofy, key);
  }

  /**
   * Get Entity of Type<T> from DataStore which has Id of type <Long> inside a transaction. If some
   * overridden behavior is required, override {@link #get(Objectify, Key)}
   * 
   * @param ofy Objectify Transaction.
   * @param id Id of object to be fetched.
   * @return
   * @throws IllegalKeyTypeException
   */
  public final T get(Objectify ofy, Long id) throws IllegalKeyTypeException {
    Key<T> key = getKey(id);

    return get(ofy, key);
  }

  /**
   * Get an entity from Datastore for given Key. If it needs to be fetched insid a transaction, then
   * Transaction should be active.
   * 
   * @param ofy
   * @param key
   * @return
   */
  public T get(Objectify ofy, Key<T> key) {
    if (ofy == null) {
      ofy = ObjectifyUtils.nonTransaction();
    } else {
      checkArgument(ofy.getTxn().isActive());
    }

    return ofy.find(key);
  }
}
