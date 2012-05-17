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
package com.google.light.server.jersey.resources.test;

import static com.google.light.server.utils.LightPreconditions.checkIsNotEnv;
import static com.google.light.server.utils.LightUtils.appendSectionHeader;
import static com.google.light.server.utils.LightUtils.appendSessionData;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.HtmlPathEnum;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.QueueEnum;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.module.ExternalIdMappingEntity;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionResourceEntity;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import java.util.Iterator;
import java.util.List;
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
@Path(JerseyConstants.RESOURCE_PATH_TEST)
public class TestResources extends AbstractJerseyResource {

  @Inject
  public TestResources(Injector injector, HttpServletRequest request, HttpServletResponse response) {
    super(injector, request, response);
    checkIsNotEnv(this, LightEnvEnum.PROD);
  }

  @GET
  @Path(JerseyConstants.PATH_SESSION)
  @Produces(ContentTypeConstants.TEXT_HTML)
  public Response getSessionDetails() {
    SessionManager sessionManager = GuiceUtils.getInstance(SessionManager.class);

    if (!sessionManager.isPersonLoggedIn()) {
      throw new PersonLoginRequiredException("Login required.");
    }

    StringBuilder builder = new StringBuilder();
    appendSessionData(builder, sessionManager.getSession());

    return Response.ok(builder.toString()).build();
  }

  @GET
  @Path(JerseyConstants.PATH_TEST_LINKS)
  @Produces(ContentTypeConstants.TEXT_HTML)
  public Response getTestLinks() {
    try {
      StringBuilder builder = new StringBuilder();
      appendServletPath(builder, ServletPathEnum.OAUTH2_GOOGLE_LOGIN);

      appendSectionHeader(builder, "Admin Utilities");
      appendHtmlPath(builder, HtmlPathEnum.PUT_OAUTH2_CONSUMER_CREDENTIAL);
      appendHref(builder, JerseyConstants.URI_MISC_ADMIN_CONFIG, "Config");
      appendHref(builder, JerseyConstants.URI_RESOURCE_PATH_JOBS_ME, "Jobs by me");
      appendHref(builder, JerseyConstants.URI_TEST_DELETE_ALL, "DeleteAll");
      appendHref(builder, JerseyConstants.URI_TEST_PURGE_QUEUES, "PurgeQueues");

      appendSectionHeader(builder, "Owner OAuth2 Workflow :");
      appendServletPath(builder, ServletPathEnum.OAUTH2_GOOGLE_LOGIN);
      appendServletPath(builder, ServletPathEnum.OAUTH2_GOOGLE_DOC_AUTH);

      appendSectionHeader(builder, "Import Batch");
      appendHtmlPath(builder, HtmlPathEnum.IMPORT_BATCH);

      appendSectionHeader(builder, "Google Doc Integration");
      appendHref(builder, JerseyConstants.URI_GOOGLE_DOC_LIST, "Google Doc List");
      appendHtmlPath(builder, HtmlPathEnum.GOOGLE_DOC_INFORMATION);

      appendSectionHeader(builder, "CSE/GSS");
      appendHref(builder, ServletPathEnum.GSS.get(), "Search Admin Credentials");

      appendSectionHeader(builder, "Some helper Utils.");
      appendHtmlPath(builder, HtmlPathEnum.REST_CLIENT);

      appendSectionHeader(builder, "Module Utils.");
      appendHref(builder, JerseyConstants.URI_RESOURCE_PATH_MODULE_ME, "Modules Published by Me");
      appendHref(builder, JerseyConstants.URI_RESOURCE_PATH_MODULE_ME_HTML,
          "Modules Published by Me in HTML List");

      appendSectionHeader(builder, "Collection Utils.");
      appendHref(builder, JerseyConstants.URI_RESOURCE_PATH_COLLECTION_ME,
          "Collections Published by Me");
      appendHref(builder, JerseyConstants.URI_RESOURCE_PATH_COLLECTION_ME_HTML,
          "Collections Published by Me in HTML List");

      return Response.ok(builder.toString()).build();
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException(e);
    }
  }

  private void appendHtmlPath(StringBuilder builder, HtmlPathEnum htmlPath) {
    appendHref(builder, htmlPath.get(), htmlPath.name());
  }

  private void appendServletPath(StringBuilder builder, ServletPathEnum servletPath) {
    appendHref(builder, servletPath.get(), servletPath.name());
  }

  private void appendHref(StringBuilder builder, String href, String name) {
    String idStr = "id=" + name;
    builder.append("<a " + idStr + " target=_blank href=\"").append(href).append("\">")
        .append(name).append("</a><br>");
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @GET
  @Produces(ContentTypeConstants.TEXT_PLAIN)
  @Path(JerseyConstants.PATH_TEST_DELETE_ALL)
  public  String deleteAll() throws ClassNotFoundException {
    List<String> tableNames = Lists.newArrayList(
        ModuleEntity.class.getName(),
        ModuleVersionEntity.class.getName(),
        ModuleVersionResourceEntity.class.getName(),

        CollectionEntity.class.getName(),
        CollectionVersionEntity.class.getName(),

        ExternalIdMappingEntity.class.getName(),
        JobEntity.class.getName());

    StringBuilder builder = new StringBuilder("\nDeleting Tables.");
    Objectify ofy = ObjectifyUtils.nonTransaction();
    for (String currClassName : tableNames) {
      Class currClass = this.getClass().getClassLoader().loadClass(currClassName);
      builder.append("\n\n  Deleting " + currClass.getSimpleName());
      QueryResultIterable queryIterable = ofy.query(currClass).fetchKeys();
      Iterator iterator = queryIterable.iterator();
      while(iterator.hasNext()) {
        ofy.delete(iterator.next());
      }
      
      builder.append("\n  Successfully deleted " + currClass.getSimpleName());
    }
    
    builder.append(purgeQueue());
    
    return builder.toString();
  }
  
  @GET
  @Produces(ContentTypeConstants.TEXT_PLAIN)
  @Path(JerseyConstants.PATH_TEST_PURGE_QUEUES)
  public String purgeQueue() {
    StringBuilder builder = new StringBuilder("\n\n\nPurging Queues.");
    for (QueueEnum currQueue : QueueEnum.values()) {
      builder.append("\n\n  Purging Queue " + currQueue.getName());
      Queue queue = QueueFactory.getQueue(currQueue.getName());
      queue.purge();
      builder.append("\n  Successfully Purged Queue " + currQueue.getName());
    }
    
    return builder.toString();
  }
}
