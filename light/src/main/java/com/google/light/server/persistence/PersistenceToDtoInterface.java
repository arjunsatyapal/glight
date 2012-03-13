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
package com.google.light.server.persistence;

import java.io.Serializable;

/**
 * Light POJO which needs to be converted to and from DTO needs to implement this Interface. D : DTO
 * : Data Transfer Object which will be converted to JSON/XML depending on API. P : Persistence
 * Entity.
 * 
 * @author Arjun Satyapal
 */
public interface PersistenceToDtoInterface<P, D> extends Serializable {
  /**
   * Convert Persistence entity to corresponding DTO.
   * 
   * @return
   */
  public D toDto();

  @Override
  public boolean equals(Object obj);

  @Override
  public int hashCode();

  @Override
  public String toString();
}
