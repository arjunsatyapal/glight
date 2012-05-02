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

import com.google.light.server.dto.pojo.longwrapper.PersonId;

import com.google.light.server.dto.AbstractPojo;

import com.google.common.base.Preconditions;
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
  protected PersonId ownerId;

  /** Id of Person who is performing current action on behalf of Owner. */
  protected PersonId actorId;

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
      this.ownerId = ownerId;
    }
    
    if (actorId.isValid()) {
      this.actorId = actorId;
    }
  }

  public PersonId getOwnerId() {
    return ownerId;
  }

  public PersonEntity getOwner() {
    if (ownerId == null) {
      return null;
    }

    if (owner == null) {
      owner = loadPerson(ownerId);
    }

    return owner;
  }

  public PersonId getActorId() {
    return actorId;
  }

  public PersonEntity getActor() {
    if (actor == null) {
      actor = loadPerson(actorId);
      Preconditions.checkNotNull(actor, "Actor[" + actorId + "] not found");
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
    checkNotNull(actorId, "actorId");
    checkPositiveLong(actorId.get(), "actorId");

    if (ownerId != null && ownerId.get() != null) {
      checkPositiveLong(ownerId.get(), "ownerId");
    }
    return this;
  }
}
