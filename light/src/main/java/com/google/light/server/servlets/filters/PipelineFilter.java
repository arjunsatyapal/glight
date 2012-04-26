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
package com.google.light.server.servlets.filters;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.getStackTraceAsString;
import static com.google.light.server.constants.LightConstants.LIGHT_BOT_EMAIL;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.exception.unchecked.pipelineexceptions.ignore.PipelineIgnoreException;
import com.google.light.server.exception.unchecked.pipelineexceptions.pause.PipelineStopException;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.PipelineUtils;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Arjun Satyapal
 */
public class PipelineFilter implements Filter {
  @Inject
  private PipelineFilter(Injector injector) {
    // TODO(arjuns): See if this can be put in context initializer. Or put as part of the pre-load.
    GuiceUtils.setInjector(injector);
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) resp;

    try {
      PersonManager personManager = getInstance(PersonManager.class);
      PersonEntity actor = personManager.findByEmail(LIGHT_BOT_EMAIL);
      
      checkNotNull(actor, "Person for LightBot is missing.");
//      seedEntityInRequestScope(request, PersonId.class, AnotActor.class, actor.getPersonId());

      filterChain.doFilter(request, response);
    } catch (PipelineIgnoreException e) {
      // Ignore this.
    } catch (PipelineStopException e) {
        PipelineUtils.stopPipeline(e.getPipelineId(), getStackTraceAsString(e));
    } 
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {
  }
}
