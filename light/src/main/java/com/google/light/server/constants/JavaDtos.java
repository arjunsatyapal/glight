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
package com.google.light.server.constants;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.dto.pojo.longwrapper.PersonId;

import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;

import com.google.light.server.dto.person.PersonDto;

import com.google.light.server.dto.pages.PageDto;

import com.google.common.collect.Lists;
import com.google.light.server.dto.AbstractPojo;
import java.util.List;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public enum JavaDtos {
  GOOGLE_DOC_INFO_DTO(GoogleDocInfoDto.class),
  PAGE_DTO(PageDto.class),
  PERSON_ID(PersonId.class),
  PERSON_DTO(PersonDto.class);
  
  @SuppressWarnings("rawtypes")
  private Class<? extends AbstractPojo> clazz;
  
  @SuppressWarnings("rawtypes")
  private JavaDtos(Class<? extends AbstractPojo> clazz) {
    this.clazz = checkNotNull(clazz, "class");
  }
  
  @SuppressWarnings("rawtypes")
  public Class<? extends AbstractPojo> getClazz() {
    return clazz;
  }
  
  @SuppressWarnings("rawtypes")
  public static List<Class> getListOfDtoClasses() {
    List<Class> list = Lists.newArrayList();
    
    for (JavaDtos curr : JavaDtos.values()) {
      list.add(curr.getClazz());
    }
    
    return list;
  }
  
  @SuppressWarnings("rawtypes")
  public static Class[] getArrayOfDtoClasses() {
    Class[] array = new Class[JavaDtos.values().length];
    
    for (int index=0; index < JavaDtos.values().length; index++) {
      array[index] = JavaDtos.values()[index].getClazz();
    }
    
    return array;
  }
}
