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

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

/**
 *
 * Source : https://developers.google.com/storage/docs/accesscontrol#managingaccess
 * See "Applying ACLs with the extension request Header".
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public enum GoogleCloudStorageAcls {
  PROJECT_PRIVATE("project-private"),
  PRIVATE("private"),
  PUBLIC_READ("public-read"),
  PUBLIC_READ_WRITE("public-read-write"),
  AUTHENTICATED_READ("authenticated-read"),
  BUCKET_OWNER_READ("bucket-owner-read"),
  BUCKET_OWNER_FULL_CONTROL("bucket-owner-full-control");
  
  private String acl;
  private GoogleCloudStorageAcls(String acl) {
    this.acl = checkNotBlank(acl, "acl");
  }
  
  public String get() {
    return acl;
  }
}
