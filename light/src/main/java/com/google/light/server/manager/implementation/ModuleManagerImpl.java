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
import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.module.ModuleVersionState;
import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.exception.unchecked.IdShouldNotBeSet;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.dao.ModuleDao;
import com.google.light.server.persistence.dao.ModuleVersionDao;
import com.google.light.server.persistence.dao.ModuleVersionResourceDao;
import com.google.light.server.persistence.dao.OriginModuleMappingDao;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionResourceEntity;
import com.google.light.server.persistence.entity.module.OriginModuleMappingEntity;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.util.List;
import java.util.logging.Logger;
import org.joda.time.Instant;

/**
 * Implementation class for {@link ModuleManager}.
 * <p>
 * For perfoming Module Operations user has to be logged in, and session needs to be valid.
 * 
 * @author Arjun Satyapal
 */
public class ModuleManagerImpl implements ModuleManager {
  private static final Logger logger = Logger.getLogger(ModuleManagerImpl.class.getName());

  private final ModuleDao moduleDao;
  private final ModuleVersionDao moduleVersionDao;
  private final ModuleVersionResourceDao moduleVersionResourceDao;

  private final OriginModuleMappingDao originModuleMappingDao;

  @Inject
  public ModuleManagerImpl(ModuleDao moduleDao, ModuleVersionDao moduleVersionDao,
      ModuleVersionResourceDao moduleVersionResourceDao,
      OriginModuleMappingDao originModuleMappingDao) {
    this.moduleDao = checkNotNull(moduleDao, "moduleDao");
    this.moduleVersionDao = checkNotNull(moduleVersionDao, "moduleVersionDao");
    this.moduleVersionResourceDao = checkNotNull(moduleVersionResourceDao,
        "moduleVersionResourceDao");
    this.originModuleMappingDao = checkNotNull(originModuleMappingDao, "originModuleMappingDao");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleEntity create(Objectify ofy, ModuleEntity entity) {
    /*
     * We don't trust client to provide PersonId at time of Creation. So it has to be set
     * on the server side.
     */
    if (entity.getModuleId() != null) {
      throw new IdShouldNotBeSet();
    }

    return moduleDao.put(ofy, entity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleEntity update(ModuleEntity updatedEntity) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleEntity get(Objectify ofy, ModuleId moduleId) {
    return moduleDao.get(ofy, moduleId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleEntity delete(ModuleId id) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleId findModuleIdByOriginId(Objectify ofy, String originId) {
    OriginModuleMappingEntity mapping = originModuleMappingDao.get(ofy, originId);
    if (mapping != null) {
      return mapping.getModuleId();
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleId reserveModuleIdForExternalId(ModuleType moduleType, String externalId,
      List<PersonId> owners) {
    Objectify ofy = ObjectifyUtils.initiateTransaction();

    try {
      ModuleId existingModuleId = findModuleIdByOriginId(ofy, externalId);
      if (existingModuleId != null) {
        return existingModuleId;
      }

      // Now create a new Module.
      // Creating a dummy module to get Id.
      // TODO(arjuns): Add a cleanup job for cleaning up MOdules which dont move from Reserved state
      // for long time.
      ModuleEntity moduleEntity = new ModuleEntity.Builder()
          .title("reserved")
          .state(ModuleState.RESERVED)
          .type(moduleType)
          .owners(owners)
          .latestVersion(new Version(0L /* noVersion */, Version.State.NO_VERSION))
          .lastEditTime(new Instant(0L))
          .etag("etag")
          .build();
      ModuleEntity persistedEntity = this.create(ofy, moduleEntity);

      OriginModuleMappingEntity mappingEntity = new OriginModuleMappingEntity.Builder()
          .id(externalId)
          .moduleId(persistedEntity.getModuleId())
          .build();
      originModuleMappingDao.put(ofy, mappingEntity);

      ObjectifyUtils.commitTransaction(ofy);

      return persistedEntity.getModuleId();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      if (ofy.getTxn().isActive()) {
        ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
      }
    }
  }
  
  /**
   * {@inheritDoc} TODO(arjuns): Make this better once we have other Docs.
   */
  @Deprecated
  @Override
  public ModuleVersionEntity addModuleVersionForGoogleDoc(ModuleId moduleId, String content,
      GoogleDocInfoDto docInfoDto) {
    // TODO(arjuns): Handle ofy txn.
    Objectify ofy = ObjectifyUtils.initiateTransaction();

    try {
      Key<ModuleEntity> moduleKey = ModuleEntity.generateKey(moduleId);
      ModuleEntity moduleEntity = moduleDao.get(ofy, moduleKey);
      checkNotNull(moduleEntity, "moduleEntity not found for moduleId[" + moduleId + "].");
      Version moduleVersion = moduleEntity.getLatestVersion().getNextVersion();

      ModuleVersionEntity moduleVersionEntity = new ModuleVersionEntity.Builder()
          .version(moduleVersion)
          .moduleKey(moduleKey)
          .content(content)
          .creationTime(LightUtils.getNow())
          .lastUpdateTime(LightUtils.getNow())
          .build();
      moduleVersionDao.put(ofy, moduleVersionEntity);

      moduleEntity.setTitle(docInfoDto.getTitle());
      moduleEntity.setEtag(docInfoDto.getEtag());
      moduleEntity.setLatestVersion(moduleVersion);
      moduleDao.put(ofy, moduleEntity);

      ObjectifyUtils.commitTransaction(ofy);
      return moduleVersionEntity;
    } finally {
      if (ofy.getTxn().isActive()) {
        ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
      }
    }
  }

  /**
   * {@inheritDoc} TODO(arjuns): Make this better once we have other Docs.
   */
  @Override
  public ModuleVersionEntity publishModuleVersion(ModuleId moduleId, Version version,
      String content, GoogleDocInfoDto docInfo) {
    // TODO(arjuns): Handle ofy txn.
    Objectify ofy = ObjectifyUtils.initiateTransaction();

    try {
      ModuleVersionEntity moduleVersionEntity = moduleVersionDao.get(ofy, moduleId, version);
      moduleVersionEntity.setContent(content);
      moduleVersionEntity.setTitle(docInfo.getTitle());
      moduleVersionEntity.setEtag(docInfo.getEtag());
      
      moduleVersionDao.put(ofy, moduleVersionEntity);

      ObjectifyUtils.commitTransaction(ofy);
      return moduleVersionEntity;
    } finally {
      if (ofy.getTxn().isActive()) {
        ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleVersionResourceEntity addModuleResource(ModuleId moduleId, Version moduleVersion,
      String resourceId, GSBlobInfo resourceInfo) {
    // TODO(arjuns): Handle ofy txn.
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      Key<ModuleVersionEntity> moduleVersionKey =
          ModuleVersionEntity.generateKey(moduleId, moduleVersion);

      ModuleVersionResourceEntity entity = new ModuleVersionResourceEntity.Builder()
          .id(resourceId)
          .moduleVersionKey(moduleVersionKey)
          .resourceInfo(resourceInfo)
          .build();
      ModuleVersionResourceEntity savedEntity = moduleVersionResourceDao.put(ofy, entity);

      ObjectifyUtils.commitTransaction(ofy);

      return savedEntity;
    } finally {
      if (ofy.getTxn().isActive()) {
        ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleVersionEntity getModuleVersion(Objectify ofy, ModuleId moduleId, Version version) {
    Version requiredVersion = convertLatestToSpecificVersion(ofy, moduleId, version);

    ModuleVersionEntity moduleVersionEntity = moduleVersionDao.get(ofy, moduleId, requiredVersion);
    return moduleVersionEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleVersionResourceEntity getModuleResource(Objectify ofy, ModuleId moduleId,
      Version version,
      String resourceId) {
    Version requiredVersion = convertLatestToSpecificVersion(ofy, moduleId, version);

    ModuleVersionResourceEntity entity =
        moduleVersionResourceDao.get(moduleId, requiredVersion, resourceId);
    if (entity == null) {
      throw new NotFoundException("Resource[" + resourceId + " Not found for " + moduleId + ":"
          + version);
    }

    return entity;
  }

  private Version convertLatestToSpecificVersion(Objectify ofy, ModuleId moduleId,
      Version version) {
    if (version.isLatest()) {
      ModuleEntity moduleEntity = get(ofy, moduleId);
      return moduleEntity.getLatestVersion();
    }

    return version;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Version reserveModuleVersionForImport(
      ModuleId moduleId, String etag, Instant lastEditTime) {
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      ModuleEntity moduleEntity = this.get(ofy, moduleId);
      Preconditions.checkNotNull(moduleEntity, "moduleEntity");

      System.out.println("Module update time : " + moduleEntity.getLastEditTime());
      System.out.println("Google update time : " + lastEditTime);
      if (moduleEntity.getLastEditTime().isEqual(lastEditTime)
          || moduleEntity.getLastEditTime().isAfter(lastEditTime)) {
        // No need to import new version.
        logger.info("Skipping creating version because for [" + moduleEntity.getModuleId()
            + "], lastEditTime[" + moduleEntity.getLastEditTime()
            + "] is equal/after lastEditTime[" + lastEditTime + "] given by Google.");
        return null;
      }

      Version version = moduleEntity.getLatestVersion().getNextVersion();

      // TODO(arjuns): differentiate latest and published version.
      moduleEntity.setLatestVersion(version);
      // moduleEntity.setLastEditTime(lastEditTime);
      moduleDao.put(ofy, moduleEntity);

      ModuleVersionEntity moduleVersionEntity = new ModuleVersionEntity.Builder()
          .version(version)
          .moduleKey(moduleEntity.getKey())
          .state(ModuleVersionState.RESERVED)
          .etag(etag)
          .lastEditTime(lastEditTime)
          .build();

      moduleVersionDao.put(ofy, moduleVersionEntity);

      ObjectifyUtils.commitTransaction(ofy);
      return moduleVersionEntity.getVersion();
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }
}
