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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.servlets.thirdparty.google.youtube.YouTubeUtils.getContentLicenseFromVideoEntry;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.light.server.dto.module.ModuleType;

import com.google.light.server.dto.thirdparty.google.gdata.GDataUtils;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.urls.YouTubeUrl;
import com.google.light.server.utils.LightUtils;
import java.util.List;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.Instant;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * TODO(arjuns): See if we should reuse keywords.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "youTubeVideoInfo")
@XmlRootElement(name = "youTubeVideoInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class YouTubeVideoInfo extends AbstractDto<YouTubeVideoInfo> {
  @XmlElement(name = "id")
  @JsonProperty(value = "id")
  private String id;

  @XmlElement(name = "publishTimeInMillis")
  @JsonProperty(value = "publishTimeInMillis")
  private Long publishTimeInMillis;

  @XmlElement(name = "lastUpdateTimeInMillis")
  @JsonProperty(value = "lastUpdateTimeInMillis")
  private Long lastUpdateTimeInMillis;

  @XmlElement(name = "title")
  @JsonProperty(value = "title")
  private String title;

  @XmlElement(name = "contentType")
  @JsonProperty(value = "contentType")
  private ContentTypeEnum contentType;

  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private ExternalId externalId;

  @XmlElementWrapper(name = "owners")
  @XmlElement(name = "ytUsername")
  @JsonProperty(value = "owners")
  private List<String> owners;

  @XmlElement(name = "description")
  @JsonProperty(value = "description")
  private String description;

  @XmlElement(name = "durationInSec")
  @JsonProperty(value = "durationInSec")
  private Long durationInSec;

  @XmlElement(name = "videoId")
  @JsonProperty(value = "videoId")
  private String videoId;

  @XmlElementWrapper(name = "contentLicenses")
  @XmlAnyElement
  @JsonProperty(value = "contentLicenses")
  private List<ContentLicense> contentLicenses;

  @XmlElement(name = "embeddable")
  @JsonProperty(value = "embeddable")
  private Boolean embeddable;

  @XmlElement(name = "incomplete")
  @JsonProperty(value = "incomplete")
  private Boolean incomplete;

  @XmlElement(name = "isPrivate")
  @JsonProperty(value = "isPrivate")
  private Boolean isPrivate;

  @XmlElement(name = "moduleState")
  @JsonProperty(value = "moduleState")
  private ModuleState moduleState;

  @XmlElement(name = "aspectRatio")
  @JsonProperty(value = "aspectRatio")
  private AspectRatio aspectRatio;
  
  @XmlElement(name = "moduleType")
  @JsonProperty(value = "moduleType")
  private ModuleType moduleType;

  /**
   * {@inheritDoc}
   */
  @Override
  public YouTubeVideoInfo validate() {
    checkNotBlank(id, "id");
    checkNotNull(getPublishTime());
    checkNotNull(getLastUpdated());
    checkNotNull(externalId, "externalId");

    checkNotBlank(title, "title");
    checkNotNull(durationInSec, "durationInSec");
    checkNotNull(contentType, "contentType");
    
    checkNotNull(moduleType, "mopduleType");

    return this;
  }

  public String getId() {
    return id;
  }

  public Instant getPublishTime() {
    checkNotNull(publishTimeInMillis, "publishTimeInMillis");
    return new Instant(publishTimeInMillis);
  }

  public Instant getLastUpdated() {
    checkNotNull(lastUpdateTimeInMillis, "lastUpdateTimeInMillis");
    return new Instant(lastUpdateTimeInMillis);
  }

  public String getTitle() {
    return title;
  }

  public ContentTypeEnum getContentType() {
    return contentType;
  }

  public ExternalId getExternalId() {
    return externalId;
  }

  public List<String> getOwners() {
    return owners;
  }

  public String getDescription() {
    return description;
  }

  public Long getDurationInSec() {
    return durationInSec;
  }

  public String getVideoId() {
    return videoId;
  }

  public List<ContentLicense> getContentLicenses() {
    return contentLicenses;
  }

  public ModuleType getModuleType() {
    return moduleType;
  }

  public Boolean isEmbeddable() {
    return embeddable;
  }

  public Boolean isIncomplete() {
    return incomplete;
  }

  public Boolean isPrivate() {
    return isPrivate;
  }

  public ModuleState getModuleState() {
    return moduleState;
  }

  public AspectRatio getAspectRatio() {
    return aspectRatio;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private String id;
    private Instant publishTime;
    private Instant lastUpdated;
    private String title;
    private ContentTypeEnum contentType;
    private ExternalId externalId;
    private List<String> owners;
    private String description;
    private Long durationInSec;
    private String videoId;
    private List<ContentLicense> contentLicenses;
    private Boolean embeddable;
    private Boolean incomplete;
    private Boolean isPrivate;
    private ModuleState moduleState;
    private AspectRatio aspectRatio;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder publishTime(Instant publishTime) {
      this.publishTime = publishTime;
      return this;
    }

    public Builder lastUpdated(Instant lastUpdated) {
      this.lastUpdated = lastUpdated;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder contentType(ContentTypeEnum contentType) {
      this.contentType = contentType;
      return this;
    }

    public Builder externalId(ExternalId externalId) {
      this.externalId = externalId;
      return this;
    }

    public Builder owners(List<String> owners) {
      this.owners = owners;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder durationInSec(Long durationInSec) {
      this.durationInSec = durationInSec;
      return this;
    }

    public Builder videoId(String videoId) {
      this.videoId = videoId;
      return this;
    }

    public Builder contentLicenses(List<ContentLicense> contentLicenses) {
      this.contentLicenses = contentLicenses;
      return this;
    }

    public Builder embeddable(Boolean embeddable) {
      this.embeddable = embeddable;
      return this;
    }

    public Builder incomplete(Boolean incomplete) {
      this.incomplete = incomplete;
      return this;
    }

    public Builder isPrivate(Boolean isPrivate) {
      this.isPrivate = isPrivate;
      return this;
    }

    public Builder moduleState(ModuleState moduleState) {
      this.moduleState = moduleState;
      return this;
    }

    public Builder aspectRatio(AspectRatio aspectRatio) {
      this.aspectRatio = aspectRatio;
      return this;
    }

    public Builder withVideoEntry(VideoEntry videoEntry) {
      id(videoEntry.getId());
      publishTime(new Instant(videoEntry.getPublished().getValue()));
      lastUpdated(new Instant(videoEntry.getUpdated().getValue()));
      title(videoEntry.getTitle().getPlainText());

      // TODO(arjuns): Currently hardcoding, but ideally should be parsed from the feed.
      contentType(ContentTypeEnum.APPLICATION_FLASH);

      YouTubeUrl ytUrl = new YouTubeUrl(LightUtils.getURL(videoEntry.getHtmlLink().getHref()));
      externalId(ytUrl.getExternalId());

      owners(GDataUtils.convertListPersonToAuthors(videoEntry.getAuthors()));

      YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
      description(mediaGroup.getDescription().getPlainTextContent());
      durationInSec(mediaGroup.getDuration());

      videoId(mediaGroup.getVideoId());
      contentLicenses(getContentLicenseFromVideoEntry(videoEntry));

      embeddable(videoEntry.isEmbeddable());
      incomplete(videoEntry.isYtIncomplete());
      isPrivate(mediaGroup.isPrivate());

      if (videoEntry.getPublicationState() != null) {
        moduleState(ModuleState.valueOf(videoEntry.getPublicationState().getState().name()));
      }

      if (mediaGroup.getAspectRatio() != null) {
        aspectRatio(AspectRatio.valueOf(mediaGroup.getAspectRatio().getValue().getXmlName()));
      }

      return this;
    }

    @SuppressWarnings("synthetic-access")
    public YouTubeVideoInfo build() {
      return new YouTubeVideoInfo(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private YouTubeVideoInfo(Builder builder) {
    super(builder);
    this.id = builder.id;

    this.publishTimeInMillis = builder.publishTime.getMillis();
    this.lastUpdateTimeInMillis = builder.lastUpdated.getMillis();
    this.title = builder.title;
    this.contentType = builder.contentType;
    this.externalId = builder.externalId;
    this.owners = builder.owners;
    this.description = builder.description;
    this.durationInSec = builder.durationInSec;
    this.videoId = builder.videoId;
    this.contentLicenses = builder.contentLicenses;
    this.embeddable = builder.embeddable;
    this.incomplete = builder.incomplete;
    this.isPrivate = builder.isPrivate;
    this.moduleState = builder.moduleState;
    this.aspectRatio = builder.aspectRatio;
    this.moduleType = ModuleType.YOU_TUBE_VIDEO;
  }

  // For JAXB
  private YouTubeVideoInfo() {
    super(null);
  }
}
