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
package com.google.light.server.persistence.dao;

import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import com.google.common.collect.Lists;
import com.google.light.server.exception.unchecked.EmailInUseException;
import com.google.light.server.exception.unchecked.IllegalKeyTypeException;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import java.util.List;

/**
 * 
 * @author Arjun Satyapal
 */
public class PersonDao extends AbstractBasicDao<PersonEntity> {
  static {
    ObjectifyService.register(PersonEntity.class);
  }

  @Override
  public Key<PersonEntity> getKey(String id) {
    return new Key<PersonEntity>(PersonEntity.class, id);
  }

  /**
   * {@inheritDoc} For PersonEntity, at present we are using UserId returned by AppEngine which is
   * String. Therefore KeyType for Long is not supported.
   * TODO(arjuns) : Change this to DataStore ID instead of UserId.
   */
  @Override
  protected Key<PersonEntity> getKey(Long id) throws IllegalKeyTypeException {
    throw new IllegalKeyTypeException();
  }

  public PersonEntity getByEmail(String email) {
    Objectify ofy = ObjectifyUtils.nonTransaction();

    Query<PersonEntity> filter =
        ofy.query(PersonEntity.class).filter(PersonEntity.OFY_EMAIL_QUERY_STRING, email);

    return assertAndReturnUniqueUserEntity(email, filter);
  }

  /**
   * For PersonEntity, DataStore does not generate Ids. Instead UserIds returned by AppEngine are
   * treated as Ids.
   * 
   * {@inheritDoc}
   */
  @Override
  public PersonEntity put(Objectify txn, PersonEntity entity) {
    checkPersonId(entity.getId());
    PersonEntity existingEntity = this.getByEmail(entity.getEmail());
    if (existingEntity != null) {
      /*
       * There is already an entity with given email, and a new entity is attempted to be persisted
       * with same emailId. 
       */
      if (!existingEntity.getId().equals(entity.getId()))
        throw new EmailInUseException();
    }

    return super.put(txn, entity);
  }

  /**
   * Asserts that records returned by this is <= 1. If it is < 1, then returns null.
   */
  private PersonEntity assertAndReturnUniqueUserEntity(String email, Query<PersonEntity> filter) {
    List<PersonEntity> tempList = Lists.newArrayList(filter.iterator());

    if (tempList != null && tempList.size() > 1) {
      throw new IllegalStateException("For email=[" + email + "], found [" + tempList.size()
          + "] records.");
    }

    if (tempList.size() == 0) {
      return null;
    }

    return tempList.get(0);
  }
}
