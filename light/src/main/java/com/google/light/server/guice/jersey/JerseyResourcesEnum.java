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

import com.google.light.server.jersey.resources.AbstractJerseyResource;

import com.google.light.server.jersey.resources.thirdparty.google.GoogleDocIntegration;

import com.google.light.server.jersey.resources.ModuleResource;


/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public enum JerseyResourcesEnum {
  GOOGLE_DOC_RESOURCE(GoogleDocIntegration.class),
  MODULE_RESOURCE(ModuleResource.class);
  
  private Class<? extends AbstractJerseyResource> clazz;
  
  private JerseyResourcesEnum(Class<? extends AbstractJerseyResource> clazz) {
    this.clazz = checkNotNull(clazz, "clazz");
  }
  
  public Class<? extends AbstractJerseyResource> getClazz() {
    return clazz;
  }
}
