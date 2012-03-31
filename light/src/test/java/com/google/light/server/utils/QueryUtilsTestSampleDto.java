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
package com.google.light.server.utils;

import com.google.light.server.dto.DtoInterface;

@SuppressWarnings("serial")
/**
 * Used by {@link QueryUtilsTest} to test the QueryUtils#getDto.
 * 
 * This class could not be a private static class inside QueryUtilsTest because
 * Apache Commons BeanUtils does not work with such kind of class.
 * 
 * @author Walter Cacau
 */
public class QueryUtilsTestSampleDto implements DtoInterface<QueryUtilsTestSampleDto> {

  /**
   * Choosing private because several DTO's create private constructors to force the use of their
   * builders.
   */
  private QueryUtilsTestSampleDto() {
  }

  String field;
  int someOtherField;

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public int getSomeOtherField() {
    return someOtherField;
  }

  public void setSomeOtherField(int someOtherField) {
    this.someOtherField = someOtherField;
  }

  @Override
  public String toJson() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toXml() {
    throw new UnsupportedOperationException();
  }

  // Used for latter assert that the DTO was validated.
  private boolean wasValidated = false;

  public boolean wasValidated() {
    return wasValidated;
  }

  @Override
  public QueryUtilsTestSampleDto validate() {
    wasValidated = true;
    return this;
  }

}
