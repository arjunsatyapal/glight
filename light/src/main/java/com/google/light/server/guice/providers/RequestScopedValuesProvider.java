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
package com.google.light.server.guice.providers;

import static com.google.light.server.utils.GuiceUtils.getInstance;

import com.google.light.server.dto.pojo.longwrapper.PersonId;

import com.google.light.server.dto.pojo.RequestScopedValues;



import com.google.inject.Provider;
import com.google.light.server.annotations.AnotActor;
import com.google.light.server.annotations.AnotOwner;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class RequestScopedValuesProvider implements Provider<RequestScopedValues>{
  /** 
   * {@inheritDoc}
   */
  @Override
  public RequestScopedValues get() {
    PersonId ownerId = getInstance(PersonId.class, AnotOwner.class);
    PersonId actorId = getInstance(PersonId.class, AnotActor.class);
    return new RequestScopedValues(ownerId, actorId);
  }

}
