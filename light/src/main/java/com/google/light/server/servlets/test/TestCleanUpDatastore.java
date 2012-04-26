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
package com.google.light.server.servlets.test;

import static com.google.light.server.utils.LightPreconditions.checkIsEnv;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.light.server.constants.LightEnvEnum;

@Deprecated
@SuppressWarnings("serial")
public class TestCleanUpDatastore extends HttpServlet {
  
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    checkIsEnv(this, LightEnvEnum.DEV_SERVER);
    super.service(req, resp);
  }
  
  private static class EntityKeyIterator implements Iterator<Key>
  {
    private Iterator<Entity> entityIter;

    public EntityKeyIterator(Iterator<Entity> iter) {
      this.entityIter = iter;
    }

    @Override
    public boolean hasNext() {
      return entityIter.hasNext();
    }

    @Override
    public Key next() {
      return entityIter.next().getKey();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    final PreparedQuery queryAll = datastore.prepare(new Query().setKeysOnly());
    datastore.delete(new Iterable<Key>() {

      @Override
      public Iterator<Key> iterator() {
        return new EntityKeyIterator(queryAll.asIterator());
      }

    });
    resp.getWriter().println("done.");
  }
}
