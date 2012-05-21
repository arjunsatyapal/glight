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
package com.google.light.server.dto.pojo.typewrapper;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.inject.Inject;
import com.google.light.server.dto.AbstractPojo;
import com.google.light.server.dto.NeedsDtoValidation;
import javax.xml.bind.annotation.XmlElement;

/**
 * I : Type of Primitive Type.
 * T : Wrapper Type.
 * 
 * TODO(arjuns) : Move other Long Wrappers to use this.
 * TODO(arjuns): Add test for this.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public abstract class AbstractTypeWrapper<I, T> extends AbstractPojo<T> implements NeedsDtoValidation{
  @XmlElement(name = "value", nillable=true)
  @JsonProperty(value = "value")
  private I value;

  protected AbstractTypeWrapper(I value) {
    this.value = value;
    validate();
  }
  
  // For JAXB.
  @Inject
  protected AbstractTypeWrapper() {
  }
  
  public I getValue() {
    return value;
  }
  
  // Deliberately protected so that child classes can manipulate as they want but no external one.
  protected void setValue(I value) {
    this.value = value;
  }
  
  // TODO(arjuns): Get rid of this.
  @Deprecated
  public abstract T createInstance(I value);

  public boolean isValid() {
    try {
      validate();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ":" + getValue();
  }

  @Override
  public abstract T validate();
}
