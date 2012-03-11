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

import java.util.logging.Logger;

import com.google.light.server.dto.person.PersonDto;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.light.server.exception.unchecked.EmailInUseException;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import java.util.List;

/**
 * 
 * @author Arjun Satyapal
 */
public class PersonDao extends AbstractBasicDao<PersonDto, PersonEntity, Long> {
  private static final Logger logger = Logger.getLogger(PersonDao.class.getName());

  @Inject
  public PersonDao() {
    super(PersonEntity.class, Long.class);
  }

  static {
    ObjectifyService.register(PersonEntity.class);
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
    PersonEntity existingEntity = this.getByEmail(entity.getEmail());
    if (existingEntity != null) {
      /*
       * There is already an entity with given email, and a new entity is attempted to be persisted
       * with same emailId.
       */
      if (!existingEntity.getId().equals(entity.getId()))
        throw new EmailInUseException();
    }
    boolean isCreate = false;
    if (entity.getId() == null) {
      isCreate = false;
    }
      
    PersonEntity returnEntity = super.put(txn, entity);
    String returnMsg = "";
    
    if (isCreate) {
      returnMsg = "Created PersonEntity[" + returnEntity.getId() + "].";
    } else {
      returnMsg = "Updated PersonEntity[" + returnEntity.getId() + "].";
    }
    
    return logAndReturn(logger, returnEntity, returnMsg);
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