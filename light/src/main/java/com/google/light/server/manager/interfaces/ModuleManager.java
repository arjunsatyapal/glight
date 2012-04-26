package com.google.light.server.manager.interfaces;

import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.dto.pojo.ModuleId;
import com.google.light.server.dto.pojo.PersonId;
import com.google.light.server.dto.pojo.Version;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
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
  public ModuleEntity update(ModuleEntity updatedEntity);

  /**
   * Get details of an existing Module by ModuleId.
   * 
   * @param id
   * @return
   * @throws IllegalKeyTypeException
   */
  public ModuleEntity get(ModuleId moduleId);

  /**
   * Delete an existing Module.
   * 
   * @param id
   * @return
   */
  public ModuleEntity delete(ModuleId moduleId);

  /**
   * Find Module by originId
   * 
   * @param originId
   * @return
   */
  public ModuleId findModuleIdByOriginId(Objectify ofy, String originId);

  /**
   * Reserve a ModuleId for a OriginId.
   * 
   * @param originId
   * @param ownerId
   * @return
   */
  public ModuleId reserveModuleIdForOriginId(String originId, PersonId ownerId);

  /**
   * Add ModuleVersion for GoogleDoc.
   * 
   * @param moduleId
   * @param content
   * @param docInfoDto
   * @return
   */
  public Version addModuleVersionForGoogleDoc(ModuleId moduleId, String content,
      GoogleDocInfoDto docInfoDto);

  /**
   * Add resources for a Module-Version.
   * 
   * @param moduleId
   * @param moduleVersion
   * @param resourceInfo
   * @return
   */
  public ModuleVersionResourceEntity addModuleResource(ModuleId moduleId, Version moduleVersion,
      String resourceId, GSBlobInfo resourceInfo);

  /**
   * Get Module-Version.
   * 
   * @param moduleId
   * @param moduleVersion
   * @return
   */
  public ModuleVersionEntity getModuleVersion(ModuleId moduleId, Version version);
  
  /**
   * Serve ModuleResource.
   * 
   * @param moduleId
   * @param version
   * @param resourceKey
   */
  public ModuleVersionResourceEntity getModuleResource(ModuleId moduleId, Version version,
      String resourceId);
}
