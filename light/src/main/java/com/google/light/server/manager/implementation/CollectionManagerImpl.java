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

import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.dto.collection.CollectionDto;
import com.google.light.server.dto.collection.CollectionState;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.exception.unchecked.IdShouldNotBeSet;
import com.google.light.server.exception.unchecked.VersionMutabilityViolation;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
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
    if (entity.getCollectionId() != null) {
      throw new IdShouldNotBeSet();
    }

    return collectionDao.put(ofy, entity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionEntity update(Objectify ofy, CollectionEntity updatedEntity) {
    Preconditions.checkArgument(updatedEntity.getCollectionId().isValid(),
        "Invalid CollectionId : " + updatedEntity.getCollectionId());
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
  public Version reserveCollectionVersion(Objectify ofy, CollectionId collectionId) {
    checkTxnIsRunning(ofy);
    checkNotNull(collectionId, "collectionId");
    
    CollectionEntity collectionEntity = this.get(ofy, collectionId);
    checkNotNull(collectionEntity, "collectionEntity");

    Version reservedVersion = collectionEntity.reserveVersion();
    collectionDao.put(ofy, collectionEntity);

    return reservedVersion;
  }

  @Override
  public CollectionVersionEntity publishCollectionVersion(Objectify ofy,
      CollectionId collectionId, Version version, CollectionTreeNodeDto collectionRoot) {
    checkTxnIsRunning(ofy);
    
    CollectionEntity collectionEntity = this.get(ofy, collectionId); 
    checkNotNull(collectionEntity, "Collection[" + collectionId + "] was not found.");
    
    CollectionVersionEntity fetchedEntity = collectionVersionDao.get(ofy, collectionId, version);

    CollectionVersionEntity cvEntity = new CollectionVersionEntity.Builder()
        .version(version)
        .title(collectionRoot.getTitle())
        .collectionKey(collectionEntity.getKey())
        .collectionTree(collectionRoot)
        .creationTime(LightUtils.getNow())
        .lastUpdateTime(LightUtils.getNow())
        .build();
    
    if (fetchedEntity != null) {
      if (fetchedEntity.equals(cvEntity)) {
        return fetchedEntity;
      } else {
        throw new VersionMutabilityViolation("Tried to modify : " + collectionId + ":" 
            + version); 
      }
    }
    collectionVersionDao.put(ofy, cvEntity);

    // TODO(arjuns): Add support for etag.
    collectionEntity.publishVersion(version, LightUtils.getNow(), null);
    collectionEntity.setTitle(collectionRoot.getTitle());
    
    collectionDao.put(ofy, collectionEntity);

    return cvEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionVersionEntity getCollectionVersion(Objectify ofy, CollectionId collectionId,
      Version version) {
    Version requiredVersion =
        convertLatestToLatestPublishedVersionForCollection(collectionId, version);

    CollectionVersionEntity cvEntity = collectionVersionDao.get(ofy, collectionId, requiredVersion);
    return cvEntity;
  }

  private Version convertLatestToLatestPublishedVersionForCollection(CollectionId collectionId,
      Version version) {
    if (version.isLatestVersion()) {
      CollectionEntity entity = get(null, collectionId);
      if (entity == null) {
        throw new NotFoundException("Collection [" + collectionId + "] was not found.");
      }
      return entity.getLatestPublishVersion();
    }

    return version;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PageDto findCollectionsByOwnerId(PersonId ownerId, String startIndex, int maxResults) {
    GAEQueryWrapper<CollectionEntity> wrapper = collectionDao.findCollectionsByOwnerId(
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
