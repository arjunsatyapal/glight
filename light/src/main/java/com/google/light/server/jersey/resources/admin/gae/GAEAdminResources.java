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
package com.google.light.server.jersey.resources.admin.gae;

import static com.google.light.server.constants.LightConstants.LIGHT_BOT_EMAIL;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkPersonIsGaeAdmin;
import static com.google.light.server.utils.LightUtils.appendAttention;
import static com.google.light.server.utils.LightUtils.appendKeyValue;
import static com.google.light.server.utils.LightUtils.appendSectionHeader;
import static com.google.light.server.utils.LightUtils.appendSessionData;
import static com.google.light.server.utils.LightUtils.getPST8PDTime;

import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.manager.implementation.oauth2.consumer.OAuth2ConsumerCredentialManagerFactory;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.GaeUtils;
import com.google.light.server.utils.LightPreconditions;
import com.google.light.server.utils.ServletUtils;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
@Path(JerseyConstants.RESOURCE_PATH_MISC_ADMIN)
public class GAEAdminResources extends AbstractJerseyResource {
  private OAuth2ConsumerCredentialManagerFactory consumerCredManagerFactory;
  private PersonManager personManager;

  @Inject
  public GAEAdminResources(Injector injector, HttpServletRequest request,
      HttpServletResponse response) {
    super(injector, request, response);

    checkPersonIsGaeAdmin();

    consumerCredManagerFactory = getInstance(OAuth2ConsumerCredentialManagerFactory.class);
    personManager = getInstance(PersonManager.class);
  }

  @SuppressWarnings("deprecation")
  @GET
  @Produces(ContentTypeConstants.TEXT_HTML)
  @Path(JerseyConstants.PATH_CONFIG)
  public Response getConfig() {
    LightPreconditions.checkPersonIsGaeAdmin();

    try {
      StringBuilder builder = new StringBuilder();
      Environment env = ApiProxy.getCurrentEnvironment();
      appendSectionHeader(builder, "Appengine SystemProperty");
      appendKeyValue(builder, "ApiProxy.getCurrentEnvironment().getAppId()", GaeUtils.getAppId());
      appendKeyValue(builder, "SystemProperty.applicationId.get()", GaeUtils.getAppIdFromSystemProperty());

      String applicationVersion = SystemProperty.applicationVersion.get();
      appendKeyValue(builder, "applicationVersion", applicationVersion);

      String[] splits = applicationVersion.split("\\.");
      appendKeyValue(builder, "version", splits[0]);

      /*
       * Source :
       * stackoverflow.com/questions/3948861/appengine-get-current-serving-application-version
       */
      Long timeStamp = (long) (Long.parseLong(splits[1]) / (long) Math.pow(2, 28));
      appendKeyValue(builder, "possible deployTime (PST8PDT)", getPST8PDTime(timeStamp * 1000));

      // appendKeyValue("version", applicationVersion.split(".")[0]);
      /*
       * TODO(arjuns): Replace with ServerService when Available. For now suppressing deprecated
       * warning.
       */
      appendKeyValue(builder, "instanceReplicaId", SystemProperty.instanceReplicaId.get());
      appendKeyValue(builder, "JavaSdkVersion", SystemProperty.version.get());

      appendSectionHeader(builder, "Appengine Environment");
      appendKeyValue(builder, "applicationEnvironment", SystemProperty.environment.get());

      appendKeyValue(builder, "isDevServer", GaeUtils.isDevServer());
      appendKeyValue(builder, "isProduction", GaeUtils.isProductionServer());
      appendKeyValue(builder, "isQaServer", GaeUtils.isQaServer());
      appendKeyValue(builder, "isUnitTestServer", GaeUtils.isUnitTestServer());
      appendKeyValue(builder, "authDomain", env.getAuthDomain());
      appendKeyValue(builder, "env.applicationId", env.getAppId());
      appendKeyValue(builder, "email", env.getEmail());

      Map<String, Object> map = env.getAttributes();
      for (String currKey : map.keySet()) {
        appendKeyValue(builder, currKey, map.get(currKey));
      }

      // Populating Session Data.
      HttpSession session = ServletUtils.getSession(request);
      appendSessionData(builder, session);

      // Populating system status.
      populateConsumerCredentialStatus(builder);

      populateRequirePersons(builder);

      // TODO(arjuns): Add things here to fetch acl from GoogleCloudStorage for Required Buckets.

      return Response.ok().entity(builder.toString()).build();
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException(e);
    }
  }

  /**
   * @param builder
   */
  private void populateRequirePersons(StringBuilder builder) {
    appendSectionHeader(builder, "Required Persons");
    PersonEntity personEntity = personManager.findByEmail(LIGHT_BOT_EMAIL);

    String key = "LightBot[" + LIGHT_BOT_EMAIL + "]";
    if (personEntity == null) {
      appendAttention(builder, key, "Missing.");
    } else {
      appendKeyValue(builder, key, "ok");
    }
  }

  /**
     * 
     */
  private void populateConsumerCredentialStatus(StringBuilder builder) {
    appendSectionHeader(builder, "Consumer Credential Status");
    for (OAuth2ProviderEnum currProvider : OAuth2ProviderEnum.values()) {
      try {
        consumerCredManagerFactory.create(currProvider);
        appendKeyValue(builder, currProvider.name(), "ok");
      } catch (Exception e) {
        appendAttention(builder, currProvider.name(), "Needs Attention.");
      }
    }
  }
}
