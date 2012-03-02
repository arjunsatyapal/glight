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

import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Injector;
import javax.servlet.http.HttpServlet;

/**
 * Base servlets for all Light Servlets. Essentially it forces all the derived servlets
 * to implement this methods. This ensures that any of the deriving classes dont miss any of these
 * REST methods.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public abstract class AbstractLightServlet extends HttpServlet {
  @Inject
  private Injector injector;
  
  protected Injector getInjector() {
    return checkNotNull(injector);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected abstract void doDelete(HttpServletRequest request, HttpServletResponse response);

  /**
   * {@inheritDoc}
   */
  @Override
  protected abstract void doGet(HttpServletRequest request, HttpServletResponse response);

  /**
   * {@inheritDoc}
   * @return 
   */
  @Override
  protected abstract long getLastModified(HttpServletRequest request);

  /**
   * {@inheritDoc}
   */
  @Override
  protected abstract void doHead(HttpServletRequest request, HttpServletResponse response);

  /**
   * {@inheritDoc}
   */
  @Override
  protected abstract void doOptions(HttpServletRequest request, HttpServletResponse response);

  /**
   * {@inheritDoc}
   */
  @Override
  protected abstract void doPost(HttpServletRequest request, HttpServletResponse response);

  /**
   * {@inheritDoc}
   */
  @Override
  protected abstract void doPut(HttpServletRequest request, HttpServletResponse response);
}
