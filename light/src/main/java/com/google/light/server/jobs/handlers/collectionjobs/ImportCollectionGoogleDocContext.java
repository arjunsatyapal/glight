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
package com.google.light.server.jobs.handlers.collectionjobs;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;
import static com.google.light.server.utils.LightUtils.isCollectionEmpty;
import static com.google.light.server.utils.LightUtils.replaceInstanceInList;

import com.google.common.collect.Lists;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "importCollectionGoogleDocContext")
@XmlRootElement(name = "importCollectionGoogleDocContext")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportCollectionGoogleDocContext extends AbstractDto<ImportCollectionGoogleDocContext> {
  @XmlElement(name = "title")
  @JsonProperty(value = "title")
  private String title;

  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private ExternalId externalId;

  @XmlElement(name = "gdocInfo")
  @JsonProperty(value = "gdocInfo")
  private GoogleDocInfoDto gdocInfo;

  public GoogleDocInfoDto getGdocInfo() {
    return gdocInfo;
  }

  public List<ContentLicense> getContentLicenses() {
    return contentLicenses;
  }

  @XmlElementWrapper(name = "list")
  @XmlElement(name = "item")
  @JsonProperty(value = "list")
  private List<ImportExternalIdDto> list;
  
  @XmlElementWrapper(name = "contentLicenses")
  @XmlAnyElement
  @JsonProperty(value = "contentLicenses")
  private List<ContentLicense> contentLicenses;

  /**
   * {@inheritDoc}
   */
  @Override
  public ImportCollectionGoogleDocContext validate() {
    if (!isCollectionEmpty(getList())) {
      ImportExternalIdDto.doValidationForList(getList(), false /* isRequest */);
    }
    checkNotNull(externalId, "externalId");
    checkNotBlank(title, "title");
    checkNotEmptyCollection(contentLicenses, "contentLicenses");
    checkNotNull(gdocInfo, "gdocInfo");
    return this;
  }

  public String getTitle() {
    return title;
  }

  public ExternalId getExternalId() {
    return externalId;
  }

  public GoogleDocInfoDto getGDocInfo() {
    return gdocInfo;
  }

  public List<ImportExternalIdDto> getList() {
    if (isCollectionEmpty(list)) {
      list = Lists.newArrayList();
    }

    return list;
  }

  public ImportExternalIdDto findImportExternalIdDtoByExternalId(ExternalId externalId) {
    for (ImportExternalIdDto curr : getList()) {
      if (curr.getExternalId().equals(externalId)) {
        return curr;
      }
    }

    return null;
  }

  public void addImportModuleDto(ImportExternalIdDto importExternalIdDto) {
    ImportExternalIdDto existingDto = findImportExternalIdDtoByExternalId(
        importExternalIdDto.getExternalId());

    if (existingDto != null) {
      replaceInstanceInList(getList(), existingDto, importExternalIdDto);
    } else {
      list.add(importExternalIdDto);
    }
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private ExternalId externalId;
    private String title;
    private List<ContentLicense> contentLicenses;
    private GoogleDocInfoDto gdocInfo;

    public Builder externalId(ExternalId externalId) {
      this.externalId = externalId;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }
    
    public Builder contentLicenses(List<ContentLicense> contentLicenses) {
      this.contentLicenses = contentLicenses;
      return this;
    }

    public Builder gdocInfo(GoogleDocInfoDto gdocInfo) {
      this.gdocInfo = gdocInfo;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ImportCollectionGoogleDocContext build() {
      return new ImportCollectionGoogleDocContext(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private ImportCollectionGoogleDocContext(Builder builder) {
    super(builder);
    this.externalId = builder.externalId;
    this.title = builder.title;
    this.contentLicenses = builder.contentLicenses;
    this.gdocInfo = builder.gdocInfo;
  }

  private ImportCollectionGoogleDocContext() {
    super(null);
  }
}
