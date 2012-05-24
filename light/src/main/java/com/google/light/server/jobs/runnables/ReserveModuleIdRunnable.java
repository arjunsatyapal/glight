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
package com.google.light.server.jobs.runnables;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;
import static com.google.light.server.utils.ObjectifyUtils.repeatInTransaction;

import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.pojo.tree.externaltree.ExternalIdTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.Transactable;
import com.googlecode.objectify.Objectify;
import java.util.List;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ReserveModuleIdRunnable implements Runnable {
  private final ExternalIdTreeNodeDto externalIdTreeNode;
  private ImportExternalIdDto importExternalIdDto;
  private final List<PersonId> owners;

  public ReserveModuleIdRunnable(ExternalIdTreeNodeDto externalIdTreeNode, List<PersonId> owners) {
    this.externalIdTreeNode = checkNotNull(externalIdTreeNode, "externalIdTreeNode");
    this.owners = checkNotEmptyCollection(owners, "owners");
  }

  public ImportExternalIdDto getImportExternalIdDto() {
    return importExternalIdDto;
  }
  
  @Override
  public void run() {
    repeatInTransaction("reserving moduleId for " + externalIdTreeNode.getExternalId() + "]", 
        new Transactable<Void>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Void run(Objectify ofy) {
        ExternalId externalId = externalIdTreeNode.getExternalId();
        System.out.println(externalId);
        ModuleManager moduleManager = GuiceUtils.getInstance(ModuleManager.class);
        ModuleId moduleId = moduleManager.reserveModuleId(ofy, externalId, owners, 
            externalIdTreeNode.getTitle(),
            ContentLicense.DEFAULT_LIGHT_CONTENT_LICENSES);
        externalIdTreeNode.setModuleId(moduleId);
        return null;
      }
    });
  }
}
