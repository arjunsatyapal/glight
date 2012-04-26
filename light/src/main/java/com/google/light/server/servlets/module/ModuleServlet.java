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
package com.google.light.server.servlets.module;

import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightUtils.wrapIntoRuntimeExceptionAndThrow;

import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.pojo.ModuleId;
import com.google.light.server.dto.pojo.Version;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.servlets.AbstractLightServlet;
import com.google.light.server.utils.ServletUtils;
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
public class ModuleServlet extends AbstractLightServlet {
  private ModuleManager moduleManager;

  /**
   * {@inheritDoc}
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    moduleManager = getInstance(ModuleManager.class);

    try {
      super.service(request, response);
    } catch (Exception e) {
      // TODO(arjuns): Auto-generated catch block
      wrapIntoRuntimeExceptionAndThrow(e);
    }
  }

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
      String moduleIdStr =
          ServletUtils.getRequestParameterValue(request, RequestParamKeyEnum.MODULE_ID);
      checkNotBlank(moduleIdStr, "moduleId");
      ModuleId moduleId = new ModuleId(moduleIdStr);

      String moduleVersionStr =
          ServletUtils.getRequestParameterValue(request, RequestParamKeyEnum.MODULE_VERSION);
      checkNotBlank(moduleIdStr, "moduleVersion");
      Version moduleVersion = Version.createVersion(moduleVersionStr);

      ModuleVersionEntity moduleVersionEntity =
          moduleManager.getModuleVersion(moduleId, moduleVersion);

      response.setContentType(ContentTypeEnum.TEXT_HTML.get());
      response.getWriter().println(moduleVersionEntity.getContent());
    } catch (Exception e) {
      // TODO(arjuns): ADd exception handling.
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
