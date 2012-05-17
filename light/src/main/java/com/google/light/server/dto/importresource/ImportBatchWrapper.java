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
package com.google.light.server.dto.importresource;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightConstants.IMPORT_BATCH_SIZE_MAX;
import static com.google.light.server.utils.LightPreconditions.checkIntegerIsInRage;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNull;
import static com.google.light.server.utils.LightUtils.isCollectionEmpty;
import static com.google.light.server.utils.LightUtils.replaceInstanceInList;

import com.google.common.collect.Lists;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.exception.unchecked.httpexception.BadRequestException;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.persistence.entity.jobs.JobStateCategory;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.StringUtils;
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
@JsonTypeName(value = "importBatch")
@XmlRootElement(name = "importBatch")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportBatchWrapper extends AbstractDto<ImportBatchWrapper> {
  // TODO(arjuns) : See how this looks
  @XmlElementWrapper(name = "list")
  @XmlElement(name = "item")
  @JsonProperty(value = "list")
  private List<ImportExternalIdDto> list;

  @XmlElement(name = "collectionTitle")
  @JsonProperty(value = "collectionTitle")
  private String collectionTitle;

  @XmlElement(name = "collectionId")
  @JsonProperty(value = "collectionId")
  private CollectionId collectionId;

  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private Version version;

  @XmlElement(name = "baseVersion")
  @JsonProperty(value = "baseVersion")
  private Version baseVersion;

  @XmlElement(name = "jobId")
  @JsonProperty(value = "jobId")
  private JobId jobId;

  @XmlElement(name = "jobState")
  @JsonProperty(value = "jobState")
  private JobState jobState;

  @XmlElement(name = "jobStateCategory")
  @JsonProperty(value = "jobStateCategory")
  private JobStateCategory jobStateCategory;

  /**
   * {@inheritDoc}
   */
  @Override
  public ImportBatchWrapper validate() {
    ImportBatchType type = getImportBatchType();
    switch (type) {
      case APPEND_COLLECTION_JOB:
        checkNotNull(getCollectionId(), "CollectionId cannot be null for " + type);
        //$FALL-THROUGH$

      case CREATE_COLLECTION_JOB:
        checkNotBlank(collectionTitle, "CollectionTitle cannot be blank for :" + type);
        //$FALL-THROUGH$

      case MODULE_JOB:
        checkIntegerIsInRage(getList().size(), 1, IMPORT_BATCH_SIZE_MAX,
            "Number of externalIds that can be downloaded in a batch should be between 1 & " +
                IMPORT_BATCH_SIZE_MAX + ".");
        break;

      default:
        throw new IllegalStateException("Unsupported Type : " + type);
    }

    return this;
  }

  public void requestValidation() {
    doValidation(true);

  }

  public void responseValidation() {
    doValidation(false);

  }

  private void doValidation(boolean isRequest) {
    doValidationForListMembers(isRequest);

    if (isRequest) {
      checkNull(jobId, "jobId should not be set by client.");
      checkNull(jobState, "jobState should not be set by client.");
      checkNull(jobStateCategory, "jobStateCategory should not be set by client.");
      checkNull(version, "version should not be set by client.");
    } else {
      checkNotNull(jobId, "jobId");
      checkNotNull(jobState, "jobState");
      checkNotNull(jobStateCategory, "jobStateCategory");
    }
  }

  private void doValidationForListMembers(boolean isRequest) {

    if (isRequest) {
      // Request size is limited. Response can be bigger.
      checkArgument(list.size() <= LightConstants.IMPORT_BATCH_SIZE_MAX);
    }
    
    for (ImportExternalIdDto curr : getList()) {
      if (isRequest) {
        curr.requestValidation();
      } else {
        curr.responseValidation();
      }
    }
  }

  public String getCollectionTitle() {
    return collectionTitle;
  }

  public void setCollectionTitle(String collectionTitle) {
    this.collectionTitle = collectionTitle;
  }

  public CollectionId getCollectionId() {
    return collectionId;
  }

  public void setCollectionId(CollectionId collectionId) {
    this.collectionId = checkNotNull(collectionId, "collectionId");
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = checkNotNull(version, "version");
  }

  public Version getBaseVersion() {
    return baseVersion;
  }

  public void setBaseVersion(Version baseVersion) {
    this.baseVersion = checkNotNull(baseVersion, "baseVersion");
  }

  public JobId getJobId() {
    return jobId;
  }
  
  public void setJobId(JobId jobId) {
    this.jobId = checkNotNull(jobId, "jobId");
  }

  public JobState getJobState() {
    return jobState;
  }
  
  public void setJobState(JobState jobState) {
    this.jobState = checkNotNull(jobState, "jobState");
    this.jobStateCategory = jobState.getCategory();
  }

  public JobStateCategory getJobStateCategory() {
    return jobStateCategory;
  }

  public ImportBatchType getImportBatchType() {
    if (collectionId != null) {
      // User wants to apppend current list to the existing collection.
      return ImportBatchType.APPEND_COLLECTION_JOB;
    } else if (StringUtils.isNotBlank(collectionTitle)) {
      // CollectionId is null, but title is there. This means create new collection.
      return ImportBatchType.CREATE_COLLECTION_JOB;
    }

    if (listContainsFolder()) {
      throw new BadRequestException(
          "Request contains a folder, and folders need to be part of a collection. So either "
              + "provide a CollectionId where it can be imported or Provide a collectionTitle "
              + "for new Collection.");
    }

    return ImportBatchType.MODULE_JOB;
  }

  private boolean listContainsFolder() {
    if (isCollectionEmpty(list)) {
      return false;
    }

    for (ImportExternalIdDto curr : list) {
      ExternalId externalId = curr.getExternalId();
      if (externalId.getModuleType().mapsToCollection()) {
        return true;
      }
    }

    return false;
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

  public ImportBatchWrapper() {
    super(null);
  }
}
