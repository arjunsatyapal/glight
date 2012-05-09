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
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkTxnIsRunning;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.dto.collection.CollectionDto;
import com.google.light.server.dto.collection.CollectionState;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.tree.CollectionTreeNodeDto;
import com.google.light.server.exception.unchecked.IdShouldNotBeSet;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.persistence.dao.CollectionDao;
import com.google.light.server.persistence.dao.CollectionVersionDao;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.serveronlypojos.GAEQueryWrapper;
import com.google.light.server.utils.LightUtils;
import com.googlecode.objectify.Objectify;
import java.util.List;

/**
 * Implementation class for {@link ModuleManager}.
 * <p>
 * For perfoming Module Operations user has to be logged in, and session needs to be valid.
 * 
 * @author Arjun Satyapal
 */
public class CollectionManagerImpl implements CollectionManager {
  private final CollectionDao collectionDao;
  private final CollectionVersionDao collectionVersionDao;

  @Inject
  public CollectionManagerImpl(CollectionDao collectionDao,
      CollectionVersionDao collectionVersionDao) {
    this.collectionDao = checkNotNull(collectionDao, "collectionDao");
    this.collectionVersionDao = checkNotNull(collectionVersionDao, "collectionVersionDao");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionEntity create(Objectify ofy, CollectionEntity entity) {
    if (entity.getId() != null) {
      throw new IdShouldNotBeSet();
    }

    return collectionDao.put(ofy, entity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionEntity update(Objectify ofy, CollectionEntity updatedEntity) {
    Preconditions.checkArgument(updatedEntity.getId().isValid(),
        "Invalid CollectionId : " + updatedEntity.getId());
    return collectionDao.put(ofy, updatedEntity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionEntity get(Objectify ofy, CollectionId collectionId) {
    return collectionDao.get(ofy, collectionId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionEntity delete(CollectionId id) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionId findCollectionIdByOriginId(Objectify ofy, String originId) {
    // OriginModuleMappingEntity mapping = originModuleMappingDao.get(ofy, originId);
    // if (mapping != null) {
    // return mapping.getModuleId();
    // }
    //
    // return null;

    throw new UnsupportedOperationException();
  }

//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  public CollectionEntity reserveCollectionIdForOriginId(Objectify ofy, String originId,
//      PersonId ownerId) {
//    CollectionId existingCollectionId = null; // findCollectionIdByOriginId(ofy, originId);
//    if (existingCollectionId != null) {
//      return collectionDao.get(ofy, existingCollectionId);
//    }
//
//    // Now create a new Collection.
//    // TODO(arjuns): Add a cleanup job for cleaning up MOdules which dont move from Reserved state
//    // for long time.
//    CollectionEntity collectionEntity = new CollectionEntity.Builder()
//        .title("reserved")
//        .collectionState(CollectionState.RESERVED)
//        .owners(Lists.newArrayList(ownerId))
//        .etag("etag")
//        .build();
//    CollectionEntity persistedEntity = this.create(ofy, collectionEntity);
//
//    // OriginCollectionMappingEntity mappingEntity = new OriginModuleMappingEntity.Builder()
//    // .id(originId)
//    // .moduleId(persistedEntity.getModuleId())
//    // .build();
//    // originModuleMappingDao.put(ofy, mappingEntity);
//
//    return persistedEntity;
//  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionEntity reserveCollectionId(Objectify ofy, List<PersonId> owners, String title) {
    checkNotBlank(title, "title");
    // Now create a new Collection.
    CollectionEntity collectionEntity = new CollectionEntity.Builder()
        .title(title)
        .collectionState(CollectionState.RESERVED)
        .owners(owners)
        .build();
    CollectionEntity persistedEntity = this.create(ofy, collectionEntity);

    return persistedEntity;
  }

  /**
   * {@inheritDoc} TODO(arjuns): Make this better once we have other Docs.
   */
  @Override
  public Version reserveCollectionVersion(Objectify ofy, CollectionEntity collectionEntity) {
    checkTxnIsRunning(ofy);
    checkNotNull(collectionEntity, "collectionEntity");
    
    Version collectionVersion = collectionEntity.reserveVersion();
    collectionDao.put(ofy, collectionEntity);
    
    return collectionVersion;
  }
  
  @Override
  public CollectionVersionEntity publishCollectionVersion(Objectify ofy, 
      CollectionEntity collectionEntity, Version version, CollectionTreeNodeDto collectionRoot) {
    checkTxnIsRunning(ofy);
    checkNotNull(collectionEntity, "collectionEntity");
    
    CollectionVersionEntity collectionVersionEntity = new CollectionVersionEntity.Builder()
        .version(version)
        .collectionKey(collectionEntity.getKey())
        .collectionTree(collectionRoot)
        .creationTime(LightUtils.getNow())
        .lastUpdateTime(LightUtils.getNow())
        .build();
    collectionVersionDao.put(ofy, collectionVersionEntity);
    
    // TODO(arjuns): Add support for etag.
    collectionEntity.publishVersion(version, LightUtils.getNow(), null);
    collectionDao.put(ofy, collectionEntity);

    return collectionVersionEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionVersionEntity getCollectionVersion(Objectify ofy, CollectionId collectionId,
      Version version) {
    Version requiredVersion = convertLatestToSpecificVersion(collectionId, version);

    CollectionVersionEntity cvEntity = collectionVersionDao.get(ofy, collectionId, requiredVersion);
    return cvEntity;
  }

  private Version convertLatestToSpecificVersion(CollectionId collectionId, Version version) {
    if (version.isLatestVersion()) {
      CollectionEntity entity = get(null, collectionId);
      return entity.getLatestPublishVersion();
    }

    return version;
  }
  
  /** 
   * {@inheritDoc}
   */
  @Override
  public PageDto findCollectionsByOwnerId(PersonId ownerId, String startIndex, int maxResults) {
    GAEQueryWrapper<CollectionEntity>  wrapper = collectionDao.findCollectionsByOwnerId(
        ownerId, startIndex, maxResults);
    
    List<CollectionDto> list = Lists.newArrayList();
    for (CollectionEntity currEntity : wrapper.getList()) {
      list.add(currEntity.toDto());
    }
    
    
    PageDto pageDto = new PageDto.Builder()
        .startIndex(wrapper.getStartIndex())
        .list(list)
        .handlerUri(JerseyConstants.URI_RESOURCE_PATH_COLLECTION_ME)
        .build();
    
    return pageDto;
  }
}
