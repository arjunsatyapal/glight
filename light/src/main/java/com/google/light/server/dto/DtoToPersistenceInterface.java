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
package com.google.light.server.dto;

import java.io.Serializable;

/**
 * Light POJO which needs to be converted to and from JSON needs to implement this Interface. D :
 * DTO : Data Transfer Object which will be converted to JSON/XML depending on API. P : Persistence
 * Entity.
 * 
 * D : DTO P : Persistence Entity.
 * 
 * TODO(arjuns): Refactor this into DtoInterface, and DtoToPersistence interface.
 * 
 * @author Arjun Satyapal
 */
public interface DtoToPersistenceInterface<D, P> extends Serializable {

  /**
   * Convert DTO to JSON String.
   */
  public String toJson();

  /**
   * Convert DTO to Persistence Entity.
   * 
   * @return
   */
  public P toPersistenceEntity(Long id);
  
  /**
   * Ensure that DTO is valid. TODO(arjuns): Throw a checked exception. TODO(arjuns) : Sync with
   * Walter on refactoring. Issue : 2003.
   * 
   * @return
   */
  public D validate();
  
  /**
   * Convert DTO to XML.
   */
  public String toXml();

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj);

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode();

  /**
   * {@inheritDoc}
   * 
   * @return
   */
  @Override
  public String toString();
}
