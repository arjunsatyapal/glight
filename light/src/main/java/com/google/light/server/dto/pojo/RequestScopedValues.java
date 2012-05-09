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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;

import com.google.light.server.dto.AbstractPojo;
import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class RequestScopedValues extends AbstractPojo<RequestScopedValues> {
  /** Id of Person who is responsible for the current action */
  protected Long ownerId;

  /** Id of Person who is performing current action on behalf of Owner. */
  protected Long actorId;

  // These entities are lazily initialized.
  private PersonEntity owner;
  private PersonEntity actor;

  public boolean isValid() {
    try {
      validate();
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  public RequestScopedValues(PersonId ownerId, PersonId actorId) {
    if (ownerId.isValid()) {
      this.ownerId = getWrapperValue(ownerId);
    }

    if (actorId.isValid()) {
      this.actorId = getWrapperValue(actorId);
    }
  }

  public PersonId getOwnerId() {
    if (ownerId == null) {
      return null;
    }
    
    return new PersonId(ownerId);
  }

  public void setOwnerId(PersonId ownerId) {
    this.ownerId = getWrapperValue(ownerId);
  }

  public PersonEntity getOwner() {
    if (ownerId == null) {
      return null;
    }

    if (owner == null) {
      owner = loadPerson(getOwnerId());
      checkNotNull(owner, "owner[" + getOwnerId() + "] not found");
    }

    return owner;
  }

  public PersonId getActorId() {
    if (actorId == null) {
      return null;
    }
    
    return new PersonId(actorId);
  }

  public void setActorId(PersonId actorId) {
    this.actorId = getWrapperValue(actorId);
  }

  public PersonEntity getActor() {
    if (actorId  == null) {
      return null;
    }
    
    if (actor == null) {
      actor = loadPerson(getActorId());
      checkNotNull(actor, "Actor[" + getActorId() + "] not found");
    }

    return actor;
  }

  /**
   * Ensures that Person is Logged In.
   * 
   * TODO(arjuns): Add test for this.
   */
  public void checkPersonLoggedIn() {
    if (!isPersonLoggedIn()) {
      throw new PersonLoginRequiredException("");
    }
  }

  /**
   * Returns true if Person is logged in. In order to determine if User is logged in or not, we
   * depend on whether Session is null or not.
   * 
   * TODO(arjuns): Add test.
   * 
   * @return
   */
  public boolean isPersonLoggedIn() {
    if (actorId == null) {
      return false;
    }

    return true;
  }

  public boolean isPerformerWatcher() {
    if (ownerId == null) {
      return false;
    }

    return ownerId == actorId;
  }

  public boolean isPerformerBot() {

    return isBot(getActor().getEmail());
  }

  public static boolean isBot(String email) {
    return email.equals("light-bot@myopenedu.com");
  }

  private PersonEntity loadPerson(PersonId personid) {
    PersonManager personManager = getInstance(PersonManager.class);
    return personManager.get(personid);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RequestScopedValues validate() {
    checkNotNull(getActorId(), "actorId");
    checkPositiveLong(actorId, "actorId");

    if (ownerId != null && getOwnerId() != null) {
      checkPositiveLong(ownerId, "ownerId");
    }
    return this;
  }
}
