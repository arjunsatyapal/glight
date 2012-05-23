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
package com.google.light.server.dto.thirdparty.google.gdoc;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.utils.LightPreconditions.checkExternalIdIsGDocResource;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightUtils.getWrapper;

import com.google.light.server.utils.XmlUtils;

import com.google.common.collect.Lists;
import com.google.gdata.data.Link;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclFeed;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;
import com.google.gdata.data.acl.AdditionalRole;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.joda.time.DateTime;
import org.joda.time.Instant;

/**
 * DTO for Importing Google Doc.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "googleDocInfo")
@XmlRootElement(name = "googleDocInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class GoogleDocInfoDto extends AbstractDto<GoogleDocInfoDto> implements Comparable<GoogleDocInfoDto>{
  private static final Logger logger = Logger.getLogger(GoogleDocInfoDto.class.getName());
  @XmlElement(name = "id")
  @JsonProperty(value = "id")
  private String id;
  
//  @XmlElement(name = "resourceId")
//  @JsonProperty(value = "resourceId")
//  private String resourceId;
  
  @XmlElement(name = "lastEditTimeInMillis")
  @JsonProperty(value = "lastEditTimeInMillis")
  private Long lastEditTimeInMillis;
  
  @XmlElement(name = "moduleType")
  @JsonProperty(value = "moduleType")
  private ModuleType moduleType;
  
  @XmlElement(name = "title")
  @JsonProperty(value = "title")
  private String title;
  
  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private ExternalId externalId;
  
  @XmlElement(name = "aclFeedLink")
  @JsonProperty(value = "aclFeedLink")
  private String aclFeedLink;
  
  @XmlElement(name = "htmlExportUrl")
  @JsonProperty(value = "htmlExportUrl")
  private String htmlExportUrl;
  
  @XmlElementWrapper(name = "parentFolderUrls")
  @XmlElement(name = "url")
  @JsonProperty(value = "parentFolderUrls")
  private List<String> parentFolderUrls;

  /*
   * TODO(arjuns): Figure out what should happen when a Person publishes a document on Light, and
   * then changes owner to someone else.
   */
  /**
   * For more details on roles see {@link https://developers.google.com/google-apps/documents-list/#
   * retrieving_the_acl_for_a_document_file_or_collection}.
   * See roles at : {@link AclRole}.
   */
  @XmlElementWrapper(name = "owners")
  @XmlElement(name = "email")
  @JsonProperty(value = "owners")
  private List<String> owners;
  
  @XmlElementWrapper(name = "writers")
  @XmlElement(name = "email")
  @JsonProperty(value = "writers")
  private List<String> writers;

  @XmlElementWrapper(name = "commenters")
  @XmlElement(name = "email")
  @JsonProperty(value = "commenters")
  private List<String> commenters;
  
  @XmlElementWrapper(name = "readers")
  @XmlElement(name = "email")
  @JsonProperty(value = "readers")
  private List<String> readers;

  // TODO(arjuns): See what happesn when access is via group.

  // Optional Fields which are present only with Files.
  // File name is set on Google Side only when its a non-converted file. e.g. PDF.
  @XmlElement(name = "fileName")
  @JsonProperty(value = "fileName")
  private String fileName;
  
  // File name suggested by Google to download.
  @XmlElement(name = "suggestedFileName")
  @JsonProperty(value = "suggestedFileName")
  private String suggestedFileName;
  
  @XmlElement(name = "etag")
  @JsonProperty(value = "etag")
  private String etag;
  
  // MD5 checksum for file.
  @XmlElement(name = "md5sum")
  @JsonProperty(value = "md5sum")
  private String md5sum;
  
  // Size of file in bytes.
  @XmlElement(name = "sizeInBytes")
  @JsonProperty(value = "sizeInBytes")
  private Long sizeInBytes;

  @XmlElement(name = "lastCommentTime")
  @JsonProperty(value = "lastCommentTime")
  private Instant lastCommentTime;
  
  @XmlElement(name = "config")
  @JsonProperty(value = "config")
  private Configuration config;

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
    checkNotNull(externalId, "externalId");
