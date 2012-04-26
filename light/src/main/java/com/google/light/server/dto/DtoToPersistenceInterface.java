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


/**
 * Light POJO which needs to be converted to and from JSON needs to implement this Interface. <br>
 * D : DTO : Data Transfer Object which will be converted to JSON/XML depending on API.<br>
 * P : Persistence Entity.
 * I : Type of Id used by P (Persistence Entity).
 * 
 * TODO(arjuns): Remove this in favour of {@link AbstractDtoToPersistence}.
 * 
 * @author Arjun Satyapal
 */
public interface DtoToPersistenceInterface<D, P, I> extends DtoInterface<D> {

  /**
   * Convert DTO to Persistence Entity.
   * 
   * T : Type of Id. This should be either Long or String as Objectify supports
   * only those type of Ids.
   * 
   * @return
   */
  public P toPersistenceEntity(I id);
}
