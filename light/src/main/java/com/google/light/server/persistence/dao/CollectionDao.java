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
package com.google.light.server.persistence.dao;

import com.google.inject.Inject;
import com.google.light.server.dto.collection.CollectionDto;
import com.google.light.server.dto.pojo.longwrapper.CollectionId;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import java.util.logging.Logger;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class CollectionDao extends AbstractBasicDao<CollectionDto, CollectionEntity>{
private static final Logger logger = Logger.getLogger(CollectionDao.class.getName());

  static {
    ObjectifyService.register(CollectionEntity.class);
  }
  
  @Inject
  public CollectionDao() {
    super(CollectionEntity.class);
  }

  /**
   * Put Module on datastore. Id for {@link ModuleEntity} is generated by DataStore.
   * 
   * {@inheritDoc}
   */
  @Override
  public CollectionEntity put(Objectify ofy, CollectionEntity entity) {
    CollectionEntity returnEntity = super.put(ofy, entity);
    String returnMsg = "Created/Updated [" + returnEntity.getCollectionId() + "].";

    return logAndReturn(logger, returnEntity, returnMsg);
  }
  
  public CollectionEntity get(CollectionId collectionId) {
    return get(null, collectionId);
  }
  
  public CollectionEntity get(Objectify ofy, CollectionId collectionId) {
    return super.get(ofy, CollectionEntity.generateKey(collectionId));
  }
}