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

import com.google.light.server.constants.LightEnvEnum;
import javax.servlet.Filter;

/**
 * Enum to Map Filter Implementations with URL Patterns.
 * TODO(arjuns): Find equivalent solution for Jersey servlets. 
 *  
 * @author Arjun Satyapal
 */
public enum FilterPathEnum {
  API(ProdServletFilter.class, true, false),
  TASK_QUEUE(PipelineFilter.class, true, true),
  TEST(TestServletFilter.class, false, true);
  
  private Class<? extends Filter> clazz;
  private boolean allowedInProd;
  private boolean allowedInNonProd;
  
  private FilterPathEnum(Class<? extends Filter> clazz, boolean allowedInProd,
      boolean allowedInNonProd) {
    this.clazz = clazz;
    this.allowedInProd = allowedInProd;
    this.allowedInNonProd = allowedInNonProd;
  }
  
  public Class<? extends Filter> getClazz() {
    return clazz;
  }
  
  public boolean isAllowedInCurrentEnv() {
    LightEnvEnum env = LightEnvEnum.getLightEnv();
    
    switch (env) {
      case PROD : return allowedInProd;
      
      case DEV_SERVER:
        //$FALL-THROUGH$
      case QA: 
        //$FALL-THROUGH$
      case UNIT_TEST :
        return allowedInNonProd;
        
      default :
        throw new IllegalStateException("unknown env : " + env);
    }
  }
}
