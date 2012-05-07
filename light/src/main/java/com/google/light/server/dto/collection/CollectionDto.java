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
package com.google.light.server.dto.collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.dto.pojo.tree.CollectionTreeNodeDto;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.utils.LightPreconditions;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "collection")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionDto extends
    AbstractDtoToPersistence<CollectionDto, CollectionEntity, String> {

  @XmlElement
  private String title;
  
  @XmlElement(name = "state")
  private CollectionState state;
  
  @XmlElement
  private List<PersonId> owners;
  
  @XmlElement
  private Version version;
  
  @XmlElement
  private CollectionTreeNodeDto root;

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionEntity toPersistenceEntity(String id) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionDto validate() {
    checkNotBlank(title, "title");
    checkNotNull(state, "collectionState");
    
    LightPreconditions.checkNonEmptyList(owners, "owners");

    checkNotNull(version, "latestVersion");
    checkNotNull(root, "root");
    return this;
  }

  public String getTitle() {
    return title;
  }

  public CollectionState getState() {
    return state;
  }

  public List<PersonId> getOwners() {
    return owners;
  }

  public Version getVersion() {
    return version;
  }

  public CollectionTreeNodeDto getRoot() {
    return root;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private String title;
    private CollectionState state;
    private List<PersonId> owners;
    private Version version;
    private CollectionTreeNodeDto root;

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder collectionState(CollectionState state) {
      this.state = state;
      return this;
    }

    public Builder ownerPersonId(List<PersonId> owners) {
      this.owners = owners;
      return this;
    }

    public Builder version(Version version) {
      this.version = version;
      return this;
    }

    public Builder root(CollectionTreeNodeDto root) {
      this.root = root;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public CollectionDto build() {
      return new CollectionDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private CollectionDto(Builder builder) {
    super(builder);
    this.title = builder.title;
    this.state = builder.state;
    this.owners = builder.owners;
    this.version = builder.version;
    this.root = builder.root;
  }

  // For Jaxb
  private CollectionDto() {
    super(null);
  }
}
