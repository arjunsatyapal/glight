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

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

/**
 * Enum to contain mapping between json resources and corresponding xml resources.
 * 
 * @author Arjun Satyapal
 */
public enum TestResourceMappings {
  CREATE_PERSON("/person/json/create_person_request.json",
                "/person/json/create_person_response.json",
                "/person/xml/create_person_request.xml",
                "/person/xml/create_person_response.xml");

  private String jsonReqPath;
  private String jsonResPath;

  private String xmlReqPath;
  private String xmlResPath;

  private TestResourceMappings(String jsonReqPath, String jsonResPath,
      String xmlReqPath, String xmlResPath) {
    this.jsonReqPath = checkNotBlank(jsonReqPath, "jsonReqPath");
    this.jsonResPath = checkNotBlank(jsonResPath, "jsonResPath");
    this.xmlReqPath = checkNotBlank(xmlReqPath, "xmlReqPath");
    this.xmlResPath = checkNotBlank(xmlResPath, "xmlResPath");
  }

  public String getJsonReqPath() {
    return jsonReqPath;
  }

  public String getJsonResPath() {
    return jsonResPath;
  }

  public String getXmlReqPath() {
    return xmlReqPath;
  }

  public String getXmlResPath() {
    return xmlResPath;
  }
}