//    checkNotBlank(resourceId, "resourceId");

    if (config != Configuration.DTO_FOR_IMPORT) {
      checkNotBlank(etag, "etag");
    }

    if (config != Configuration.DTO_FOR_IMPORT) {
      checkNotNull(lastEditTimeInMillis, "lastEditTime");
    }

    // Required for all configs.
    checkNotNull(moduleType, "ModuleType");
    checkNotBlank(title, "title");
    checkNotNull(externalId, "externalId");
    checkExternalIdIsGDocResource(externalId);

    if (config != Configuration.DTO_FOR_IMPORT) {

      checkNotBlank(aclFeedLink, "aclFeedLink");
      checkNotBlank(htmlExportUrl, "htmlExportUrl");
      // checkNonEmptyList(parentCollectionUrls);

//      checkNotNull(owners, "owners");
      // We dont do any validation for writers, commenters and readers.
      // checkNotNull(writers);
      // checkNotNull(commenters);
      // checkNotNull(readers);

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

//  public String getResourceId() {
//    return resourceId;
//  }

//  public GoogleDocResourceId getGoogleDocsResourceId() {
//    checkNotBlank(resourceId, "resourceId");
//    return new GoogleDocResourceId(resourceId);
//  }

  public String getEtag() {
    return etag;
  }

  public Instant getLastEditTime() {
    return new Instant(getLastEditTimeInMillis());
  }
  
  public Long getLastEditTimeInMillis() {
    return lastEditTimeInMillis;
  }

  public ModuleType getModuleType() {
    return moduleType;
  }

  public String getTitle() {
    return title;
  }

  public ExternalId getExternalId() {
    return externalId;
  }
  
  public GoogleDocResourceId getGoogleDocResourceId() {
    return new GoogleDocResourceId(getExternalId());
  }

  public String getAclFeedLink() {
    return aclFeedLink;
  }

  public String getHtmlExportUrl() {
    return htmlExportUrl;
  }

  public List<String> getParentFolderUrls() {
    return parentFolderUrls;
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

  public Instant getLastCommentTime() {
    return lastCommentTime;
  }

  public static Logger getLogger() {
    return logger;
  }

  public Configuration getConfig() {
    return config;
  }
  
  /** 
   * {@inheritDoc}
   */
  @Override
  public int compareTo(GoogleDocInfoDto other) {
    return this.getTitle().compareTo(other.getTitle());
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private String id;
    private String resourceId;
    private String etag;
    private Long lastEditTimeInMillis;
    private ModuleType moduleType;
    private String title;
    private ExternalId externalId;
    private String aclFeedLink;
    private String htmlExportUrl;
    private List<String> parentFolderUrls = Lists.newArrayList();
    private List<String> owners = Lists.newArrayList();
    private List<String> writers = Lists.newArrayList();
    private List<String> commenters = Lists.newArrayList();
    private List<String> readers = Lists.newArrayList();
    private String fileName;
    private String suggestedFileName;
    private String md5sum;
    private Long sizeInBytes;
    private Instant lastCommentTime;

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

    public Builder lastEditTime(Instant lastEditTime) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        this.lastEditTimeInMillis = lastEditTime.getMillis();
      }
      return this;
    }

    public Builder moduleType(ModuleType moduleType) {
      this.moduleType = moduleType;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder externalId(ExternalId externalId) {
      this.externalId = externalId;
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

    public Builder parentFolderUrls(List<Link> parentFolderUrls) {
      if (config != Configuration.DTO_FOR_IMPORT) {
        for (Link currLink : parentFolderUrls) {
          this.parentFolderUrls.add(currLink.getHref());
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

    public Builder lastCommentTime(Instant lastCommentTime) {
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
              .append("For ModuleType[").append(moduleType)
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
                "Unknown role : " + role.getValue() + " for moduleType[" + moduleType
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
              "Unknown role : " + role.getValue() + " for moduleType[" + moduleType
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

      creationTime(new DateTime(docListEntry.getPublished().getValue()).toInstant());
      lastUpdateTime(new DateTime(docListEntry.getUpdated().getValue()).toInstant());
      lastEditTime(new DateTime(docListEntry.getEdited().getValue()).toInstant());

      moduleType(ModuleType.getByProviderServiceAndCategory(GOOGLE_DOC, docListEntry.getType()));
      title(docListEntry.getTitle().getPlainText());

      externalId(getWrapper(docListEntry.getDocumentLink().getHref(), ExternalId.class));
      aclFeedLink(docListEntry.getAclFeedLink().getHref());
      // TODO(arjuns) : Verify this.
      int contentTypeInt = docListEntry.getContent().getType();
      htmlExportUrl(docListEntry.getHtmlLink().getHref());
//      switch (contentTypeInt) {
//      /**
//       * See {@link IContent.Type}
//       */
//        case 7:
//          MediaContent mediaContent = (MediaContent) docListEntry.getContent();
//          
//          break;
//
//        default:
//          System.out.println("Here i am : " + XmlUtils.getXmlEntry(docListEntry));
//          throw new IllegalArgumentException("ContentType : " + contentTypeInt
//              + " is currently not supported. Failed for " + XmlUtils.getXmlEntry(docListEntry));
//      }

      List<Link> parentFolderUrls = docListEntry.getParentLinks();

      // TODO(arjuns): Eventually we will need to handle movement of documents in/out of collection.
      parentFolderUrls(parentFolderUrls);

      // TODO(arjuns): Add for readers.

      fileName(docListEntry.getFilename());
      suggestedFileName(docListEntry.getSuggestedFilename());
      md5sum(docListEntry.getMd5Checksum());
      sizeInBytes(docListEntry.getQuotaBytesUsed());

      // Most of the time this is not set.
      com.google.gdata.data.DateTime lastCommentGdataTime = docListEntry.getLastCommented();
      if (lastCommentGdataTime != null) {
        lastCommentTime(new DateTime(docListEntry.getLastCommented().getValue()).toInstant());
      }

      // TODO(arjuns): Fix this.
//      if (config != Configuration.DTO_FOR_IMPORT) {
//        withAclFeed(docListEntry.getAclFeed());
//      }

      return this;
    }
  }

  @SuppressWarnings("synthetic-access")
  private GoogleDocInfoDto(Builder builder) {
    super(builder);
    this.id = builder.id;
//    this.resourceId = builder.resourceId;
    this.etag = builder.etag;
    this.lastEditTimeInMillis = builder.lastEditTimeInMillis;
    this.moduleType = builder.moduleType;
    this.title = builder.title;
    this.externalId = builder.externalId;
    this.aclFeedLink = builder.aclFeedLink;
    this.htmlExportUrl = builder.htmlExportUrl;
    this.parentFolderUrls = getNonEmptyList(builder.parentFolderUrls);

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

  // For Jaxb.
  @JsonCreator
  private GoogleDocInfoDto() {
    super(null);
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
