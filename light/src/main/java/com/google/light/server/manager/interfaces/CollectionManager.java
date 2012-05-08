package com.google.light.server.manager.interfaces;

import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.dto.pojo.tree.CollectionTreeNodeDto;
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
   * Find Collection by originId
   * 
   * @param originId
   * @return
   */
  public CollectionId findCollectionIdByOriginId(Objectify ofy, String originId);

  /**
   * Reserve a CollectionId for a OriginId.
   * 
   * @param originId
   * @param ownerId
   * @return
   */
  public CollectionEntity reserveCollectionIdForOriginId(Objectify ofy, String originId, PersonId ownerId);
  
  /**
   * Reserve a CollectionId.
   * 
   * @param originId
   * @param ownerId
   * @return
   */
  public CollectionEntity reserveCollectionId(Objectify ofy, PersonId ownerId, String title);

  /**
   * Add CollectionVersion for GoogleDoc.
   */
  public CollectionVersionEntity addCollectionVersionForGoogleDoc(Objectify ofy, 
      CollectionEntity collectionEntity, CollectionTreeNodeDto collectionRoot);

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
