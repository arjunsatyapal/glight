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
package com.google.light.server.guice.provider;

import com.google.light.server.dto.pojo.RequestScopedValues;

import com.google.light.pojo.TestRequestScopedValues;


import com.google.common.base.Preconditions;



import com.google.inject.Provider;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class TestRequestScopedValuesProvider implements Provider<RequestScopedValues>{
  private TestRequestScopedValues testParticipants;
  public TestRequestScopedValuesProvider(TestRequestScopedValues testParticipants) {
    this.testParticipants = Preconditions.checkNotNull(testParticipants);
  }
  
  
  public TestRequestScopedValues getParticipants() {
    return testParticipants;
  }
  /** 
   * {@inheritDoc}
   */
  @Override
  public RequestScopedValues get() {
    return testParticipants;
  }
}
