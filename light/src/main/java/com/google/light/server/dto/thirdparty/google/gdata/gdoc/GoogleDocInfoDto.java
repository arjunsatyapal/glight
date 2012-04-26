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
package com.google.light.server.dto.thirdparty.google.gdata.gdoc;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import com.google.common.collect.Lists;
import com.google.gdata.data.Link;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclFeed;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;
import com.google.gdata.data.acl.AdditionalRole;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.light.server.dto.DtoInterface;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.exception.unchecked.JsonException;
import com.google.light.server.utils.JsonUtils;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.joda.time.DateTime;

/**
 * DTO for Importing Google Doc.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@JsonSerialize(include = Inclusion.NON_NULL)
@SuppressWarnings("serial")
public class GoogleDocInfoDto implements DtoInterface<GoogleDocInfoDto> {
  private static final Logger logger = Logger.getLogger(GoogleDocInfoDto.class.getName());

  private String id;
  private String resourceId;
  private String etag;

  private DateTime creationTime;
  private DateTime lastUpdated;
  private DateTime lastEditTime;

  private ModuleType type;
  private String title;

  private String documentLink;
  private String aclFeedLink;
  private String htmlExportUrl;
  private List<String> parentCollectionUrls;

  /*
   * TODO(arjuns): Figure out what should happen when a Person publishes a document on Light, and
   * then changes owner to someone else.
   */
  /**
   * For more details on roles see {@link https://developers.google.com/google-apps/documents-list/#
   * retrieving_the_acl_for_a_document_file_or_collection}.
   * See roles at : {@link AclRole}.
   */
  private List<String> owners;
  private List<String> writers;
  private List<String> commenters;
  private List<String> readers;

  // TODO(arjuns): See what happesn when access is via group.

  // Optional Fields which are present only with Files.
  // File name is set on Google Side only when its a non-converted file. e.g. PDF.
  private String fileName;
  // File name suggested by Google to download.
  private String suggestedFileName;
  // MD5 checksum for file.
  private String md5sum;
  // Size of file in bytes.
  private Long sizeInBytes;

  private DateTime lastCommentTime;
  private Configuration config;

  /**
   * {@inheritDoc}
   */
  @Override
  public String toJson() {
    try {
      return JsonUtils.toJson(this);
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      // TODO(arjuns): Update other toJson to throw this exception.
      throw new JsonException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toXml() {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GoogleDocInfoDto validate() {
    checkNotNull(config, "config");
    
    if (config != Configuration.DTO_FOR_IMPORT) {
      checkNotBlank(id, "id");
    }
    
    // Required for all configs.
    checkNotBlank(resourceId, "resourceId");
    
    if (config != Configuration.DTO_FOR_IMPORT) {
      checkNotBlank(etag, "etag");
    }

    if (config != Configuration.DTO_FOR_IMPORT) {
      checkNotNull(creationTime, "creationTime");
      checkNotNull(lastUpdated, "lastUpdated");
      checkNotNull(lastEditTime, "lastEditTime");
    }

    // Required for all configs.
    checkNotNull(type, "ModuleType");
    checkNotBlank(title, "title");
    checkNotBlank(documentLink, "documentLink");
    
    if (config != Configuration.DTO_FOR_IMPORT) {

      checkNotBlank(aclFeedLink, "aclFeedLink");
      checkNotBlank(htmlExportUrl, "htmlExportUrl");
      // checkNonEmptyList(parentCollectionUrls);

      checkNotNull(owners);
      // We dont do any validation for writers, commenters and readers.
//      checkNotNull(writers);
//      checkNotNull(commenters);
//      checkNotNull(readers);

      if (fileName != null) {
        // Current Entry is for a file which is not converted to Google Docs.
        checkNotBlank(fileName, "fileName");
        checkNotBlank(suggestedFileName, "suggestedFileName");
        checkNotBlank(md5sum, "md5sum");
        checkPositiveLong(sizeInBytes, "sizeInBytes : " + sizeInBytes);
      }
    }
    // For writers and readers, we dont do any validation.

    // lastComment Time is an optional field. So not checking it.

    return this;
  }

  public String getId() {
    return id;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getEtag() {
    return etag;
  }

  public DateTime getCreationTime() {
    return creationTime;
  }

  public DateTime getLastUpdated() {
    return lastUpdated;
  }

  public DateTime getLastEditTime() {
    return lastEditTime;
  }

  public ModuleType getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getDocumentLink() {
    return documentLink;
  }

  public String getAclFeedLink() {
    return aclFeedLink;
  }

  public String getHtmlExportUrl() {
    return htmlExportUrl;
  }

  public List<String> getParentCollectionUrls() {
    return parentCollectionUrls;
  }

  public List<String> getOwners() {
    return owners;
  }

  public List<String> getWriters() {
    return writers;
  }

  public List<String> getCommenters() {
    return commenters;
  }

  public List<String> getReaders() {
    return readers;
  }

  public String getFileName() {
    return fileName;
  }

  public String getSuggestedFileName() {
    return suggestedFileName;
  }

  public String getMd5sum() {
    return md5sum;
  }

  public Long getSizeInBytes() {
    return sizeInBytes;
  }

  public DateTime getLastCommentTime() {
    return lastCommentTime;
  }
  
  
  
  public static Logger getLogger() {
    return logger;
  }

  public Configuration getConfig() {
    return config;
  }



  public static class Builder {
    private String id;
    private String resourceId;
    private String etag;
    private DateTime creationTime;
    private DateTime lastUpdated;
    private DateTime lastEditTime;
    private ModuleType type;
    private String title;
    private String documentLink;
    private String aclFeedLink;
    private String htmlExportUrl;
    private List<String> parentCollectionUrls = Lists.newArrayList();
    private List<String> owners = Lists.newArrayList();
    private List<String> writers = Lists.newArrayList();
    private List<String> commenters = Lists.newArrayList();
    private List<String> readers = Lists.newArrayList();
    private String fileName;
    private String suggestedFileName;
    private String md5sum;
    private Long sizeInBytes;
    private DateTime lastCommentTime;

    // Builder configuration.
    private Configuration config;

    public Builder(Configuration config) {
      this.config = checkNotNull(config, "configuration");
    }

    public Builder id(String id) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.id = id;
      }
      return this;
    }

    public Builder resourceId(String resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public Builder etag(String etag) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.etag = etag;
      }
      return this;
    }

    public Builder creationTime(DateTime creationTime) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.creationTime = creationTime;
      }
      return this;
    }

    public Builder lastUpdated(DateTime lastUpdated) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.lastUpdated = lastUpdated;
      }
      return this;
    }

    public Builder lastEditTime(DateTime lastEditTime) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.lastEditTime = lastEditTime;
      }
      return this;
    }

    public Builder type(ModuleType type) {
      this.type = type;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder documentLink(String documentLink) {
      this.documentLink = documentLink;
      return this;
    }

    public Builder aclFeedLink(String aclFeedLink) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.aclFeedLink = aclFeedLink;
      }
      return this;
    }

    public Builder htmlExportUrl(String htmlExportUrl) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.htmlExportUrl = htmlExportUrl;
      }
      return this;
    }

    public Builder parentCollectionUrls(List<Link> parentUrls) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        for (Link currLink : parentUrls) {
          parentCollectionUrls.add(currLink.getHref());
        }
      }
      return this;
    }

    public Builder owners(List<String> owners) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.owners = owners;
      }
      return this;
    }

    public Builder writers(List<String> writers) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.writers = writers;
      }
      return this;
    }

    public Builder readers(List<String> readers) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.readers = readers;
      }
      return this;
    }

    public Builder fileName(String fileName) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.fileName = fileName;
      }
      return this;
    }

    public Builder suggestedFileName(String suggestedFileName) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.suggestedFileName = suggestedFileName;
      }
      return this;
    }

    public Builder md5sum(String md5sum) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.md5sum = md5sum;
      }
      return this;
    }

    public Builder sizeInBytes(Long sizeInBytes) {
      if (sizeInBytes != null && sizeInBytes == 0) {
        return this;
      }

      if (config != Configuration.DTO_FOR_IMPORT) {
        this.sizeInBytes = sizeInBytes;
      }
      return this;
    }

    public Builder lastCommentTime(DateTime lastCommentTime) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.lastCommentTime = lastCommentTime;
      }
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public GoogleDocInfoDto build() {
      return new GoogleDocInfoDto(this).validate();
    }

    @SuppressWarnings("synthetic-access")
    public Builder withAclFeed(AclFeed aclFeed) {
      checkNotNull(aclFeed, "aclFeed");

      for (AclEntry aclEntry : aclFeed.getEntries()) {
        boolean isCommenter = false;
        List<AdditionalRole> additionalRoles = aclEntry.getAdditionalRoles();
        if (additionalRoles.size() > 1) {
          StringBuilder errorMessageBuilder = new StringBuilder()
              .append("For ModuleType[").append(type)
              .append("], resourceId[").append(resourceId)
              .append("], found more then one additional roles : ");
          for (AdditionalRole currRole : additionalRoles) {
            errorMessageBuilder.append(currRole.getValue()).append(", ");
          }

          logger.severe(errorMessageBuilder.toString());
          throw new RuntimeException(errorMessageBuilder.toString());
        } else if (additionalRoles.size() == 1) {
          AdditionalRole role = additionalRoles.get(0);

          if (role.getValue().equals(AdditionalRole.COMMENTER.getValue())) {
            isCommenter = true;
          } else {
            String errorMessage =
                "Unknown role : " + role.getValue() + " for moduleType[" + type
                    + "], resourceId[" + resourceId + "].";
            logger.severe(errorMessage);
            throw new RuntimeException(errorMessage);
          }
        }

        AclRole role = aclEntry.getRole();
        AclScope scope = aclEntry.getScope();
        String email = scope.getValue();
        if (role.getValue().equals(AclRole.OWNER.getValue())) {
          owners.add(email);
        } else if (role.getValue().equals(AclRole.WRITER.getValue())) {
          writers.add(email);
        } else if (role.getValue().equals(AclRole.COMMENTER.getValue())) {
          commenters.add(email);
        } else if (role.getValue().equals(AclRole.READER.getValue())) {
          if (isCommenter) {
            /*
             * For Google Docs, for Commenters, instead of sending AclRole.Commenter,
             * we get AdditionalRole=Commenter and AclRole=Reader.
             */
            commenters.add(email);
          } else {
            readers.add(email);
          }
        } else {
          String errorMessage =
              "Unknown role : " + role.getValue() + " for moduleType[" + type
                  + "], resourceId[" + resourceId + "].";
          logger.severe(errorMessage);
          throw new RuntimeException(errorMessage);
        }
      }

      return this;
    }

    public Builder withDocumentListEntry(DocumentListEntry docListEntry) {
      checkNotNull(docListEntry, "docListEntry");
      id(docListEntry.getId());
      resourceId(docListEntry.getResourceId());
      etag(docListEntry.getEtag());

      creationTime(new DateTime(docListEntry.getPublished().getValue()));
      lastUpdated(new DateTime(docListEntry.getUpdated().getValue()));
      lastEditTime(new DateTime(docListEntry.getEdited().getValue()));

      type(ModuleType.getByProviderServiceAndCategory(GOOGLE_DOC, docListEntry.getType()));
      title(docListEntry.getTitle().getPlainText());

      documentLink(docListEntry.getDocumentLink().getHref());
      aclFeedLink(docListEntry.getAclFeedLink().getHref());
      // TODO(arjuns) : Verify this.
      int contentTypeInt = docListEntry.getContent().getType();
      switch (contentTypeInt) {
      /**
       * See {@link IContent.Type}
       */
        case 7:
          MediaContent mediaContent = (MediaContent) docListEntry.getContent();
          htmlExportUrl(mediaContent.getUri());
          break;

        default:
          throw new IllegalArgumentException("ContentType : " + contentTypeInt
              + " is currently not supported.");
      }

      List<Link> parentUrls = docListEntry.getParentLinks();

      // TODO(arjuns): Eventually we will need to handle movement of documents in/out of collection.
      parentCollectionUrls(parentUrls);

      // TODO(arjuns): Add for readers.

      fileName(docListEntry.getFilename());
      suggestedFileName(docListEntry.getSuggestedFilename());
      md5sum(docListEntry.getMd5Checksum());
      sizeInBytes(docListEntry.getQuotaBytesUsed());

      // Most of the time this is not set.
      com.google.gdata.data.DateTime lastCommentGdataTime = docListEntry.getLastCommented();
      if (lastCommentGdataTime != null) {
        lastCommentTime(new DateTime(docListEntry.getLastCommented().getValue()));
      }

      if (config != Configuration.DTO_FOR_IMPORT) {
        withAclFeed(docListEntry.getAclFeed());
      }

      return this;
    }
  }
  
  // For JAXB
  private GoogleDocInfoDto() {
  }

  @SuppressWarnings("synthetic-access")
  private GoogleDocInfoDto(Builder builder) {
    this.id = builder.id;
    this.resourceId = builder.resourceId;
    this.etag = builder.etag;
    this.creationTime = builder.creationTime;
    this.lastUpdated = builder.lastUpdated;
    this.lastEditTime = builder.lastEditTime;
    this.type = builder.type;
    this.title = builder.title;
    this.documentLink = builder.documentLink;
    this.aclFeedLink = builder.aclFeedLink;
    this.htmlExportUrl = builder.htmlExportUrl;
    this.parentCollectionUrls = getNonEmptyList(builder.parentCollectionUrls);

    this.owners = getNonEmptyList(builder.owners);
    this.writers = getNonEmptyList(builder.writers);
    this.commenters = getNonEmptyList(builder.commenters);
    this.readers = getNonEmptyList(builder.readers);

    this.fileName = builder.fileName;
    this.suggestedFileName = builder.suggestedFileName;
    this.md5sum = builder.md5sum;
    this.sizeInBytes = builder.sizeInBytes;
    this.lastCommentTime = builder.lastCommentTime;
    this.config = checkNotNull(builder.config, "config");
  }

  private <D> List<D> getNonEmptyList(List<D> list) {
    if (list != null && list.size() > 0) {
      return list;
    }

    return null;
  }

  public static enum Configuration {
    DTO_FOR_IMPORT,
    DTO_FOR_PERSISTENCE,
    DTO_FOR_DEBUGGING;
  }
}