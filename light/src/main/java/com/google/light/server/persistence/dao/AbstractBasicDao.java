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
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Logger;

import com.google.light.server.persistence.PersistenceToDtoInterface;

import com.google.common.annotations.VisibleForTesting;
import com.google.light.server.exception.unchecked.IllegalKeyTypeException;
import com.google.light.server.exception.unchecked.ObjectifyTransactionShouldBeRunning;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

/**
 * Each child should register the Entity with Objectify and provide a constructor to bind with
 * Guice.
 * 
 * D : DTO for P.
 * P : PersistenceEntity Type.
 * I : Type of Id used by T.
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractBasicDao<D, P extends PersistenceToDtoInterface<P, D>, I> {
  private final Class<P> entityClazz;
  private final Class<I> idTypeClazz;

  /**
   * This method is purely for testing. Should not be used in production.
   * @return
   */
  @VisibleForTesting
  protected Class<P> getEntityClazz() {
    return entityClazz;
  }

  /**
   * This method is purely for testing. Should not be used in production.
   * @return
   */
  @VisibleForTesting
  protected Class<I> getIdTypeClazz() {
    return idTypeClazz;
  }

  @VisibleForTesting
  public AbstractBasicDao(Class<P> entityClazz, Class<I> idTypeClazz) {
    this.entityClazz = checkNotNull(entityClazz);
    this.idTypeClazz = checkNotNull(idTypeClazz);
    
    // Objectify allows Ids of Type Long and String only.
    if (idTypeClazz.isAssignableFrom(Long.class) ||
        idTypeClazz.isAssignableFrom(String.class)) {
      return;
    } else {
      throw new IllegalKeyTypeException(idTypeClazz.getName());
    }
  }
  
  /**
   * Put an entity on DataStore. TODO(arjuns) : Look into improving transaction. Reference :
   * http://code.google.com/p/objectify-appengine/wiki/BestPractices#Use_Pythonic_Transactions
   * 
   * @param entity
   * @return
   */
  public final P put(P entity) {
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
  public P put(Objectify ofy, P entity) {
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
  protected Key<P> getKey(I id) {
    if (idTypeClazz.isAssignableFrom(Long.class)) {
      return new Key<P>(entityClazz, (Long)id);
    } else if (id instanceof String) {
      return new Key<P>(entityClazz, (String)id);
    } else {
      throw new IllegalKeyTypeException();
    }
  }

  /**
   * Get Entity of Type<T> from DataStore which has Id of type <U>. If some overridden behavior
   * is required, override {@link #get(Objectify, Key)}
   * 
   * @param id Id of object to be fetched.
   * @return
   * @throws IllegalKeyTypeException
   */
  public final P get(I id) throws IllegalKeyTypeException {
    return get(null, id);
  }

  /**
   * Get Entity of Type<T> from DataStore which has Id of type <String> in a Transaction. If Entity
   * Id of type <String> then DAO should overload it and make it public.
   * 
   * @param ofy Objectify Transaction.
   * @param id Id of object to be fetched.
   * @return
   * @throws IllegalKeyTypeException
   */
  protected final P get(Objectify ofy, I id) throws IllegalKeyTypeException {
    Key<P> key = getKey(id);

    return get(ofy, key);
  }

  /**
   * Get an entity from Datastore for given Key. If it needs to be fetched inside a transaction, then
   * Transaction should be active.
   * 
   * @param ofy
   * @param key
   * @return
   * @throws ObjectifyTransactionShouldBeRunning 
   */
  public P get(Objectify ofy, Key<P> key) {
    if (ofy == null) {
      ofy = ObjectifyUtils.nonTransaction();
    } else {
      if (!ofy.getTxn().isActive()) {
        throw new ObjectifyTransactionShouldBeRunning();
      }
    }

    return ofy.find(key);
  }
  
  protected static <T> T logAndReturn(Logger logger, T returnObject, String message) {
    logger.info(message);
    return returnObject;
  }
}
