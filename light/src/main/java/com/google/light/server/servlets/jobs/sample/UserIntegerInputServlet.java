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
package com.google.light.server.servlets.jobs.sample;

import static com.google.light.server.constants.RequestParamKeyEnum.PROMISE_HANDLE;
import static com.google.light.server.constants.RequestParamKeyEnum.PROMISE_VALUE;
import static com.google.light.server.utils.ServletUtils.getRequestParameterValue;

import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.light.server.servlets.AbstractLightServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class UserIntegerInputServlet extends AbstractLightServlet {
  private PipelineService service = PipelineServiceFactory.newPipelineService();

  /** 
   * {@inheritDoc}
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      String promiseHandle = getRequestParameterValue(request, PROMISE_HANDLE);
      String promiseValue = getRequestParameterValue(request, PROMISE_VALUE);
    
      service.submitPromisedValue(promiseHandle, Integer.parseInt(promiseValue));
      response.getWriter().println("done");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void doHead(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void doOptions(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

}
