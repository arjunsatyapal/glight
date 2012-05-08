///*
// * Copyright 2012 Google Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//package com.google.light.server.persistence.entity.queue.importflow.gdoc;
//
//import static com.google.common.base.Preconditions.checkNotNull;
//import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
//import static com.google.light.server.utils.LightPreconditions.checkPersonId;
//
//import com.google.light.server.dto.pojo.longwrapper.PersonId;
//
//import com.google.light.server.dto.module.ModuleType;
//import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
//import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
//import com.googlecode.objectify.Key;
//import javax.persistence.Id;
//
///**
// * Entity class used for Storing details about Import for Google Doc.
// * 
// * TODO(arjuns): Add test for this class.
// * 
// * @author Arjun Satyapal
// */
//@SuppressWarnings("serial")
//public class GoogleDocImportJobEntity extends AbstractPersistenceEntity<GoogleDocImportJobEntity, Object> {
//  @Id
//  private Long id;
//  private ModuleType moduleType;
//  private String resourceId;
//  private Long personId;
//  private String additionalJsonInfo;
//
//  protected static String computeId(ModuleType moduleType, String resourceId) {
//    checkNotNull(moduleType, "moduleType");
//    checkNotBlank(resourceId, "resourceId");
//
//    return moduleType.name() + ":" + resourceId;
//
//  }
//
//  /**
//   * Method to generate Objectify key for {@link ImportEntity}.
//   */
//  public static Key<GoogleDocImportJobEntity> generateKey(ModuleType moduleType, String resourceId) {
//    return new Key<GoogleDocImportJobEntity>(GoogleDocImportJobEntity.class, computeId(moduleType, resourceId));
//  }
//
//  /**
//   * Method to generate Objectify Key for {@link ImportEntity}.
//   * 
//   * @param importId
//   * @return
//   */
//  public static Key<GoogleDocImportJobEntity> generateKey(String importId) {
//    return new Key<GoogleDocImportJobEntity>(GoogleDocImportJobEntity.class, importId);
//  }
//
//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  public Key<GoogleDocImportJobEntity> getKey() {
//    return generateKey(moduleType, resourceId);
//  }
//
//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  public GoogleDocInfoDto toDto() {
//    // TODO(arjuns): Auto-generated method stub
//    throw new UnsupportedOperationException();
//  }
//
//  public String getId() {
//    return id;
//  }
//
//  public ModuleType getModuleType() {
//    return moduleType;
//  }
//
//  public String getResourceId() {
//    return resourceId;
//  }
//
//  public PersonId getPersonId() {
//    return new PersonId(personId);
//  }
//
//  public String getAdditionalJsonInfo() {
//    return additionalJsonInfo;
//  }
//
//  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
//    private ModuleType moduleType;
//    private String resourceId;
//    private PersonId personId;
//    private String additionalJsonInfo;
//
//    public Builder moduleType(ModuleType moduleType) {
//      this.moduleType = moduleType;
//      return this;
//    }
//
//    public Builder resourceId(String resourceId) {
//      this.resourceId = resourceId;
//      return this;
//    }
//
//    public Builder personId(PersonId personId) {
//      this.personId = personId;
//      return this;
//    }
//    
//    public Builder additionalJsonInfo(String additionalJsonInfo) {
//      this.additionalJsonInfo = additionalJsonInfo;
//      return this;
//    }
//
//    @SuppressWarnings("synthetic-access")
//    public GoogleDocImportJobEntity build() {
//      return new GoogleDocImportJobEntity(this).validate();
//    }
//  }
//
//  @SuppressWarnings("synthetic-access")
//  private GoogleDocImportJobEntity(Builder builder) {
//    super(builder, false);
//    this.moduleType = checkNotNull(builder.moduleType, "moduleType");
//    this.resourceId = checkNotBlank(builder.resourceId, "resourceId");
//    this.id = computeId(moduleType, resourceId);
//    checkPersonId(builder.personId);
//    this.personId = builder.personId.get();
//    this.additionalJsonInfo = checkNotBlank(builder.additionalJsonInfo, "additionalJsonInfo");
//  }
//
//  // For Objectify.
//  private GoogleDocImportJobEntity() {
//    super(null, false);
//  }
//}
