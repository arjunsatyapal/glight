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
package com.google.light.server.jersey.resources.search;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.manager.interfaces.FTSManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.serveronlypojos.GAEQueryWrapper;
import com.google.light.server.utils.ObjectifyUtils;
import com.google.light.server.utils.Transactable;
import com.googlecode.objectify.Objectify;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@Path(JerseyConstants.RESOURCE_PATH_SEARCH_INDEX)
public class SearchIndexResource extends AbstractJerseyResource {
  private static final Logger logger = Logger.getLogger(SearchIndexResource.class.getName());

  private FTSManager ftsManager;
  private ModuleManager moduleManager;

  @Inject
  public SearchIndexResource(Injector injector, HttpServletRequest request,
      HttpServletResponse response, FTSManager ftsManager, ModuleManager moduleManager) {
    super(injector, request, response);
    this.ftsManager = checkNotNull(ftsManager, "ftsManager");
    this.moduleManager = checkNotNull(moduleManager, "moduleManager");
  }

  @GET
  @Produces(ContentTypeConstants.TEXT_PLAIN)
  @Path(JerseyConstants.PATH_SEARCH_INDEX_UPDATE_INDICES)
  public Response updateIndices() {
    StringBuilder builder = new StringBuilder();

    try {
      GAEQueryWrapper<ModuleVersionEntity> queryWrapper =
          moduleManager.findModuleVersionsForFTSIndexUpdate(LightConstants.MAX_RESULTS_MAX, null);

      List<ModuleVersionEntity> listOfModuleVersions = queryWrapper.getList();
      for (ModuleVersionEntity curr : listOfModuleVersions) {
        builder.append(curr.getModuleId() + ",");
      }
      logger.info("Found (" + listOfModuleVersions.size() + ") modules for FTSIndexUpdate : "
          + builder.toString());

      ftsManager.indexModules(listOfModuleVersions);
      logger.info("Finished indexing for FTS");

      for (ModuleVersionEntity currModule : listOfModuleVersions) {
        String logMsg = null;
        try {
          updateFTSIndexFlag(currModule);
          logMsg = "Successfully updated IndexFTSFlag for " + currModule.getModuleId();
        } catch (Throwable throwable) {
          logMsg = "Failed to update IndexFTSFlag for " + currModule.getModuleId() + " due to : " +
              Throwables.getStackTraceAsString(throwable);
        }

        logger.info(logMsg);
      }
    } catch (Exception e) {
      // Eating exception.
    }

    return Response.ok("Finished for : " + builder.toString()).build();
  }

  private Void updateFTSIndexFlag(final ModuleVersionEntity moduleVersion) {
    return ObjectifyUtils.repeatInTransaction(new Transactable<Void>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Void run(Objectify ofy) {
        ModuleEntity fetchedEntity = moduleManager.get(ofy, moduleVersion.getModuleId());
        if (fetchedEntity.getLatestPublishVersion().equals(
            moduleVersion.getVersion())) {
          /*
           * No new version was published while updating FTS Index. So it is safe to mark FTS
           * Flag as done.
           */
          fetchedEntity.getSearchIndexStatus().setIndexForFTS(false);
          moduleManager.put(ofy, fetchedEntity);
        }
        return null;
      }
    });
  }
}
