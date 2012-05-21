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
package com.google.light.server.dto.thirdparty.google.youtube;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.light.server.utils.LightUtils.isCollectionEmpty;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.google.light.server.dto.NeedsDtoValidation;
import java.util.List;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@XmlRootElement(name = "contentLicense")
public enum ContentLicense implements NeedsDtoValidation {
  
  // Standard YouTube License.
  @XmlEnumValue(value = "YOUTUBE")
  YOUTUBE(Lists.newArrayList("youtbue")),

  // CC Attribution.
  @XmlEnumValue(value = "CC")
  CC(Lists.newArrayList("CC")),

  // CC Attribution-ShareAlike
  @XmlEnumValue(value = "CC_SA")
  CC_SA(Lists.newArrayList("CC-SA")),

  // CC Attribution-NoDrivs
  @XmlEnumValue(value = "CC_ND")
  CC_ND(Lists.newArrayList("CC-ND")),

  // CC Attribution NonCommercial
  @XmlEnumValue(value = "CC_NC")
  CC_NC(Lists.newArrayList("CC-SA")),

  // CC Attribution-NonCommercial-ShareAlike
  @XmlEnumValue(value = "CC_NC_SA")
  CC_NC_SA(Lists.newArrayList("CC-NC-SA")),

  // CC Attribution-NonCommercial-NoDrivs
  @XmlEnumValue(value = "CC_NC_ND")
  CC_NC_ND(Lists.newArrayList("CC-NC-ND")),

  @XmlEnumValue(value = "UNKNOWN")
  UNKNOWN(Lists.newArrayList("unknown"));

  private List<String> listOfIdentifiers;

  public List<String> getListOfIdentifiers() {
    return listOfIdentifiers;
  }

  private ContentLicense(List<String> listIdentifiers) {
    checkArgument(!isCollectionEmpty(listIdentifiers), "listOfIdentifiers.");
    this.listOfIdentifiers = listIdentifiers;
  }

  public static ContentLicense getLicenseByIdentifier(String identifier) {
    for (ContentLicense curr : ContentLicense.values()) {
      if (curr.getListOfIdentifiers().contains(identifier)) {
        return curr;
      }
    }

    throw new EnumConstantNotPresentException(ContentLicense.class, identifier);
  }

  public static final List<ContentLicense> DEFAULT_LIGHT_CONTENT_LICENSES = newArrayList(CC);
}
