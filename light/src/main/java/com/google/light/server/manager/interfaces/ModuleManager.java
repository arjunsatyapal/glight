package com.google.light.server.manager.interfaces;

import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;

import com.google.light.server.serveronlypojos.GAEQueryWrapper;

import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;

import com.google.light.server.dto.pages.PageDto;

import org.joda.time.Instant;

import java.util.List;

import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionResourceEntity;
import com.googlecode.objectify.Objectify;

/**
 * Manager for operations related to a Module.
 * 
 * @author arjuns@google.com (Arjun Satyapal)
 */
public interface ModuleManager {
  /**
   * Add new Module.
   * 
   * @param entity
   * @return
   * @throws EmailInUseException
   */
  public ModuleEntity create(Objectify ofy, ModuleEntity entity);

  /**
   * Update an existing Module.
   * 
   * @param updatedEntity
   * @return
   */
  public ModuleEntity put(Objectify ofy, ModuleEntity updatedEntity);

  /**
   * Get details of an existing Module by ModuleId.
   * 
   * @param id
   * @return
   * @throws IllegalKeyTypeException
   */
  public ModuleEntity get(Objectify ofy, ModuleId moduleId);

  /**
   * Delete an existing Module.
   * 
   * @param id
   * @return
   */
  public ModuleEntity delete(ModuleId moduleId);

  /**
   * Find Module by externalId
   */
  public ModuleId findModuleIdByExternalId(Objectify ofy, ExternalId externalId);

  /**
   * Reserve a ModuleId for a ExternalId.
   */
  public ModuleId reserveModuleId(Objectify ofy, ExternalId externalId,
      List<PersonId> owners, String title, List<ContentLicense> contentLicenses);

  public ModuleEntity reserveModule(Objectify ofy, ExternalId externalId,
      List<PersonId> owners, String title, List<ContentLicense> contentLicenses);

  public Version reserveModuleVersion(Objectify ofy, ModuleId moduleId, String etag,
      Instant lastEditTime, List<ContentLicense> contentLicenses);
  
  public Version reserveModuleVersionFirst(Objectify ofy, ModuleEntity moduleEntity);

  public ModuleVersionEntity publishModuleVersion(Objectify ofy, ModuleId moduleId,
      Version version, ExternalId externalId, String title, String content, 
      List<ContentLicense> contentLicenses, String etag, Instant lastEditTime);

  /**
   * Add resources for a Module-Version.
   * 
   * @param moduleId
   * @param moduleVersion
   * @param resourceInfo
   * @return
   */
  public ModuleVersionResourceEntity publishModuleResource(Objectify ofy, ModuleId moduleId,
      Version version, String resourceId, GSBlobInfo resourceInfo);

  /**
   * Get Module-Version.
   * 
   * @param moduleId
   * @param moduleVersion
   * @return
   */
  public ModuleVersionEntity getModuleVersion(Objectify ofy, ModuleId moduleId, Version version);

  /**
   * Serve ModuleResource.
   * 
   * @param moduleId
   * @param version
   * @param resourceKey
   */
  public ModuleVersionResourceEntity getModuleResource(Objectify ofy, ModuleId moduleId,
      Version version, String resourceId);

  public PageDto findModulesByOwnerId(PersonId ownerId, String startIndex, int maxResults);

  public GAEQueryWrapper<ModuleVersionEntity> findModuleVersionsForFTSIndexUpdate(int maxResults,
      String startIndex);

  public GAEQueryWrapper<ModuleVersionEntity> findModuleVersionsForGSSIndexUpdate(int maxResults,
      String startIndex);

}
