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
package com.google.light.server.jobs.handlers.modulejobs.youtube;

import static com.google.light.server.jobs.JobUtils.updateJobContext;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightUtils.createCollectionNode;
import static com.google.light.server.utils.LightUtils.getNow;
import static com.google.light.server.utils.LightUtils.getURI;
import static com.google.light.server.utils.ObjectifyUtils.repeatInTransaction;

import com.google.inject.Inject;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.httpclient.LightHttpClient;
import com.google.light.server.jobs.handlers.JobHandlerInterface;
import com.google.light.server.jobs.handlers.modulejobs.synthetic.ImportModuleSyntheticModuleJobContext;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.urls.YouTubeUrl;
import com.google.light.server.utils.HtmlBuilder;
import com.google.light.server.utils.Transactable;
import com.googlecode.objectify.Objectify;
import java.net.URI;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ImportModuleYouTubeVideoJobHandler implements JobHandlerInterface {
  private JobManager jobManager;
  private ModuleManager moduleManager;

  @Inject
  public ImportModuleYouTubeVideoJobHandler(LightHttpClient httpClient, JobManager jobManager,
      ModuleManager moduleManager) {
    checkNotNull(httpClient, ExceptionType.SERVER_GUICE_INJECTION, "httpClient");
    this.jobManager = checkNotNull(jobManager, ExceptionType.SERVER_GUICE_INJECTION, "jobManager");
    this.moduleManager = checkNotNull(moduleManager, ExceptionType.SERVER_GUICE_INJECTION,
        "moduleManager");
  }

  @Override
  public void handle(final JobEntity jobEntity) {
    final ImportModuleSyntheticModuleJobContext context =
        jobEntity.getContext(ImportModuleSyntheticModuleJobContext.class);

    final String htmlContent = generateHtmlContent(context);

    repeatInTransaction("publishing module " + context.getModuleId() + "]", 
        new Transactable<Void>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Void run(Objectify ofy) {
        // First publish HTML on Light.
        moduleManager.publishModuleVersion(ofy, context.getModuleId(), context.getVersion(),
            context.getExternalId(), context.getTitle(), htmlContent, 
            context.getModuleType().getDefaultLicenses(), null, getNow());

        updateJobContext(ofy, jobManager, JobState.COMPLETE, context, jobEntity,
            "Published module[" + context.getModuleId() + ":" + context.getVersion());
        return null;
      }
    });

    CollectionTreeNodeDto collectionNode = createCollectionNode(null/*description*/,
        context.getExternalId(), context.getModuleId(), context.getModuleType(), null/*nodeId*/, 
        TreeNodeType.LEAF_NODE, context.getTitle(), context.getVersion());
    jobManager.enqueueCompleteJob(jobEntity.getJobId(), collectionNode, "Marking as complete");
  }

  /**
   * @param context
   * @return
   */
  private String generateHtmlContent(ImportModuleSyntheticModuleJobContext context) {
    URI canonicalUri = getURI(context.getExternalId().getValue());
    YouTubeUrl ytUrl = new YouTubeUrl(context.getExternalId());
    
    HtmlBuilder htmlBuilder = new HtmlBuilder();

    htmlBuilder.appendDoctype();
    htmlBuilder.appendHeadStart();
    htmlBuilder.appendCanonicalUri(canonicalUri);
    htmlBuilder.appendStyle(getIframeStyle());

    htmlBuilder.appendHeadEnd();
    htmlBuilder.appendBodyStart();
    htmlBuilder.appendIFrame(ytUrl.getEmbedUri(), "505px", "853px");
    htmlBuilder.appendBodyEnd();

    String htmlContent = htmlBuilder.toString();
    return htmlContent;
  }

  /**
   * @return
   */
  private String getIframeStyle() {
    StringBuilder builder = new StringBuilder();
    builder.append("html, body, iframe {").append("\n")
        .append("padding: 0'").append("\n")
        .append("margin: 0;").append("\n")
        .append("border: 0;").append("\n")
        .append("width: 100%;").append("\n")
        .append("height: 100%;").append("\n")
        .append("}");

    return builder.toString();
  }
}
