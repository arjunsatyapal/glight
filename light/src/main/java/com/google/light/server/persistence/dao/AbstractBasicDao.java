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

import com.google.common.annotations.VisibleForTesting;
import com.google.light.server.exception.unchecked.IllegalKeyTypeException;
import com.google.light.server.exception.unchecked.ObjectifyTxnShouldBeRunningException;
import com.google.light.server.persistence.PersistenceToDtoInterface;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.util.logging.Logger;

/**
 * Each child should register the Entity with Objectify and provide a constructor to bind with
 * Guice.
 * 
 * D : DTO for P.
 * P : Type of PersistenceEntity.
 * I : Type of Id used by P.
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractBasicDao<D, P extends PersistenceToDtoInterface<P, D>> {
  private final Class<P> entityClazz;

  /**
   * This method is purely for testing. Should not be used in production.
   * 
   * @return
   */
  @VisibleForTesting
  protected Class<P> getEntityClazz() {
    return entityClazz;
  }

  @VisibleForTesting
  public AbstractBasicDao(Class<P> entityClazz) {
    this.entityClazz = checkNotNull(entityClazz);
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
   * Get Entity of Type<T> from DataStore which has Id of type <U>. If some overridden behavior
   * is required, override {@link #get(Objectify, Key)}
   * 
   * @param id Id of object to be fetched.
   * @return
   * @throws IllegalKeyTypeException
   */
  public P get(Key<P> key) throws IllegalKeyTypeException {
    return get(null, key);
  }

  /**
   * Get an entity from Datastore for given Key. If it needs to be fetched inside a transaction,
   * then transaction should be active.
   * 
   * @param ofy
   * @param key
   * @return
   * @throws ObjectifyTxnShouldBeRunningException
   */
  public P get(Objectify ofy, Key<P> key) {
    if (ofy == null) {
      ofy = ObjectifyUtils.nonTransaction();
    } else {
      if (!ofy.getTxn().isActive()) {
        throw new ObjectifyTxnShouldBeRunningException();
      }
    }

    return ofy.find(key);
  }

  protected static <T> T logAndReturn(Logger logger, T returnObject, String message) {
    logger.info(message);
    return returnObject;
  }
}
