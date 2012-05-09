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
package com.google.light.pojo;

import static com.google.light.testingutils.TestingUtils.getRandomPersonId;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;


import com.google.light.server.dto.pojo.RequestScopedValues;



/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class TestRequestScopedValues extends RequestScopedValues {

  /**
   * @param ownerId
   * @param actorId
   */
  public TestRequestScopedValues(PersonId ownerId, PersonId actorId) {
    super(getRandomPersonId(), getRandomPersonId());
    // Hack to bypass checks in Participant.
    updateOwner(ownerId);
    updateActor(actorId);
  }
  
  public void updateOwner(PersonId ownerId) {
    super.setOwnerId(ownerId);
  }
  
  public void updateActor(PersonId actorId) {
    super.setActorId(actorId);
  }
  
  public void updateBoth(PersonId personId) {
    updateOwner(personId);
    updateActor(personId);
  }

}
