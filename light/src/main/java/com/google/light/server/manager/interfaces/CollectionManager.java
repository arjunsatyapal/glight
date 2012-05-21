package com.google.light.server.manager.interfaces;

import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;

import com.google.light.server.dto.collection.CollectionState;

import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;

import java.util.List;

import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.googlecode.objectify.Objectify;

/**
 * Manager for operations related to a Collection.
 * 
 * @author arjuns@google.com (Arjun Satyapal)
 */
public interface CollectionManager {
  /**
   * Add new Collection.
   * 
   * @param entity
   * @return
   * @throws EmailInUseException
   */
  public CollectionEntity create(Objectify ofy, CollectionEntity entity);

  /**
   * Update an existing Collection.
   * 
   * @param updatedEntity
   * @return
   */
  public CollectionEntity update(Objectify ofy, CollectionEntity updatedEntity);

  /**
   * Get details of an existing Collection by CollectionId.
   * 
   * @param id
   * @return
   * @throws IllegalKeyTypeException
   */
  public CollectionEntity get(Objectify ofy, CollectionId collectionId);

  /**
   * Delete an existing Collection.
   * 
   * @param id
   * @return
   */
  public CollectionEntity delete(CollectionId collectionId);

  /**
   * Reserve a CollectionId.
   */
  public CollectionEntity reserveCollectionId(Objectify ofy, List<PersonId> owners,
      CollectionTreeNodeDto collectionTree, List<ContentLicense> contentLicenses);

  /**
   * Add CollectionVersion for GoogleDoc.
   */
  public Version reserveCollectionVersion(Objectify ofy, CollectionId collectionId, 
      List<ContentLicense> contentLicenses);
  
  public Version reserveCollectionVersionFirst(Objectify ofy, CollectionEntity collectionEntity); 
  
  /**
   * Add CollectionVersion.
   */
  public Version reserveAndPublishAsLatest(Objectify ofy, CollectionId collectionId,
      CollectionTreeNodeDto collectionRoot, CollectionState collectionState);
  
  public CollectionVersionEntity getLatestPublishedVersion(Objectify ofy, CollectionId collectionId);
  
  public CollectionEntity createEmptyCollection(Objectify ofy, List<PersonId> owners, 
      CollectionTreeNodeDto collectionTree, List<ContentLicense> contentLicenses);

  public CollectionVersionEntity publishCollectionVersion(Objectify ofy,
      CollectionId collectionId, Version version, CollectionTreeNodeDto collectionRoot, 
      CollectionState collectionState);

  /**
   * Get Collection-Version.
   * 
   * @param collectionid
   * @param collectionVersion
   * @return
   */
  public CollectionVersionEntity getCollectionVersion(Objectify ofy, CollectionId collectionId,
      Version version);

  public PageDto findCollectionsByOwnerId(PersonId ownerId, String startIndex, int maxResult);
}
