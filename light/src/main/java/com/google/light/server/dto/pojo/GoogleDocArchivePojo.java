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
package com.google.light.server.dto.pojo;

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.common.collect.Lists;
import com.google.gdata.data.Content;
import com.google.gdata.data.OutOfLineContent;
import com.google.gdata.data.Person;
import com.google.gdata.data.docs.ArchiveComplete;
import com.google.gdata.data.docs.ArchiveConversion;
import com.google.gdata.data.docs.ArchiveEntry;
import com.google.gdata.data.docs.ArchiveNotifyStatus;
import com.google.gdata.data.docs.ArchiveResourceId;
import com.google.gdata.data.docs.ArchiveStatus;
import com.google.gdata.data.docs.ArchiveTotal;
import com.google.gdata.data.docs.ArchiveTotalComplete;
import com.google.gdata.data.docs.ArchiveTotalFailure;
import com.google.gdata.data.extensions.QuotaBytesUsed;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.AbstractPojo;
import java.util.List;
import org.codehaus.jackson.annotate.JsonCreator;
import org.joda.time.Instant;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class GoogleDocArchivePojo extends AbstractPojo<GoogleDocArchivePojo>{
  private String id;
  private String archiveId;
  private Instant published;
  private Instant updated;
  private Instant edited;
  private String title;
  private ContentTypeEnum contentType;
  private String contentLocation;
  private List<String> authors;
  private String notifyEmail;
  
  // TODO(arjuns): Replicate this enum locally.
  private LightGDocArchiveStatus archiveStatus;

  private Long sizeInBytes;
  
  // TODO(arjuns): Replicate this enum locally.
  private LightGDocArchiveNotifyStatus notifyStatus;
  private Instant archiveCompleteTime;

  private int archiveTotal;
  private int archiveTotalCompleted;
  private int archiveTotalFailure;

  private List<String> untypedResourceIds;
  private List<LightConversionPojo> conversions;

  public String getId() {
    return id;
  }
  
  public String getArchiveId() {
    return archiveId;
  }

  public Instant getPublished() {
    return published;
  }

  public Instant getUpdated() {
    return updated;
  }

  public Instant getEdited() {
    return edited;
  }

  public String getTitle() {
    return title;
  }

  public ContentTypeEnum getContentType() {
    return contentType;
  }

  public String getContentLocation() {
    return contentLocation;
  }

  public List<String> getAuthors() {
    return authors;
  }

  public String getNotifyEmail() {
    return notifyEmail;
  }

  public LightGDocArchiveStatus getArchiveStatus() {
    return archiveStatus;
  }

  public Long getSizeInBytes() {
    return sizeInBytes;
  }

  public LightGDocArchiveNotifyStatus getNotifyStatus() {
    return notifyStatus;
  }

  public Instant getArchiveCompleteTime() {
    return archiveCompleteTime;
  }

  public int getArchiveTotal() {
    return archiveTotal;
  }

  public int getArchiveTotalCompleted() {
    return archiveTotalCompleted;
  }

  public int getArchiveTotalFailure() {
    return archiveTotalFailure;
  }

  public List<String> getUntypedResourceIds() {
    return untypedResourceIds;
  }

  public List<LightConversionPojo> getConversions() {
    return conversions;
  }

  @SuppressWarnings("rawtypes")
  public static class Builder extends AbstractDtoToPersistence.BaseBuilder {
    private String id;
    private Instant published;
    private Instant updated;
    private Instant edited;
    private String title;
    private ContentTypeEnum contentType;
    private String contentLocation;
    private List<String> authors;
    private String notifyEmail;
    private LightGDocArchiveStatus archiveStatus;
    private Long sizeInBytes;
    private LightGDocArchiveNotifyStatus notifyStatus;
    private Instant archiveCompleteTime;
    private int archiveTotal;
    private int archiveTotalComplete;
    private int archiveTotalFailure;

    private List<String> untypedResourceIds;
    private List<LightConversionPojo> conversions;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder published(com.google.gdata.data.DateTime dateTime) {
      this.published = new Instant(dateTime.getValue());
      return this;
    }

    public Builder updated(com.google.gdata.data.DateTime dateTime) {
      this.updated = new Instant(dateTime.getValue());
      return this;
    }

    public Builder edited(com.google.gdata.data.DateTime dateTime) {
      this.edited = new Instant(dateTime.getValue());
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

    public Builder contentLocation(String contentLocation) {
      this.contentLocation = contentLocation;
      return this;
    }

    public Builder authors(List<Person> listOfAuthors) {
      authors = Lists.newArrayList();

      for (Person currPerson : listOfAuthors) {
        authors.add(currPerson.getEmail());
      }
      return this;
    }

    public Builder notifyEmail(String notifyEmail) {
      this.notifyEmail = notifyEmail;
      return this;
    }

    public Builder archiveStatus(ArchiveStatus archiveStatus) {
      this.archiveStatus = LightGDocArchiveStatus.fromGdata(archiveStatus);
      return this;
    }

    public Builder sizeInBytes(QuotaBytesUsed quotaBytesUsed) {
      if (quotaBytesUsed != null) {
        this.sizeInBytes = quotaBytesUsed.getValue();
      }
      return this;
    }

    public Builder notifyStatus(ArchiveNotifyStatus archiveNotifyStatus) {
      if (archiveNotifyStatus != null) {
        this.notifyStatus = LightGDocArchiveNotifyStatus.fromGdata(archiveNotifyStatus);
      }
      return this;
    }

    public Builder archiveCompleteTime(ArchiveComplete archiveComplete) {
      // If Google is in process of archiving, it will not return archiveComplete.
      if (archiveComplete != null) {
        this.archiveCompleteTime = new Instant(archiveComplete.getValue().getValue());
      }

      return this;
    }

    public Builder archiveTotal(ArchiveTotal archiveTotal) {
      if (archiveTotal != null) {
        this.archiveTotal = archiveTotal.getValue();
      }
      return this;
    }

    public Builder archiveTotalComplete(ArchiveTotalComplete archiveTotalComplete) {
      if (archiveTotalComplete != null) {
        this.archiveTotalComplete = archiveTotalComplete.getValue();
      }
      return this;
    }

    public Builder archiveTotalFailure(ArchiveTotalFailure archiveTotalFailure) {
      if (archiveTotalFailure != null) {
        this.archiveTotalFailure = archiveTotalFailure.getValue();
      }
      return this;
    }

    public Builder untypedResourceIds(List<ArchiveResourceId> listOfArchiveResourceIds) {
      untypedResourceIds = Lists.newArrayList();

      for (ArchiveResourceId curr : listOfArchiveResourceIds) {
        untypedResourceIds.add(curr.getValue());
      }

      return this;
    }

    public Builder conversions(List<ArchiveConversion> listOfConversions) {
      conversions = Lists.newArrayList();

      for (ArchiveConversion curr : listOfConversions) {
        conversions.add(new LightConversionPojo(curr.getSource(), curr.getTarget()));
      }
      return this;
    }

    public Builder withArchiveEntry(ArchiveEntry archiveEntry) {
      id(archiveEntry.getId());
      published(archiveEntry.getPublished());
      updated(archiveEntry.getUpdated());
      edited(archiveEntry.getUpdated());
      title(archiveEntry.getTitle().getPlainText());
      withContent(archiveEntry.getContent());
      authors(archiveEntry.getAuthors());
      notifyEmail(archiveEntry.getArchiveNotify().getValue());
      archiveStatus(archiveEntry.getArchiveStatus());
      sizeInBytes(archiveEntry.getQuotaBytesUsed());
      notifyStatus(archiveEntry.getArchiveNotifyStatus());
      archiveCompleteTime(archiveEntry.getArchiveComplete());

      archiveTotal(archiveEntry.getArchiveTotal());
      archiveTotalComplete(archiveEntry.getArchiveTotalComplete());
      archiveTotalFailure(archiveEntry.getArchiveTotalFailure());

      untypedResourceIds(archiveEntry.getArchiveResourceIds());
      conversions(archiveEntry.getArchiveConversions());

      return this;
    }

    public Builder withContent(Content content) {
      if (content != null) {
        if (content instanceof OutOfLineContent) {
          OutOfLineContent mediaContent = (OutOfLineContent) content;
          contentType(contentType);
          contentLocation(mediaContent.getUri());
        } else {
          throw new IllegalArgumentException("Add this contentType here : "
              + content.getClass().getName());
        }
      }

      return this;
    }

    @SuppressWarnings("synthetic-access")
    public GoogleDocArchivePojo build() {
      return new GoogleDocArchivePojo(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private GoogleDocArchivePojo(Builder builder) {
    this.id = checkNotBlank(builder.id, "id");
    this.archiveId = id.replace("https://docs.google.com/feeds/archive/", ""); 
    this.published = builder.published;
    this.updated = builder.updated;
    this.edited = builder.edited;
    this.title = builder.title;
    this.contentType = builder.contentType;
    this.contentLocation = builder.contentLocation;
    this.authors = builder.authors;
    this.notifyEmail = builder.notifyEmail;
    this.archiveStatus = builder.archiveStatus;
    this.sizeInBytes = builder.sizeInBytes;
    this.notifyStatus = builder.notifyStatus;
    this.archiveCompleteTime = builder.archiveCompleteTime;

    this.archiveTotal = builder.archiveTotal;
    this.archiveTotalCompleted = builder.archiveTotalComplete;
    this.archiveTotalFailure = builder.archiveTotalFailure;

    this.untypedResourceIds = builder.untypedResourceIds;
    this.conversions = builder.conversions;
  }

  @JsonCreator
  // For JAXB.
  private GoogleDocArchivePojo() {
  }

  /** Copy of {@link com.google.gdata.data.doc.ArchiveStatus.Value} */
//  TODO(arjuns): Add test for this.
  // TODO(arjuns): Ensure that values match to that of library.
  public static enum LightGDocArchiveStatus {
    ABORTED,
    ARCHIVING,
    EXPIRED,
    FINISHED,
    FLATTENING,
    QUEUED;
    
    public static LightGDocArchiveStatus fromGdata(ArchiveStatus archiveStatus) {
      for (LightGDocArchiveStatus curr : LightGDocArchiveStatus.values()) {
        if (curr.name().equals(archiveStatus.getValue().name())) {
          return curr;
        }
      }
      
      throw new IllegalArgumentException(archiveStatus.getValue().name());

    }
  }
  
//  TODO(arjuns): Add test for this.
  /** Copy of {@link ArchiveNotifyStatus.Value} */
  public static enum  LightGDocArchiveNotifyStatus {
    FAILED,
    NONE,
    SENT;
    
    public static LightGDocArchiveNotifyStatus fromGdata(ArchiveNotifyStatus archiveNotifyStatus) {
      for (LightGDocArchiveNotifyStatus curr : LightGDocArchiveNotifyStatus.values()) {
        if (curr.name().equals(archiveNotifyStatus.getValue().name())) {
          return curr;
        }
      }
      
      throw new IllegalArgumentException(archiveNotifyStatus.getValue().name());
    }
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public GoogleDocArchivePojo validate() {
    // TODO(arjuns): Add validation logic here.
    return this;
  }
}
