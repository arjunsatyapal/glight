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
package com.google.light.server.dto.thirdparty.google.gdata.gdoc;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.dto.module.ModuleType.getByProviderServiceAndCategory;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightUtils.decodeFromUrlEncodedString;

import com.google.common.base.Preconditions;
import com.google.light.server.dto.module.ModuleType;

/**
 * Creating a type for GoogleDoc ResourceId.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class GoogleDocResourceId {
  private ModuleType moduleType;
  private String typedResourceId;

  public GoogleDocResourceId(String typedResourceId) {
    this.typedResourceId = checkNotBlank(typedResourceId, "typedResourceId");
    
    String decodedString = decodeFromUrlEncodedString(typedResourceId);
    String errMsg = "invalid typedResourceId[" + typedResourceId + "].";
    checkArgument(decodedString.contains(":"), errMsg);
    
    String[] parts = decodedString.split(":");
    checkArgument(parts.length == 2, errMsg);
    
    moduleType = getByProviderServiceAndCategory(GOOGLE_DOC, parts[0]);
    Preconditions.checkNotNull(moduleType, errMsg);
  }

  @Override
  public String toString() {
    return getTypedResourceId();
  }
  
  public String getTypedResourceId() {
    return typedResourceId;
  }
  
  public ModuleType getModuleType() {
    return moduleType;
  }
}