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
package com.google.light.server.constants.google.cloudstorage;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.constants.LightEnvEnum;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public enum GoogleCloudStorageBuckets {
  // visible to everyone.
  CONTENT("content", GoogleCloudStorageAcls.PUBLIC_READ),
  
  // Used by Light for purpose of storage.
  WORKSPACE("workspace", GoogleCloudStorageAcls.BUCKET_OWNER_FULL_CONTROL);
  
  private String bucketCategory;
  private GoogleCloudStorageAcls acl;
  
  private GoogleCloudStorageBuckets(String bucketCategory, GoogleCloudStorageAcls acl) {
    this.bucketCategory = checkNotBlank(bucketCategory, "bucketCategory");
    this.acl = checkNotNull(acl, "acl");
  }
  
  public String getBucketName() {
    return "light-" + LightEnvEnum.getLightEnv().name().toLowerCase()
        + "-" + bucketCategory + "-bucket";
  }
  
  public String getAbsoluteFilePath(String relativeFilePath) {
    return "/gs/" + getBucketName() + "/" + relativeFilePath;
  }

  public GoogleCloudStorageAcls getAcl() {
    return acl;
  }
}
