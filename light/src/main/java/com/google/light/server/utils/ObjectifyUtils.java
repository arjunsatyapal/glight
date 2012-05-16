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
import static com.google.light.server.utils.LightUtils.getStackAsString;
import static com.google.light.server.utils.LightUtils.isListEmpty;

import com.google.common.base.Throwables;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.common.collect.Lists;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.google.light.server.serveronlypojos.GAEQueryWrapper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyOpts;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import com.googlecode.objectify.impl.conv.joda.JodaTimeConverters;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

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
  public static void rollbackTransactionIfStillActive(Objectify ofy) {
    // If its a non-transaction, then txn.getTxn will return null.
    if (ofy.getTxn() != null) {
      if (ofy.getTxn().isActive()) {
        // TODO(arjuns): Add requestId once available.
          logger.info("Rolling back transaction. Stack = " + getStackAsString());
        ofy.getTxn().rollback();
      }
    }
  }

  /**
   * Commit an Objectify transaction which is in progress.
   */
  public static void commitTransaction(Objectify ofy) {
    try {
      // If its a non-transaction, then txn.getTxn will return null.
      if (ofy.getTxn() != null) {
        ofy.getTxn().commit();
      }
      logger.info("Successfully committed");
      Thread.sleep(2000);
    } catch (Exception e) {
      logger.severe("Exception while committing : " + Throwables.getStackTraceAsString(e));
      LightUtils.wrapIntoRuntimeExceptionAndThrow(e);
    }finally {
      if (ofy.getTxn() != null && ofy.getTxn().isActive()) {
        // TODO(arjuns): Add requestId once available.
        logger.severe("Rolling back.");
        ofy.getTxn().rollback();
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

  /**
   * Helper method to fetch all Child entities for a given parent.
   */
  @SuppressWarnings("rawtypes")
  public static <T, V extends AbstractPersistenceEntity> QueryResultIterable<T> getAllChildren(
      Objectify ofy, Key<V> parentKey, Class<T> clazz) {
    return getChildren(ofy, parentKey, clazz, null/* filter */, null /* value */);
  }

  /**
   * Helper method to fetch Child entities for a given Parent.
   * 
   * TODO(arjuns) : Move other getChildren use cases to use this.
   * 
   * @param ofy
   * @param parent
   * @param clazz
   * @param filter
   * @param value
   * @return
   */
  @SuppressWarnings("rawtypes")
  public static <T, V extends AbstractPersistenceEntity> QueryResultIterable<T> getChildren(
      Objectify ofy, Key<V> parentKey, Class<T> clazz, String filter, Object value) {
    if (parentKey == null)
      throw new IllegalArgumentException();

    Query<T> query = ofy.query(clazz).ancestor(parentKey);

    if (StringUtils.isNotBlank(filter)) {
      checkNotNull(value, "When filter is set, value cannot be blank.");
      query.filter(filter, value);
    }

    return query.fetch();
  }

  public static <T, O> GAEQueryWrapper<T> findQueryResultsByPage(Objectify ofy, 
      Class<T> clazz, String filterKey, List<O> listOfValues, String startIndex, int maxResults) {
    Query<T> query = ofy.query(clazz).filter(filterKey, listOfValues);
    
    if (StringUtils.isNotBlank(startIndex)) {
      query.startCursor(Cursor.fromWebSafeString(startIndex));
    }
    
    QueryResultIterator<T> iterator = query.fetch().iterator();
    
    List<T> listOfRecords = Lists.newArrayList();
    for (int counter = 1; counter <= maxResults && iterator.hasNext(); counter++) {
      listOfRecords.add(iterator.next());
    }

    GAEQueryWrapper<T> wrapper = new GAEQueryWrapper<T>();
    
    if(!isListEmpty(listOfRecords) && iterator.hasNext()) {
      wrapper.setStartIndex(iterator.getCursor().toWebSafeString());
    }
    wrapper.setList(listOfRecords);
    
    return wrapper;
  }

  // Utility class.
  private ObjectifyUtils() {
  }
}
