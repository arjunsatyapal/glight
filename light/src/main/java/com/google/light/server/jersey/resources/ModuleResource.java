package com.google.light.server.jersey.resources;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightStringConstants.VERSION_LATEST_STR;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.ResourceTypes;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionResourceEntity;
import com.google.light.server.utils.LightUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * Jersey Resource for Modules.
 * 
 * TODO(arjuns): Add test for this.
 * 
 * 
 * @author arjuns@google.com (Arjun Satyapal)
 */
@Path(JerseyConstants.RESOURCE_PATH_MODULE)
public class ModuleResource extends AbstractJerseyResource {
  private final ModuleManager moduleManager;
  private final BlobstoreService blobService;

  @Inject
  public ModuleResource(Injector injector, @Context HttpServletRequest request,
      @Context HttpServletResponse response, ModuleManager moduleManager) {
    super(injector, request, response);
    this.blobService = checkNotNull(BlobstoreServiceFactory.getBlobstoreService());
    this.moduleManager = checkNotNull(moduleManager, "moduleManager");
  }

  /**
   * Get on a ModuleId will redirect to Latest Version for that ModuleId.
   * 
   * @param moduleId
   * @return
   */
  @GET
  @Path(JerseyConstants.PATH_MODULE_ID)
  public Response getModule() {
    String uri = request.getRequestURI() + "/" + VERSION_LATEST_STR;
    return Response.seeOther(LightUtils.getURI(uri)).build();
  }

  @GET
  @Path(JerseyConstants.PATH_MODULE_VERSION)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.TEXT_XML })
  public String getModuleVersion(
      @PathParam(JerseyConstants.PATH_PARAM_MODULE_ID) String moduleIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr) {
    checkNotBlank(versionStr, "Invalid version.");
    ModuleId moduleId = new ModuleId(moduleIdStr);
    Version version = Version.createVersion(versionStr);

    ModuleVersionEntity moduleVersionEntity = moduleManager.getModuleVersion(moduleId, version);

    return moduleVersionEntity.getContent();
  }

  @GET
  @Path(JerseyConstants.PATH_MODULE_VERSION_CONTENT)
  @Produces(ContentTypeConstants.TEXT_HTML)
  public String getModuleVersionContent(
      @PathParam(JerseyConstants.PATH_PARAM_MODULE_ID) String moduleIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr) {
    ModuleVersionEntity moduleEntity = getModuleVersionEntity(moduleIdStr, versionStr);

    return moduleEntity.getContent();
  }

  @GET
  @Path(JerseyConstants.PATH_MODULE_RESOURCE)
  public Response getModuleVersionResources(
      @PathParam(JerseyConstants.PATH_PARAM_MODULE_ID) String moduleIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr,
      @PathParam(JerseyConstants.PATH_PARAM_RESOURCE_TYPE) String resourceTypeStr,
      @PathParam(JerseyConstants.PATH_PARAM_RESOURCE) String resourceStr) {
    checkNotBlank(resourceTypeStr, "resourceType");
    checkNotBlank(resourceStr, "resource");
    ResourceTypes resourceType = ResourceTypes.valueOf(resourceTypeStr);

    ModuleId moduleId = new ModuleId(moduleIdStr);
    Version version = Version.createVersion(versionStr);
    String resourceId = resourceType.name() + "/" + resourceStr;

    ModuleVersionResourceEntity resourceEntity = moduleManager.getModuleResource(
        moduleId, version, resourceId);

    if (resourceEntity == null) {
      throw new NotFoundException("Resource[" + resourceId + "] Not found for "
          + moduleId + ":" + version);
    }

    BlobKey blobKey = blobService.createGsBlobKey(resourceEntity.getGSBlobInfo().getGsKey());
    try {
      checkNotNull(response, "response");
      blobService.serve(blobKey, response);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    ResponseBuilder responseBuilder = Response.ok();
    responseBuilder.header(HttpHeaderEnum.CONTENT_TYPE.get(),
        resourceEntity.getGSBlobInfo().getContentType().get());
    return responseBuilder.build();
  }

  protected ModuleVersionEntity getModuleVersionEntity(String moduleIdStr, String versionStr) {
    checkNotBlank(moduleIdStr, "Invalid ModuleId.");
    checkNotBlank(versionStr, "Invalid version.");
    ModuleId moduleId = new ModuleId(moduleIdStr);
    Version version = Version.createVersion(versionStr);

    ModuleVersionEntity moduleEntity = moduleManager.getModuleVersion(moduleId, version);

    if (moduleEntity == null) {
      throw new NotFoundException("Could not find " + moduleId + ":" + version);
    }

    return moduleEntity;
  }
}
