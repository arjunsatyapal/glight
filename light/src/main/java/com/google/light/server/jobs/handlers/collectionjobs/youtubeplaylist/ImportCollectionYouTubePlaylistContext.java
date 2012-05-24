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
package com.google.light.server.jobs.handlers.collectionjobs.youtubeplaylist;

import static com.google.light.server.utils.LightUtils.isCollectionEmpty;
import static com.google.light.server.utils.LightUtils.replaceInstanceInList;

import com.google.common.collect.Lists;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.dto.thirdparty.google.youtube.YouTubePlaylistInfo;
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
@JsonTypeName(value = "importCollectionYouTubePlaylistContext")
@XmlRootElement(name = "importCollectionYouTubePlaylistContext")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportCollectionYouTubePlaylistContext extends AbstractDto<ImportCollectionYouTubePlaylistContext> {
  @XmlElement(name = "youTubePlaylistInfo")
  @JsonProperty(value = "youTubePlaylistInfo")
  private YouTubePlaylistInfo youTubePlaylistInfo;

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
  public ImportCollectionYouTubePlaylistContext validate() {
    return this;
  }

  public YouTubePlaylistInfo getYouTubePlaylistInfo() {
    return youTubePlaylistInfo;
  }
  
  public void setYouTubePlaylistInfo(YouTubePlaylistInfo youTubePlaylistInfo) {
    this.youTubePlaylistInfo = youTubePlaylistInfo;
  }
  
  public ExternalId getExternalId() {
    return youTubePlaylistInfo.getExternalId();
  }
  
  public List<ImportExternalIdDto> getList() {
    if (isCollectionEmpty(list)) {
      list = Lists.newArrayList();
    }

    return list;
  }

  public List<ContentLicense> getContentLicenses() {
    return contentLicenses;
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
    private YouTubePlaylistInfo youTubePlaylistInfo;
    private List<ContentLicense> contentLicenses;

    public Builder youTubePlaylistInfo(YouTubePlaylistInfo youTubePlaylistInfo) {
      this.youTubePlaylistInfo = youTubePlaylistInfo;
      return this;
    }

    public Builder contentLicenses(List<ContentLicense> contentLicenses) {
      this.contentLicenses = contentLicenses;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ImportCollectionYouTubePlaylistContext build() {
      return new ImportCollectionYouTubePlaylistContext(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private ImportCollectionYouTubePlaylistContext(Builder builder) {
    super(builder);
    this.youTubePlaylistInfo = builder.youTubePlaylistInfo;
    this.contentLicenses = builder.contentLicenses;
  }

  private ImportCollectionYouTubePlaylistContext() {
    super(null);
  }
}
