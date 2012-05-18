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
package com.google.light.server.manager.implementation;

import static com.google.light.server.utils.LightUtils.convertModuleIdToFtsDocumentId;

import com.google.light.server.dto.pages.PageDto;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.light.server.constants.fts.FTSIndex;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.FTSDocumentId;
import com.google.light.server.dto.pojo.wrappers.FTSDocumentWrapper;
import com.google.light.server.manager.interfaces.FTSManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.utils.FTSUtils;
import com.google.light.server.utils.LightUtils;

/**
 * Implementation for @link {@link GAESearchManager}.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class FTSManagerImpl implements FTSManager {
  private ModuleManager moduleManager;
  
  @Inject
  public FTSManagerImpl(ModuleManager moduleManager) {
    this.moduleManager = Preconditions.checkNotNull(moduleManager, "moduleManager");
    
  }
  /** 
   * {@inheritDoc}
   */
  @Override
  public void indexModule(ModuleId moduleId) {
    ModuleVersionEntity mvEntity = moduleManager.getModuleVersion(
        null, moduleId, LightUtils.LATEST_VERSION);
    
    FTSDocumentId ftsDocumentId = convertModuleIdToFtsDocumentId(moduleId);
    FTSDocumentWrapper ftsDocumentWrapper = new FTSDocumentWrapper.Builder()
          .ftsDocumentId(ftsDocumentId)
          .title(mvEntity.getTitle())
          .content(mvEntity.getContent())
          .contentType(ContentTypeEnum.TEXT_HTML)
          .publishTime(mvEntity.getCreationTime())
          .build();
    
    FTSUtils.addDocumentToIndex(ftsDocumentWrapper, FTSIndex.module);
  }
  /** 
   * {@inheritDoc}
   */
  @Override
  public PageDto searchForModule(String query, int maxResults, String startIndex) {
    return FTSUtils.findDocuments(query, maxResults, startIndex, FTSIndex.module);
  }
}
