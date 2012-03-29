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
package com.google.light.server.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import com.googlecode.objectify.Query;
import java.util.List;

import com.googlecode.objectify.impl.conv.joda.JodaTimeConverters;

import java.util.logging.Logger;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyOpts;
import com.googlecode.objectify.ObjectifyService;

/**
 * Utility methods related to Objectify.
 * 
 * @author Arjun Satyapal
 */
public class ObjectifyUtils {
  private static final Logger logger = Logger.getLogger(ObjectifyUtils.class.getName());
  static {
    JodaTimeConverters.add(ObjectifyService.factory().getConversions());
  }

  /**
   * Method to return Objectify Options.
   * 
   * @param globalCacheEnabled : Enable global cache.
   * @param sessionCacheEnabled : Enable session cache.
   * @param txnEnabled : Enable transaction.
   * @return
   */
  public static ObjectifyOpts getObjectifyOptions(boolean globalCacheEnabled,
      boolean sessionCacheEnabled, boolean txnEnabled) {
    return new ObjectifyOpts()
        .setGlobalCache(globalCacheEnabled)
        .setSessionCache(sessionCacheEnabled)
        .setBeginTransaction(txnEnabled);
  }

  /**
   * Rollback an Objectify transaction which is in progress.
   */
  public static void rollbackTransaction(Objectify txn) {
    // If its a non-transaction, then txn.getTxn will return null.
    if (txn.getTxn() != null) {
      if (txn.getTxn().isActive()) {
        // TODO(arjuns): Add requestId once available.
        logger.info("Rolling back transaction.");
        txn.getTxn().rollback();
      }
    }
  }

  /**
   * Commit an Objectify transaction which is in progress.
   */
  public static void commitTransaction(Objectify txn) {
    try {
      // If its a non-transaction, then txn.getTxn will return null.
      if (txn.getTxn() != null) {
        txn.getTxn().commit();
      }
    } finally {
      if (txn.getTxn() != null && txn.getTxn().isActive()) {
        // TODO(arjuns): Add requestId once available.
        logger.severe("Rolling back.");
        txn.getTxn().rollback();
      }
    }
  }

  /**
   * Return a Objectify object with transaction enabled, and caches disabled.
   * 
   * @return
   */
  public static Objectify initiateTransaction() {
    ObjectifyOpts opts =
        getObjectifyOptions(false /* global-cache */, false/* session-cache */, true /* transaction */);
    return ObjectifyService.begin(opts);
  }

  /**
   * Return a Objectify object with all global-cache, session-cache and tx disabled.
   * 
   * @return
   */
  public static Objectify nonTransaction() {
    ObjectifyOpts opts =
        getObjectifyOptions(false/* global-cache */, false/* session-cache */,
            false/* transaction */);
    return ObjectifyService.begin(opts);
  }

  /**
   * Utility method to return a Objectify key for Id of type <Long>.
   * 
   * @param clazz Entity Class.
   * @param id Id of object.
   * @return
   */
  public static <T> Key<T> getKey(Class<T> clazz, Long id) {
    checkNotNull(id);

    return new Key<T>(clazz, id);
  }

  /**
   * Utility method to return a Objectify key for Id of type <String>.
   * 
   * @param clazz Entity Class.
   * @param id Id of object.
   * @return
   */
  public static <T> Key<T> getKey(Class<T> clazz, String id) {
    checkNotNull(id);

    return new Key<T>(clazz, id);
  }

  /**
   * Returns all children for a Parent Entity.
   * 
   * @param txn Objectify Transaction.
   * @param parentKey Key for Parent Entity.
   * @param clazz Type of children to be returned.
   * @return
   */
  public static <T, V> QueryResultIterable<T> getAllChildren(Objectify txn,
      @SuppressWarnings("rawtypes") Key parentKey,
      Class<T> clazz) {
    if (parentKey == null)
      throw new IllegalArgumentException();
    return txn.query(clazz).ancestor(parentKey).fetch();
  }

  /**
   * Returns an iterable for Children for a Parent Entity.
   * 
   * @param txn Objectify Transaction.
   * @param parentKey Key for Parent Entity.
   * @param clazz Type of Children.
   * @param filter Objectify Fitler.
   * @param filterValues values.
   * @return
   */
  public static <T, V> QueryResultIterable<T> getChildren(Objectify txn,
      @SuppressWarnings("rawtypes") Key parentKey,
      Class<T> clazz, String filter, Object filterValues) {
    if (parentKey == null)
      throw new IllegalArgumentException();
    return txn.query(clazz).ancestor(parentKey).filter(filter, filterValues).fetch();
  }

  /**
   * Utility method that will ensure that for a given Query, only one output exists, else
   * it will throw an errorMessage.
   * 
   * @param query
   * @param errMessage
   * @return
   */
  public static <T> T assertAndReturnUniqueEntity(Query<T> query, String errMessage) {
    List<T> tempList = Lists.newArrayList(query.iterator());

    if (tempList != null && tempList.size() > 1) {
      throw new IllegalStateException(errMessage);
    }

    if (tempList.size() == 0) {
      return null;
    }

    return tempList.get(0);
  }
  
  // Utility class.
  private ObjectifyUtils() {
  }
}
