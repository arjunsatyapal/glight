/*
 * Copyright 2012 Google Inc.
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
 * Abstract DTO to accomodate all the common variables and methods for DTOs.
 * 
 * TODO(arjuns) : Make other DTOs to use this.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * D : DTO type.
 * P : Persistence for D.
 * I : Id for P.
 * 
 * @author Arjun @SuppressWarnings("serial")
 *         Satyapal
 */
@SuppressWarnings("serial")
public abstract class AbstractDtoToPersistence<D, P, I> extends AbstractDto<D> implements
    DtoToPersistenceInterface<D, P, I> {
  /**
   * Convert DTO to Persistence Entity.
   * 
   * I : Type of Id. This should be either Long or String as Objectify supports
   * only those type of Ids.
   * 
   * @return
   */
  @Override
  public abstract P toPersistenceEntity(I id);

  /**
   * @param builder
   */
  @SuppressWarnings("rawtypes")
  protected AbstractDtoToPersistence(BaseBuilder builder) {
    super(builder);
  }
}