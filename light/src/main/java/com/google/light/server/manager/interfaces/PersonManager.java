package com.google.light.server.manager.interfaces;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;

import com.google.light.server.persistence.entity.person.PersonEntity;

/**
 * Manager for operations related to a Person.
 * 
 * @author arjuns@google.com (Arjun Satyapal)
 */
public interface PersonManager {
  /**
   * Add new Person.
   * 
   * @param entity
   * @return
   * @throws EmailInUseException
   */
  public PersonEntity create(PersonEntity entity);

  /**
   * Update an existing Person.
   * 
   * @param updatedEntity
   * @return
   */
  public PersonEntity update(PersonEntity updatedEntity);

  /**
   * Get details of an existing Person by PersonId.
   * 
   * @param id
   * @return
   * @throws IllegalKeyTypeException
   */
  public PersonEntity get(PersonId id);
  
  /**
   * Get details of the current logged in person or null if no user
   * is logged in.
   * 
   * @param id
   * @return
   * @throws IllegalKeyTypeException
   */
  public PersonEntity getCurrent();

  /**
   * Get details of an existing Person by Email. Returns null if not found.
   * 
   * @param email
   * @return
   */
  public PersonEntity findByEmail(String email);

  /**
   * Delete an existing Person.
   * 
   * @param id
   * @return
   */
  public PersonEntity delete(PersonId id);
}
