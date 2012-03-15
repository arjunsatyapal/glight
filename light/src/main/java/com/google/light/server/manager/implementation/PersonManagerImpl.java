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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import com.google.inject.Inject;
import com.google.light.server.exception.unchecked.IdShouldNotBeSet;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.dao.PersonDao;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.SessionManager;

/**
 * Implementation class for {@link PersonManager}
 * 
 * @author Arjun Satyapal
 */
public class PersonManagerImpl implements PersonManager {
  private SessionManager sessionManager;
  private PersonDao personDao;

  @Inject
  public PersonManagerImpl(SessionManager sessionManager, PersonDao personDao) {
    this.sessionManager = checkNotNull(sessionManager);
    this.personDao = checkNotNull(personDao);
    
    checkArgument(sessionManager.isUserLoggedIn());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity createPerson(PersonEntity entity) {
    // We don't trust client to provide PersonId at time of Creation. So it has to be set
    // on the server side.
    if (entity.getId() != null) {
      throw new IdShouldNotBeSet();
    }

    // TODO(arjuns) : Add test for this.
    checkArgument(entity.getIdProviderDetails() == null);

    // TODO(arjuns) : Fix this with Federated Login.
//    IdProviderDetail currIdProviderDetail = new IdProviderDetail(
//        GaeUtils.getGaeAuthDomain(),
//        GaeUtils.getGaeUserEmail(),
//        GaeUtils.getFederatedIdentity());
//    entity.addIdProviderDetail(currIdProviderDetail);

    checkArgument(entity.getEmail() == null);
    entity.setEmail(sessionManager.getEmail());
    
    // This is a heavy operation, so perform this validation just before persisting.
    PersonEntity personByEmail = getPersonByEmail(entity.getEmail());
    
    if (personByEmail != null) {
      return personByEmail;
    }

    return personDao.put(entity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity updatePerson(PersonEntity updatedEntity) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity getPerson(Long id) {
    return personDao.get(checkPersonId(id));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity getPersonByEmail(String email) {
    return personDao.getByEmail(email);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonEntity deletePerson(String id) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

}
