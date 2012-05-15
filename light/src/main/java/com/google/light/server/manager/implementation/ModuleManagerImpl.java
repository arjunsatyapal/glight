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
import static com.google.light.server.utils.LightUtils.getNow;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.dto.module.ModuleDto;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.module.ModuleVersionState;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.exception.unchecked.IdShouldNotBeSet;
import com.google.light.server.exception.unchecked.VersionMutabilityViolation;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.dao.ExternalIdMappingDao;
import com.google.light.server.persistence.dao.ModuleDao;
import com.google.light.server.persistence.dao.ModuleVersionDao;
import com.google.light.server.persistence.dao.ModuleVersionResourceDao;
import com.google.light.server.persistence.entity.module.ExternalIdMappingEntity;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionResourceEntity;
import com.google.light.server.serveronlypojos.GAEQueryWrapper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
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

  private final ExternalIdMappingDao externalIdMappingDao;

  @Inject
  public ModuleManagerImpl(ModuleDao moduleDao, ModuleVersionDao moduleVersionDao,
      ModuleVersionResourceDao moduleVersionResourceDao,
      ExternalIdMappingDao externalIdMappingDao) {
    this.moduleDao = checkNotNull(moduleDao, "moduleDao");
    this.moduleVersionDao = checkNotNull(moduleVersionDao, "moduleVersionDao");
    this.moduleVersionResourceDao = checkNotNull(moduleVersionResourceDao,
        "moduleVersionResourceDao");
    this.externalIdMappingDao = checkNotNull(externalIdMappingDao, "externalIdMappingDao");
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
  public ModuleId findModuleIdByExternalId(Objectify ofy, ExternalId externalId) {
    ExternalIdMappingEntity mapping = externalIdMappingDao.get(ofy, externalId);
    if (mapping != null) {
      return mapping.getModuleId();
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleId reserveModuleId(Objectify ofy, ExternalId externalId,
      List<PersonId> owners, String title) {
    checkTxnIsRunning(ofy);
    checkNotNull(externalId, "externalId");

    ModuleId existingModuleId = findModuleIdByExternalId(ofy, externalId);
    if (existingModuleId != null) {
      return existingModuleId;
    }

    if (StringUtils.isBlank(title)) {
      title = "Untitled : " + new DateTime(getNow()) + ":" + externalId.getValue();
    }
    
    /*
     *  Now create a new Dummy module as a placeHolder which will be later updated when content
     *  is available.
     * 
     *  TODO(arjuns): Add a cleanup job for cleaning up MOdules which dont move from Reserved state
     * for long time.
     */  
    ModuleEntity moduleEntity = new ModuleEntity.Builder()
        .title(title)
        .moduleState(ModuleState.RESERVED)
        .moduleType(externalId.getModuleType())
        .externalId(externalId)
        .owners(owners)
        .build();
    ModuleEntity persistedEntity = this.create(ofy, moduleEntity);

    ExternalIdMappingEntity mappingEntity = new ExternalIdMappingEntity.Builder()
        .externalId(externalId)
        .moduleId(persistedEntity.getModuleId())
        .build();
    externalIdMappingDao.put(ofy, mappingEntity);

    return persistedEntity.getModuleId();
  }

  /**
   * {@inheritDoc} TODO(arjuns): Make this better once we have other Docs.
   */
  @Override
  public ModuleVersionEntity publishModuleVersion(Objectify ofy, ModuleId moduleId,
      Version version, ExternalId externalId, String title, String content, String etag, 
      Instant lastEditTime) {
    checkTxnIsRunning(ofy);
    checkNotNull(moduleId, "moduleId");
    checkNotNull(version, "version");
    checkNotNull(externalId, "externalId");
    checkNotBlank(title, "title");
    checkNotBlank(content, "content");
    
    ModuleEntity moduleEntity = this.get(ofy, moduleId);
    checkNotNull(moduleEntity, "Module[" + moduleId + "] was not found.");

    ModuleVersionEntity fetchedEntity = moduleVersionDao.get(ofy, moduleId, version);
    
    ModuleVersionEntity moduleVersionEntity = new ModuleVersionEntity.Builder()
        .version(version)
        .moduleKey(moduleEntity.getKey())
        .state(ModuleVersionState.PUBLISHED)
        .title(title)
        .content(content)
        .etag(etag)
        .externalId(externalId)
        .lastEditTime(lastEditTime)
        .build();

    if (fetchedEntity != null) {
      if (fetchedEntity.equals(moduleVersionEntity)) {
        return fetchedEntity;
      } else {
        throw new VersionMutabilityViolation("Tried to modify immutable " + moduleId + ":"
            + version);
      }
    }

    moduleVersionDao.put(ofy, moduleVersionEntity);

    moduleEntity.publishVersion(version, title, lastEditTime, etag, externalId);
    moduleDao.put(ofy, moduleEntity);

    return moduleVersionEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleVersionResourceEntity publishModuleResource(Objectify ofy, ModuleId moduleId,
      Version version, String resourceId, GSBlobInfo resourceInfo) {
    checkTxnIsRunning(ofy);

    ModuleVersionResourceEntity fetchedEntity = this.getModuleResource(
        ofy, moduleId, version, resourceId);

    Key<ModuleVersionEntity> moduleVersionKey = ModuleVersionEntity.generateKey(
        moduleId, version);

    ModuleVersionResourceEntity entity = new ModuleVersionResourceEntity.Builder()
        .id(resourceId)
        .moduleVersionKey(moduleVersionKey)
        .resourceInfo(resourceInfo)
        .build();

    if (fetchedEntity != null) {
      if (fetchedEntity.equals(entity)) {
        return fetchedEntity;
      } else {
        throw new VersionMutabilityViolation("Tried to modify immutable version : "
            + moduleId + ":" + version + ":" + resourceId);
      }
    }

    ModuleVersionResourceEntity savedEntity = moduleVersionResourceDao.put(ofy, entity);

    return savedEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleVersionEntity getModuleVersion(Objectify ofy, ModuleId moduleId, Version version) {
    Version requiredVersion =
        convertLatestToLatestPublishedVersionForModule(ofy, moduleId, version);

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
    Version requiredVersion =
        convertLatestToLatestPublishedVersionForModule(ofy, moduleId, version);

    ModuleVersionResourceEntity entity = moduleVersionResourceDao.get(
        moduleId, requiredVersion, resourceId);

    return entity;
  }

  private Version convertLatestToLatestPublishedVersionForModule(Objectify ofy, ModuleId moduleId,
      Version version) {
    if (version.isLatestVersion()) {
      ModuleEntity moduleEntity = get(ofy, moduleId);

      if (moduleEntity == null) {
        throw new NotFoundException("Module[" + moduleId + "] was not found.");
      }

      return moduleEntity.getLatestPublishVersion();
    }

    return version;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Version reserveModuleVersion(Objectify ofy, ModuleId moduleId,
      String etag, Instant lastEditTime) {
    checkTxnIsRunning(ofy);
    ModuleEntity moduleEntity = this.get(ofy, moduleId);
    checkNotNull(moduleEntity, "moduleEntity");

    if (moduleEntity.getModuleType() == ModuleType.LIGHT_SYNTHETIC_MODULE) {
      if (moduleEntity.getModuleState() == ModuleState.PUBLISHED) {
        // There is atleast one version published for this Synthetic module. That is sufficient.
        return null;
      }
    } else if (moduleEntity.getLastEditTime().isEqual(lastEditTime)
        || moduleEntity.getLastEditTime().isAfter(lastEditTime)) {
      // No need to import new version.
      logger.info("Skipping creating version because for [" + moduleEntity.getModuleId()
          + "], lastEditTime[" + moduleEntity.getLastEditTime()
          + "] is equal/after lastEditTime[" + lastEditTime + "] given by Google.");
      return null;
    }

    Version reservedVersion = moduleEntity.reserveVersion();
    moduleDao.put(ofy, moduleEntity);

    return reservedVersion;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PageDto findModulesByOwnerId(PersonId ownerId, String startIndex, int maxResults) {
    GAEQueryWrapper<ModuleEntity> wrapper = moduleDao.findModulesByOwnerId(
        ownerId, startIndex, maxResults);

    List<ModuleDto> list = Lists.newArrayList();
    for (ModuleEntity currEntity : wrapper.getList()) {
      list.add(currEntity.toDto());
    }

    PageDto pageDto = new PageDto.Builder()
        .startIndex(wrapper.getStartIndex())
        .list(list)
        .handlerUri(JerseyConstants.URI_RESOURCE_PATH_MODULE_ME)
        .build();

    return pageDto;
  }
}
