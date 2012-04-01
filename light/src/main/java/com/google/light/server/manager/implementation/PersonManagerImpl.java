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
import static com.google.light.server.utils.LightPreconditions.checkValidSession;

import com.google.inject.Inject;
import com.google.light.server.exception.unchecked.IdShouldNotBeSet;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.dao.PersonDao;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.SessionManager;

/**
 * Implementation class for {@link PersonManager}
 * In constructor, we only check for Person is logged in which depends on presence of 
 * LoginProviderUserId. Unlike other managers, where Session should be valid, this Servlet is an
 * exception to that group because in PersonManager we create a Person which returns PersonId.
 * So all the methods other then createPerson should ensure that Session is valid.
 * 
 * @author Arjun Satyapal
 */
public class PersonManagerImpl implements PersonManager {
  private SessionManager sessionManager;
  private PersonDao personDao;

  @Inject
  public PersonManagerImpl(SessionManager sessionManager, PersonDao personDao) {
    this.sessionManager = checkNotNull(sessionManager, "sessionManager");
    this.personDao = checkNotNull(personDao, "personDao");
    
    sessionManager.checkPersonLoggedIn();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity create(PersonEntity entity) {
    /*
     * We don't trust client to provide PersonId at time of Creation. So it has to be set
     * on the server side.
     */
    if (entity.getId() != null) {
      throw new IdShouldNotBeSet();
    }

    // This is a heavy operation, so perform this validation just before persisting.
    PersonEntity personByEmail = getByEmail(entity.getEmail());
    
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
    
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity get(Long id) {
    /*
     * We dont check for session validity as this is used for finding person at the time of creating
     * a person.
     */
    return personDao.get(checkPersonId(id));
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity getByEmail(String email) {
    /*
     * We dont check for session validity as this is used for finding person at the time of creating
     * a person.
     */
    return personDao.getByEmail(email);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity delete(String id) {
    checkValidSession(sessionManager);
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

}
