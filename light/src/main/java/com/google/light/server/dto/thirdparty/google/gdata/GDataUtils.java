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
package com.google.light.server.dto.thirdparty.google.gdata;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

import com.google.light.server.utils.LightUtils;

import com.google.gdata.data.Person;

import java.util.List;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class GDataUtils {
  public static List<String> convertListPersonToAuthors(List<Person> listOfPersons) {
    List<String> requiredList = Lists.newArrayList();
    if (LightUtils.isCollectionEmpty(listOfPersons)) {
      return requiredList;
    }
    
    for (Person curr : listOfPersons) {
      if (StringUtils.isNotBlank(curr.getEmail())) {
        requiredList.add(curr.getEmail());
      } else if (StringUtils.isNotBlank(curr.getName())) {
        requiredList.add(curr.getName());
      } else {
        throw new IllegalStateException("Not sure what to do here.");
      }
    }
    
    return requiredList;
    
    
    
  }
}
