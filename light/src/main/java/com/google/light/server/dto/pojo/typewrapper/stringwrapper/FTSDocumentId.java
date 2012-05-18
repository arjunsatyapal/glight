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
package com.google.light.server.dto.pojo.typewrapper.stringwrapper;

import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import com.google.light.server.utils.LightPreconditions;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class FTSDocumentId extends AbstractTypeWrapper<String, FTSDocumentId> {
  /**
   * @param value
   */
  public FTSDocumentId(String value) {
    super(value);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public FTSDocumentId createInstance(String value) {
    throw new UnsupportedOperationException();
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public FTSDocumentId validate() {
    LightPreconditions.checkNotBlank(getValue(), "value");
    return this;
  }
  
  // For JAXB
  @SuppressWarnings("unused")
  private FTSDocumentId() {
    super();
  }
}
