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
package com.google.light.server.dto.pojo.typewrapper.longwrapper;

import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "module_id")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionId extends AbstractTypeWrapper<Long, CollectionId> {
  /**
   * @param value
   */
  public CollectionId(Long value) {
    super(value);
  }

  public CollectionId(String value) {
    this(Long.parseLong(value));
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public CollectionId validate() {
    checkPositiveLong(getValue(), "Invalid CollectionId");
    return this;
  }
  
  // For JAXB.
  private CollectionId() {
    super(null);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public CollectionId createInstance(Long value) {
    return new CollectionId(value);
  }
}
