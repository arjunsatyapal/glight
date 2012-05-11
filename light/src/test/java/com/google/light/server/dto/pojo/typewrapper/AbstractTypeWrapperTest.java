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

/**
 * Test for {@link com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper}
 *
 * @author Arjun Satyapal
 */
public interface AbstractTypeWrapperTest {
  public void test_getValue() throws Exception;
  
  public void test_isValid() throws Exception;
  
  public void test_toString() throws Exception;
  
  public void test_validate() throws Exception;
  
  public void test_toXml() throws Exception;
  
  public void test_fromXml() throws Exception;
  
  public void test_toJson() throws Exception;
  
  public void test_fromJson() throws Exception;
  
  public void test_constructor() throws Exception;
  
  public void test_constructor_string() throws Exception;
  
//  public void test_fromXml_whenValueIsMissing() throws Exception;
//  public void test_fromJson_whenValueIsMissing() throws Exception;
}
