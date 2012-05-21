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
import static com.google.light.server.dto.thirdparty.google.youtube.ContentLicense.DEFAULT_LIGHT_CONTENT_LICENSES;
import static com.google.light.server.utils.LightPreconditions.checkTxnIsRunning;
import static com.google.light.server.utils.LightUtils.createCollectionVersionEntity;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.dto.collection.CollectionDto;
import com.google.light.server.dto.collection.CollectionState;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
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
import org.joda.time.Instant;

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
    checkArgument(updatedEntity.getCollectionId().isValid(),
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
  public CollectionEntity reserveCollectionId(Objectify ofy, List<PersonId> owners,
      CollectionTreeNodeDto collectionTree, List<ContentLicense> contentLicenses) {
    checkTxnIsRunning(ofy);
    checkNotNull(collectionTree, "collectionTree");

    Instant now = LightUtils.getNow();
    CollectionState collectionState = CollectionState.RESERVED;
    // Now create a new Collection.
    CollectionEntity collectionEntity = LightUtils.createCollectionEntity(
        collectionState, now, owners, collectionTree.getTitle(), contentLicenses);
    this.create(ofy, collectionEntity);

    Version reservedVersion = collectionEntity.reserveVersion();
    this.update(ofy, collectionEntity);

    CollectionVersionEntity cvEntity = LightUtils.createCollectionVersionEntity(
        collectionEntity.getKey(), collectionState, collectionTree, contentLicenses, now,
        reservedVersion);
    collectionVersionDao.put(ofy, cvEntity);

    return collectionEntity;
  }

  /**
   * {@inheritDoc} TODO(arjuns): Make this better once we have other Docs.
   */
  @Override
  public Version reserveCollectionVersion(Objectify ofy, CollectionId collectionId,
      List<ContentLicense> contentLicenses) {
    checkTxnIsRunning(ofy);
    checkNotNull(collectionId, "collectionId");

    CollectionEntity collectionEntity = this.get(ofy, collectionId);
    checkNotNull(collectionEntity, "collectionEntity");

    Version reservedVersion = collectionEntity.reserveVersion();
    
    CollectionTreeNodeDto dummyRoot = LightUtils.getDummyCollectionRoot(
        collectionEntity.getTitle(), "No Description");
    
    Instant now = LightUtils.getNow();
    // Reserving a dummy collection Version.
    CollectionVersionEntity cvEntity = LightUtils.createCollectionVersionEntity(
        collectionEntity.getKey(), CollectionState.RESERVED, dummyRoot, contentLicenses, now,
        reservedVersion);
    
    collectionVersionDao.put(ofy, cvEntity);
    collectionDao.put(ofy, collectionEntity);

    return reservedVersion;
  }
  
  /**
   * {@inheritDoc} TODO(arjuns): Make this better once we have other Docs.
   */
  @Override
  public Version reserveCollectionVersionFirst(Objectify ofy, CollectionEntity collectionEntity) {
    checkTxnIsRunning(ofy);
    checkNotNull(collectionEntity, "collectionEntity");
    checkArgument(collectionEntity.getState() == CollectionState.RESERVED, 
        "This method is a helper method so that first version can be reserved in same transaction "
            + "where collection was created and therefore expected state is : " 
            + CollectionState.RESERVED);

    checkNotNull(collectionEntity, "collectionEntity");

//    collectionEntity.reserveVersion();
//    this.update(ofy, collectionEntity);

    Version reservedVersion = new Version(Version.DEFAULT_FIRST_VERSION);
    return reservedVersion;
  }

  @Override
  public CollectionVersionEntity publishCollectionVersion(Objectify ofy,
      CollectionId collectionId, Version version, CollectionTreeNodeDto collectionRoot,
      CollectionState collectionState) {
    checkTxnIsRunning(ofy);

    CollectionEntity collectionEntity = this.get(ofy, collectionId);
    checkNotNull(collectionEntity, "Collection[" + collectionId + "] was not found.");

    CollectionVersionEntity fetchedEntity = collectionVersionDao.get(ofy, collectionId, version);
    checkNotNull(fetchedEntity, "There should be atleast a dummy version.");

    Instant now = LightUtils.getNow();
    
    CollectionVersionEntity cvEntity = LightUtils.createCollectionVersionEntity(
        collectionEntity.getKey(), collectionState, collectionRoot, 
        fetchedEntity.getContentLicenses(), now, version);

    if (fetchedEntity != null && !fetchedEntity.isMutable()) {
      if (fetchedEntity.equals(cvEntity)) {
        return fetchedEntity;
      } else {
        throw new VersionMutabilityViolation("Tried to modify : " + collectionId + ":"
            + version);
      }
    }
    return doPublishVersion(ofy, version, collectionRoot, collectionEntity, cvEntity);
  }

  /**
   * @param ofy
   * @param version
   * @param collectionRoot
   * @param collectionEntity
   * @param cvEntity
   * @return
   */
  private CollectionVersionEntity doPublishVersion(Objectify ofy, Version version,
      CollectionTreeNodeDto collectionRoot, CollectionEntity collectionEntity,
      CollectionVersionEntity cvEntity) {
    checkTxnIsRunning(ofy);
    collectionVersionDao.put(ofy, cvEntity);

    // TODO(arjuns): Add support for etag.
    collectionEntity.publishVersion(version, LightUtils.getNow(), null/*etag*/);
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

    if (requiredVersion.isNoVersion()) {
      return null;
    }

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

  @Override
  public CollectionEntity createEmptyCollection(Objectify ofy, final List<PersonId> owners,
      CollectionTreeNodeDto collectionTree, List<ContentLicense> contentLicenses) {
    Instant now = LightUtils.getNow();

    
    CollectionEntity collectionEntity = this.reserveCollectionId(
        ofy, owners, collectionTree, contentLicenses);
    Version reservedVersion = this.reserveCollectionVersionFirst(ofy, collectionEntity);
    
    collectionEntity.publishVersion(reservedVersion, now, null);
    this.update(ofy, collectionEntity);

    CollectionVersionEntity cvEntity = createCollectionVersionEntity(
        collectionEntity.getKey(), CollectionState.PUBLISHED, collectionTree, 
        contentLicenses, now, reservedVersion);
    collectionVersionDao.put(ofy, cvEntity);

    return collectionEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Version reserveAndPublishAsLatest(Objectify ofy, CollectionId collectionId,
      CollectionTreeNodeDto collectionTree, CollectionState collectionState) {
    checkTxnIsRunning(ofy);

    Instant now = LightUtils.getNow();
    CollectionEntity collectionEntity = this.get(ofy, collectionId);
    Version reserveVersion = collectionEntity.reserveVersion();

    this.update(ofy, collectionEntity);

    CollectionVersionEntity cvEntity = LightUtils.createCollectionVersionEntity(
        collectionEntity.getKey(), collectionState, collectionTree, DEFAULT_LIGHT_CONTENT_LICENSES,
        now, reserveVersion);
    this.doPublishVersion(ofy, reserveVersion, collectionTree, collectionEntity, cvEntity);
    return reserveVersion;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionVersionEntity getLatestPublishedVersion(
      Objectify ofy, CollectionId collectionId) {
    checkTxnIsRunning(ofy);
    CollectionEntity collectionEntity = this.get(ofy, collectionId);
    return this.getCollectionVersion(ofy, collectionId, collectionEntity.getLatestPublishVersion());
  }
}
