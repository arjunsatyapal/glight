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
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightUtils.addToCollectionIfNotPresent;
import static com.google.light.server.utils.LightUtils.isCollectionEmpty;

import javax.xml.bind.annotation.XmlAnyElement;

import javax.xml.bind.annotation.XmlElementWrapper;

import javax.xml.bind.annotation.XmlElement;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonTypeName;

import com.google.common.collect.Lists;
import com.google.gdata.data.youtube.PlaylistEntry;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.gdata.GDataUtils;
import com.google.light.server.servlets.thirdparty.google.youtube.YouTubeUtils;
import com.google.light.server.urls.YouTubeUrl;
import com.google.light.server.utils.LightUtils;
import java.util.List;
import org.joda.time.Instant;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "youTubePlaylistInfo")
@XmlRootElement(name = "youTubePlaylistInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class YouTubePlaylistInfo extends AbstractDto<YouTubePlaylistInfo> {
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
  
  @XmlElement(name = "description")
  @JsonProperty(value = "description")
  private String description;
  
  @XmlElement(name = "htmlLink")
  @JsonProperty(value = "htmlLink")
  private String htmlLink;
  
  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private ExternalId externalId;
  
  @XmlElementWrapper(name = "authors")
  @XmlAnyElement
  @JsonProperty(value = "authors")
  private List<String> authors;
  
  @XmlElement(name = "duration")
  @JsonProperty(value = "duration")
  private Long duration;
  
  @XmlElement(name = "playlistId")
  @JsonProperty(value = "playlistId")
  private String playlistId;

  @XmlElementWrapper(name = "listOfVideos")
  @XmlAnyElement
  @JsonProperty(value = "listOfVideos")
  private List<YouTubeVideoInfo> listOfVideos;

  @XmlElementWrapper(name = "contentLicenses")
  @XmlAnyElement
  @JsonProperty(value = "contentLicenses")
  private List<ContentLicense> contentLicenses;

  /**
   * {@inheritDoc}
   */
  @Override
  public YouTubePlaylistInfo validate() {
    checkNotBlank(id, "id");
    checkNotNull(publishTimeInMillis, "publisTimeInMillis");
    checkNotNull(lastUpdateTimeInMillis, "lastUpdateTimeInMillis");
    checkNotBlank(title, "title");
    checkNotBlank(htmlLink, "htmlLink");
    checkNotNull(externalId, "externalId");
    checkNotEmptyCollection(authors, "authors");
    checkNotBlank(playlistId, "playlistId");
    return this;
  }

  public String getId() {
    return id;
  }

  public Instant getPublishTime() {
    checkNotNull(publishTimeInMillis, "publishTimeInMillis");
    return new Instant(publishTimeInMillis);
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getHtmlLink() {
    return htmlLink;
  }

  public ExternalId getExternalId() {
    return externalId;
  }

  public List<String> getAuthors() {
    return authors;
  }

  public Long getDuration() {
    return duration;
  }

  public void addDuration(Long duration) {
    checkNotNull(this.duration, "builder should have initialized the duration to 0.");
    checkPositiveLong(duration, "new duration should be positive.");
    this.duration += duration;
  }

  public String getPlaylistId() {
    return playlistId;
  }

  public List<YouTubeVideoInfo> getListOfVideos() {
    if (isCollectionEmpty(listOfVideos)) {
      listOfVideos = Lists.newArrayList();
    }

    return listOfVideos;
  }

  public void addYouTubeVideo(YouTubeVideoInfo youTubeVideoInfo) {
    boolean wasAdded = addToCollectionIfNotPresent(getListOfVideos(), youTubeVideoInfo);
    if (wasAdded) {
      addDuration(youTubeVideoInfo.getDurationInSec());
    }
  }

  public List<ContentLicense> getContentLicenses() {
    return contentLicenses;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private String id;
    private Instant publishTime;
    private String title;
    private String description;
    private String htmlLink;
    private ExternalId externalId;
    private List<String> authors;
    // Default value for Duration for an empty playlist.
    private Long duration = 0L;
    private String playlistId;
    private List<YouTubeVideoInfo> listOfVideos;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder publishTime(Instant publishTime) {
      this.publishTime = publishTime;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder htmlLink(String htmlLink) {
      this.htmlLink = htmlLink;
      return this;
    }

    public Builder externalId(ExternalId externalId) {
      this.externalId = externalId;
      return this;
    }

    public Builder authors(List<String> authors) {
      this.authors = authors;
      return this;
    }

    public Builder duration(Long duration) {
      this.duration = duration;
      return this;
    }

    public Builder playlistId(String playlistId) {
      this.playlistId = playlistId;
      return this;
    }

    public Builder listOfVideos(List<YouTubeVideoInfo> listOfVideos) {
      this.listOfVideos = listOfVideos;
      return this;
    }

    public Builder withPlaylistEntry(PlaylistEntry playlistEntry) {
      id(playlistEntry.getId());
      publishTime(new Instant(playlistEntry.getPublished().getValue()));
      lastUpdateTime(new Instant(playlistEntry.getUpdated().getValue()));
      title(playlistEntry.getTitle().getPlainText());
      description(playlistEntry.getSummary().getPlainText());
      htmlLink(playlistEntry.getHtmlLink().getHref());

      YouTubeUrl ytUrl = new YouTubeUrl(LightUtils.getURL(this.htmlLink));
      externalId(ytUrl.getExternalId());

      authors(GDataUtils.convertListPersonToAuthors(playlistEntry.getAuthors()));

      /*
       * GData API always returns 0 for Playlist Duration. So duration will be calculated using
       * videos present in the playlist.
       */

      playlistId(YouTubeUtils.getPlaylistIdFromPlaylistEntry(playlistEntry));
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public YouTubePlaylistInfo build() {
      return new YouTubePlaylistInfo(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private YouTubePlaylistInfo(Builder builder) {
    super(builder);
    this.id = builder.id;
    this.publishTimeInMillis = builder.publishTime.getMillis();
    this.lastUpdateTimeInMillis = builder.getLastUpdateTime().getMillis();
    this.title = builder.title;
    this.description = builder.description;
    this.htmlLink = builder.htmlLink;
    this.externalId = builder.externalId;
    this.authors = builder.authors;
    this.duration = builder.duration;
    this.playlistId = builder.playlistId;
    this.listOfVideos = builder.listOfVideos;
    this.contentLicenses = Lists.newArrayList(ContentLicense.YOUTUBE);
  }

  // For JAXB
  private YouTubePlaylistInfo() {
    super(null);
  }
}
