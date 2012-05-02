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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.light.server.dto.collection.CollectionState;
import com.google.light.server.dto.pojo.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.dto.pojo.tree.CollectionTreeNode;
import com.google.light.server.exception.unchecked.IdShouldNotBeSet;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.persistence.dao.CollectionDao;
import com.google.light.server.persistence.dao.CollectionVersionDao;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.utils.LightUtils;
import com.googlecode.objectify.Objectify;

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
  public CollectionEntity get(CollectionId collectionId) {
    return collectionDao.get(collectionId);
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

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionEntity reserveCollectionIdForOriginId(Objectify ofy, String originId,
      PersonId ownerId) {
    CollectionId existingCollectionId = null; // findCollectionIdByOriginId(ofy, originId);
    if (existingCollectionId != null) {
      return collectionDao.get(ofy, existingCollectionId);
    }

    // Now create a new Collection.
    // TODO(arjuns): Add a cleanup job for cleaning up MOdules which dont move from Reserved state
    // for long time.
    CollectionEntity collectionEntity = new CollectionEntity.Builder()
        .title("reserved")
        .collectionState(CollectionState.RESERVED)
        .ownerPersonId(ownerId)
        .latestVersion(new Version(0L /* noVersion */, Version.State.NO_VERSION))
        .etag("etag")
        .build();
    CollectionEntity persistedEntity = this.create(ofy, collectionEntity);

    // OriginCollectionMappingEntity mappingEntity = new OriginModuleMappingEntity.Builder()
    // .id(originId)
    // .moduleId(persistedEntity.getModuleId())
    // .build();
    // originModuleMappingDao.put(ofy, mappingEntity);

    return persistedEntity;
  }

  /**
   * {@inheritDoc} TODO(arjuns): Make this better once we have other Docs.
   */
  @Override
  public CollectionVersionEntity addCollectionVersionForGoogleDoc(Objectify ofy,
      CollectionEntity collectionEntity, CollectionTreeNode collectionTree) {
    checkNotNull(collectionEntity, "collectionEntity");
    Version collectionVersion = collectionEntity.getLatestVersion().getNextVersion();

    CollectionVersionEntity collectionVersionEntity = new CollectionVersionEntity.Builder()
        .version(collectionVersion)
        .collectionKey(collectionEntity.getKey())
        .collectionTree(collectionTree)
        .creationTime(LightUtils.getNow())
        .lastUpdateTime(LightUtils.getNow())
        .build();
    collectionVersionDao.put(ofy, collectionVersionEntity);

    // TODO(arjuns): Add support for etag.
    collectionEntity.setLatestVersion(collectionVersion);
    collectionDao.put(ofy, collectionEntity);

    return collectionVersionEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionVersionEntity getCollectionVersion(CollectionId collectionId, Version version) {
    Version requiredVersion = convertLatestToSpecificVersion(collectionId, version);

    CollectionVersionEntity moduleVersionEntity =
        collectionVersionDao.get(collectionId, requiredVersion);
    return moduleVersionEntity;
  }

  private Version convertLatestToSpecificVersion(CollectionId collectionId, Version version) {
    if (version.isLatest()) {
      CollectionEntity entity = get(collectionId);
      return entity.getLatestVersion();
    }

    return version;
  }
}