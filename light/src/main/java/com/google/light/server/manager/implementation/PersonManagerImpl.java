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
package com.google.light.server.manager.implementation;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;
import static com.google.light.server.utils.LightPreconditions.checkPersonLoggedIn;
import static com.google.light.server.utils.LightPreconditions.checkValidSession;

import com.google.light.server.dto.pojo.longwrapper.PersonId;

import com.google.inject.Inject;
import com.google.light.server.dto.pojo.RequestScopedValues;
import com.google.light.server.exception.unchecked.IdShouldNotBeSet;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.dao.PersonDao;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.SessionManager;

/**
 * Implementation class for {@link PersonManager}. Unlike other managers, we do not check for
 * Person is logged in because this Implementation is called under various scenarios where
 * a user may/may not be logged in. So instead of validating inside a constructor, we will do
 * different validations inside different methods.
 * 
 * @author Arjun Satyapal
 */
public class PersonManagerImpl implements PersonManager {
  private PersonDao personDao;
  private SessionManager sessionManager;
  private RequestScopedValues requestScopedValues;

  @Inject
  public PersonManagerImpl(PersonDao personDao, SessionManager sessionManager,
      RequestScopedValues requestScopedValues) {
    this.personDao = checkNotNull(personDao, "personDao");
    this.sessionManager = checkNotNull(sessionManager, "sessionManager");
    this.requestScopedValues = checkNotNull(requestScopedValues, "requestScopedValues");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity create(PersonEntity entity) {
    checkPersonLoggedIn(sessionManager);
    /*
     * We don't trust client to provide PersonId at time of Creation. So it has to be set
     * on the server side.
     */
    if (entity.getPersonId() != null) {
      throw new IdShouldNotBeSet();
    }

    // This is a heavy operation, so perform this validation just before persisting.
    PersonEntity personByEmail = findByEmail(entity.getEmail());

    if (personByEmail != null) {
      return personByEmail;
    }

    return personDao.put(entity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity update(PersonEntity updatedEntity) {
    checkValidSession(sessionManager);

    // TODO(arjuns & waltercacau): How to check if an email can be associated with the account?
    // right now it is protected relying a person only can put on /api/person/me, but as soon
    // as we allow admins to change person's data we might start a security issue.

    // For now, let's not even consider a client side provided ID.
    if (updatedEntity.getPersonId() != null) {
      throw new IdShouldNotBeSet();
    }

    updatedEntity.setPersonId(sessionManager.getPersonId());

    // Overriding client side provided email
    updatedEntity.setEmail(sessionManager.getEmail());

    return personDao.put(updatedEntity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity get(PersonId personId) {
    /*
     * We dont check for session validity as this is used for finding person at the time of creating
     * a person.
     */
    return personDao.get(checkPersonId(personId));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity findByEmail(String email) {
    /*
     * We dont check for session validity as this is used for finding person at the time of creating
     * a person.
     */
    return personDao.findByEmail(email);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity delete(PersonId id) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity getCurrent() {
    PersonId personId = requestScopedValues.getOwnerId();
    
    if (personId == null) {
      return null;
    }
    return personDao.get(personId);
  }
}
