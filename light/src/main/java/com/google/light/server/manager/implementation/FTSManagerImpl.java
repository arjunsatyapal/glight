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

import com.google.light.server.persistence.entity.module.ModuleVersionEntity;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.light.server.constants.fts.FTSIndex;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.FTSDocumentId;
import com.google.light.server.dto.pojo.wrappers.FTSDocumentWrapper;
import com.google.light.server.manager.interfaces.FTSManager;
import com.google.light.server.utils.FTSUtils;
import java.util.List;

/**
 * Implementation for @link {@link GAESearchManager}.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class FTSManagerImpl implements FTSManager {
  @Inject
  public FTSManagerImpl() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PageDto searchForModule(String query, int maxResults, String startIndex) {
    return FTSUtils.findDocuments(query, maxResults, startIndex, FTSIndex.module);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void indexModules(List<ModuleVersionEntity> listOfModuleVersions) {
    List<FTSDocumentWrapper> listOfFTSDocuments = Lists.newArrayListWithExpectedSize(
        listOfModuleVersions.size());

    for (ModuleVersionEntity currModuleVersion : listOfModuleVersions) {
      FTSDocumentId ftsDocumentId = convertModuleIdToFtsDocumentId(currModuleVersion.getModuleId());
      FTSDocumentWrapper ftsDocumentWrapper = new FTSDocumentWrapper.Builder()
          .ftsDocumentId(ftsDocumentId)
          .title(currModuleVersion.getTitle())
          .content(currModuleVersion.getContent())
          .contentType(ContentTypeEnum.TEXT_HTML)
          .publishTime(currModuleVersion.getCreationTime())
          .build();
      listOfFTSDocuments.add(ftsDocumentWrapper);
    }
    
    FTSUtils.indexDocuments(listOfFTSDocuments, FTSIndex.module);
  }
}
