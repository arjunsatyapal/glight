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
package com.google.light.server.constants;

import com.google.light.server.utils.LightPreconditions;

/**
 * Enum to contain mapping between json resources and corresponding xml resources.
 * 
 * @author Arjun Satyapal
 */
public enum TestResourceMappings {
  CREATE_PERSON("/person/json/create_person.json", "/person/xml/create_person.xml");
  
  private String jsonResPath;
  private String xmlResPath;
  
  private TestResourceMappings(String jsonResPath, String xmlResPath) {
    this.jsonResPath = LightPreconditions.checkNotBlank(jsonResPath);
    this.xmlResPath = LightPreconditions.checkNotBlank(xmlResPath);
  }

  public String getJsonResPath() {
    return jsonResPath;
  }

  public String getXmlResPath() {
    return xmlResPath;
  }
}
