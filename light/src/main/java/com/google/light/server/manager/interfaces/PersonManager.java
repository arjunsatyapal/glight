package com.google.light.server.manager.interfaces;

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
  public PersonEntity createPerson(PersonEntity entity);

  /**
   * Update an existing Person.
   * 
   * @param updatedEntity
   * @return
   */
  public PersonEntity updatePerson(PersonEntity updatedEntity);

  /**
   * Get details of an existing Person by PersonId.
   * 
   * @param id
   * @return
   * @throws IllegalKeyTypeException
   */
  public PersonEntity getPerson(Long id);

  /**
   * Get details of an existing Person by Email. Returns null if not found.
   * 
   * @param email
   * @return
   */
  public PersonEntity getPersonByEmail(String email);

  /**
   * Delete an existing Person.
   * 
   * @param id
   * @return
   */
  public PersonEntity deletePerson(String id);
}
