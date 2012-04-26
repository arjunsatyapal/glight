/*
 * Copyright 2012 Google Inc.
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
package com.google.light.server.servlets.test.jobs;

import static com.google.appengine.api.datastore.Entity.KEY_RESERVED_PROPERTY;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Value;
import com.google.common.collect.Lists;
import com.google.light.jobs.LightJob1;
import com.google.light.jobs.LightJob2;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class DeleteJobs {
  static final Logger logger = Logger.getLogger(DeleteJobs.class.getName());
  static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public static class CreateDeleteJobs extends LightJob1<Integer> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<Integer> handler() {
      // Start with unrestricted kind query
      Query q = new Query(Query.KIND_METADATA_KIND);

      List<String> ignoreEntities = Lists.newArrayList(
          "OAuth2ConsumerCredentialEntity",
          "PersonEntity");

      List<FutureValue<Integer>> list = Lists.newArrayList();
      // Print query results
      for (Entity e : datastore.prepare(q).asIterable()) {
        String kindName = e.getKey().getName();
        if (kindName.startsWith("__") || kindName.startsWith("pipeline")|| ignoreEntities.contains(kindName)) {
          continue;
        } else {
          logger.info("Adding " + kindName + " for deletion.");
          list.add(futureCall(new DeleteKinds(), immediate(getContext()), immediate(kindName)));
        }
      }

      for (FutureValue<Integer> curr : list) {
        futureCall(new WaitJob(), immediate(getContext()), waitFor(curr));
      }
      return immediate(5);
    }
  }

  public static class DeleteKinds extends LightJob2<Integer, String> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<Integer> handler(String entityName) {
      logger.info("Received entity : " + entityName);
      Query query = new Query(entityName)
          .addFilter(KEY_RESERVED_PROPERTY, Query.FilterOperator.GREATER_THAN_OR_EQUAL, makeKindKey("a"))
          .setKeysOnly();
      // Print query results
      for (Entity e : datastore.prepare(query).asIterable()) {
        logger.info("Deleting " + e.getKey());
        datastore.delete(e.getKey());
      }

      query = new Query(entityName)
          .addFilter(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.NOT_EQUAL, makeKindKey("a"))
          .setKeysOnly();
      // Print query results
      for (Entity e : datastore.prepare(query).asIterable()) {
        logger.info("Deleting " + e.getKey());
        datastore.delete(e.getKey());
      }

      return immediate(10);
    }
  }

  public static class WaitJob extends LightJob1<Integer> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Value<Integer> handler() {
      return immediate(0);
    }
  }

  // Helper function to make key from kind name
 public static Key makeKindKey(String kind) {
    return KeyFactory.createKey(Query.KIND_METADATA_KIND, kind);
  }
}
