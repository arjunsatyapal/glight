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
package com.google.light.jobs;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.GuiceUtils.enqueueRequestScopedVariables;
import static com.google.light.server.utils.GuiceUtils.getInstance;

import com.google.appengine.tools.pipeline.Job4;
import com.google.appengine.tools.pipeline.Value;
import com.google.light.server.dto.pojo.LightJobContextPojo;
import com.google.light.server.manager.interfaces.JobManager;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public abstract class LightJob4<T, T2, T3, T4> extends Job4<T, LightJobContextPojo, T2, T3, T4> implements
    BasicJobMethods {
  private LightJobContextPojo context;
  protected JobManager jobManager;

  @Override
  public LightJobContextPojo getContext() {
    return context;
  }

  @Override
  public String getPipelineId() {
    return getPipelineKey().getName();
  }

  @Override
  public void bootStrapGuice() {
    enqueueRequestScopedVariables(context.getOwnerId(), context.getActorId());
    jobManager = getInstance(JobManager.class);
  }

  @Override
  public Value<T> run(LightJobContextPojo context, T2 param2, T3 param3, T4 param4) {
    this.context = checkNotNull(context);

    bootStrapGuice();

    return handler(param2, param3, param4);
  }

  /**
   * @param param3
   * @return
   */
  public abstract Value<T> handler(T2 param2, T3 param3, T4 param4);
}
