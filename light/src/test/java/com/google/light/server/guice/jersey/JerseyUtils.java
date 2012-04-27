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
package com.google.light.server.guice.jersey;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import java.lang.reflect.Method;
import javax.ws.rs.Path;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class JerseyUtils {
  public static String getPath(JerseyMethodEnum jerseyMethodEnum)
      throws SecurityException, NoSuchMethodException {
    checkNotNull(jerseyMethodEnum, "jerseyMetthodEnum");

    String contextPath = JerseyConstants.JERSEY_CONTEXT_PATH;
    
    Class<? extends AbstractJerseyResource> clazz = jerseyMethodEnum.getClazz();
    Path path = clazz.getAnnotation(Path.class);
    String classPath = path.value();
    
    Method m = jerseyMethodEnum.getMethod();
    path = m.getAnnotation(Path.class);
    String methodPath = path.value();
    
    return contextPath + classPath + methodPath;
  }
}
