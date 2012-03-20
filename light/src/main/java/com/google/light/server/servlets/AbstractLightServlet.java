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
package com.google.light.server.servlets;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.guice.providers.InstanceProvider;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base servlets for all Light Servlets. Essentially it forces all the derived servlets to implement
 * this methods. This ensures that any of the deriving classes does not miss any of these REST 
 * methods. NOTE :<br>
 * * If Child class does not support a method, it should throw 
 * {@link UnsupportedOperationException}.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public abstract class AbstractLightServlet extends HttpServlet {
  @Inject
  private Injector injector;
  private InstanceProvider instanceProvider;

  protected Injector getInjector() {
    return checkNotNull(injector);
  }

  protected InstanceProvider getInstanceProvider() {
    if (instanceProvider == null) {
      this.instanceProvider = checkNotNull(injector.getInstance(InstanceProvider.class));
    }
    return instanceProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    try {
      super.service(request, response);
    } catch (Exception e) {
      // TODO(arjuns): Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract void doDelete(HttpServletRequest request, HttpServletResponse response);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract void doGet(HttpServletRequest request, HttpServletResponse response);

  /**
   * {@inheritDoc}
   * 
   * @return
   */
  @Override
  public abstract long getLastModified(HttpServletRequest request);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract void doHead(HttpServletRequest request, HttpServletResponse response);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract void doOptions(HttpServletRequest request, HttpServletResponse response);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract void doPost(HttpServletRequest request, HttpServletResponse response);

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract void doPut(HttpServletRequest request, HttpServletResponse response);
}
