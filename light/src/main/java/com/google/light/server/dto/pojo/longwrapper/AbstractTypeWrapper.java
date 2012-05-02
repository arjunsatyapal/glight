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
package com.google.light.server.dto.pojo.longwrapper;

import javax.xml.bind.annotation.XmlElement;

import com.google.light.server.dto.AbstractPojo;

/**
 * TODO(arjuns) : Move other Long Wrappers to use this.
 * TODO(arjuns): Add test for this.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public abstract class AbstractTypeWrapper<I, T> extends AbstractPojo<T> {
  @XmlElement private I value;

  protected AbstractTypeWrapper(I value) {
    this.value = value;
  }

  public I getValue() {
    return value;
  }

  public boolean isValid() {
    return value != null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ":" + getValue();
  }

  @Override
  public abstract T validate();
}
