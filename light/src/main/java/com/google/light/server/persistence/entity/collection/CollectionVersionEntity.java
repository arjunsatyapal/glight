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
package com.google.light.server.persistence.entity.collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightUtils.getWrapper;

import com.google.appengine.api.datastore.Text;
import com.google.light.server.dto.collection.CollectionState;
import com.google.light.server.dto.collection.CollectionVersionDto;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import java.util.List;
import javax.persistence.Id;

/**
 * Persistence entity for {@link CollectionDto}.
 * Additional details like Title, Description etc are fetched from the root node.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class CollectionVersionEntity extends
    AbstractPersistenceEntity<CollectionVersionEntity, CollectionVersionDto> {
  @Id
  private Long version;
  private CollectionState collectionState;
  private List<ContentLicense> contentLicenses;

  @Parent
  private Key<CollectionEntity> collectionKey;
  private Text collectionTreeJson;

  @Override
  public CollectionVersionEntity validate() {
    checkPositiveLong(version, "version");
    checkNotNull(collectionKey, "collectionKey");
    checkNotNull(collectionTreeJson, "collectionTreeJson");
    checkNotNull(collectionState, "collectionState");
    checkNotEmptyCollection(contentLicenses, "contentLicenses");
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Key<CollectionVersionEntity> getKey() {
    return generateKey(collectionKey, new Version(version));
  }

  public static Key<CollectionVersionEntity> generateKey(Key<CollectionEntity> collectionKey,
      Version version) {
    checkNotNull(collectionKey, "collectionKey cannot be null");
    checkNotNull(version, "version");
    return new Key<CollectionVersionEntity>(collectionKey, CollectionVersionEntity.class,
        version.getValue());

  }

  public static Key<CollectionVersionEntity>
      generateKey(CollectionId collectionId, Version version) {
    return generateKey(CollectionEntity.generateKey(collectionId), version);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionVersionDto toDto() {
    CollectionVersionDto dto = new CollectionVersionDto.Builder()
        .collectionId(getCollectionId())
        .collectionTree(getCollectionTree())
        .collectionState(collectionState)
        // TODO(arjuns): See if this is used in ui.
        .title(getCollectionTree().getTitle())
        .version(getVersion())
        .build();

    return dto.validate();
  }

  public Version getVersion() {
    if (version == null) {
      return null;
    }

    return new Version(version);
  }

  public CollectionTreeNodeDto getCollectionTree() {
    checkNotNull(collectionTreeJson, "collectionTreeJson");
    return JsonUtils.getDto(collectionTreeJson.getValue(), CollectionTreeNodeDto.class);
  }

  public List<ContentLicense> getContentLicenses() {
    return contentLicenses;
  }

  public Key<CollectionEntity> getCollectionKey() {
    return collectionKey;
  }

  public CollectionId getCollectionId() {
    return getWrapper(collectionKey.getId(), CollectionId.class);
  }

  public CollectionState getCollectionState() {
    return collectionState;
  }
  
  public boolean isMutable() {
    return getCollectionState() == CollectionState.PARTIALLY_PUBLISHED ||
        getCollectionState() == CollectionState.RESERVED;
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private Version version;
    private CollectionTreeNodeDto collectionTree;
    private Key<CollectionEntity> collectionKey;
    private CollectionState collectionState;
    private List<ContentLicense> contentLicenses;

    public Builder collectionKey(Key<CollectionEntity> collectionKey) {
      this.collectionKey = collectionKey;
      return this;
    }

    public Builder version(Version version) {
      this.version = version;
      return this;
    }

    public Builder collectionTree(CollectionTreeNodeDto collectionTree) {
      this.collectionTree = collectionTree;
      return this;
    }

    public Builder collectionState(CollectionState collectionState) {
      this.collectionState = collectionState;
      return this;
    }

    public Builder contentLicenses(List<ContentLicense> contentLicenses) {
      this.contentLicenses = contentLicenses;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public CollectionVersionEntity build() {
      return new CollectionVersionEntity(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private CollectionVersionEntity(Builder builder) {
    super(builder, true);

    this.version = LightUtils.getWrapperValue(builder.version);

    checkNotNull(builder.collectionTree, "collectionTree");
    this.collectionTreeJson = new Text(builder.collectionTree.toJson());
    this.collectionKey = builder.collectionKey;
    this.collectionState = builder.collectionState;
    this.contentLicenses = builder.contentLicenses;
  }

  // For Objectify.
  private CollectionVersionEntity() {
    super(null, true);
  }
}
