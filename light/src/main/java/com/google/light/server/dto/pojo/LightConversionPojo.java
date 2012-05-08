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
package com.google.light.server.dto.pojo;

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.dto.AbstractPojo;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class LightConversionPojo extends AbstractPojo<LightConversionPojo>{
  private String source;
  private String target;
  
  public LightConversionPojo(String source, String target) {
    this.source = source;
    this.target = target;
    validate();
  }
  
  public String getSource() {
    return source;
  }
  
  public String getTarget() {
    return target;
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public LightConversionPojo validate() {
    checkNotBlank(source, "source");
    checkNotBlank(target, "target");
    return this;
  }

  // For Jaxb.
  @SuppressWarnings("unused")
  private LightConversionPojo() {
  }
}
