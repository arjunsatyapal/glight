package com.google.light.server.jersey.resources;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightConstants.MAX_RESULTS_MAX;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonLoggedIn;
import static com.google.light.server.utils.LightUtils.isListEmpty;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.LightStringConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.dto.module.ModuleDto;
import com.google.light.server.dto.module.ModuleVersionDto;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.utils.GuiceUtils;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import org.apache.commons.lang.StringUtils;

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

  @Inject
  public ModuleResource(Injector injector, @Context HttpServletRequest request,
      @Context HttpServletResponse response, ModuleManager moduleManager) {
    super(injector, request, response);
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
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  public ModuleDto getModule(
      @PathParam(JerseyConstants.PATH_PARAM_MODULE_ID)String moduleIdStr) {
    ModuleId moduleId = new ModuleId(moduleIdStr);
    ModuleEntity moduleEntity = moduleManager.get(null, moduleId);
    
    if (moduleEntity == null) {
      throw new NotFoundException("Module[" + moduleId + "] was not found.");
    }
    
    return moduleEntity.toDto();
  }

  @GET
  @Path(JerseyConstants.PATH_MODULE_VERSION)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  public ModuleVersionDto getModuleVersion(
      @PathParam(JerseyConstants.PATH_PARAM_MODULE_ID) String moduleIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr) {
    checkNotBlank(versionStr, "Invalid version.");
    ModuleVersionDto moduleVersion= getModuleVersionDto(moduleIdStr, versionStr);

    return moduleVersion;
  }

  protected ModuleVersionDto getModuleVersionDto(String moduleIdStr, String versionStr) {
    checkNotBlank(moduleIdStr, "Invalid ModuleId.");
    checkNotBlank(versionStr, "Invalid version.");
    ModuleId moduleId = new ModuleId(moduleIdStr);
    Version version = new Version(versionStr);

    ModuleVersionEntity moduleEntity = moduleManager.getModuleVersion(
        null /* ofy */, moduleId, version);

    if (moduleEntity == null) {
      throw new NotFoundException("Could not find " + moduleId + ":" + version);
    }

    return moduleEntity.toDto();
  }

  @GET
  @Path(JerseyConstants.PATH_ME)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  public PageDto getModulesPublishedByMe(
      @QueryParam(LightStringConstants.START_INDEX_STR) String startIndex,
      @QueryParam(LightStringConstants.MAX_RESULTS_STR) String maxResultStr) {
    SessionManager sessionManager = GuiceUtils.getInstance(SessionManager.class);
    checkPersonLoggedIn(sessionManager);
    
    int maxResult = LightConstants.MAX_RESULTS_DEFAULT;
    
    if (StringUtils.isNotBlank(maxResultStr)) {
      maxResult = Integer.parseInt(maxResultStr);
    }
    
    checkArgument(maxResult <= MAX_RESULTS_MAX, "Max results allowed = " + MAX_RESULTS_MAX);
    
    return moduleManager.findModulesByOwnerId(GuiceUtils.getOwnerId(), startIndex, maxResult);
  }
  
  @SuppressWarnings("unchecked")
  @GET
  @Path(JerseyConstants.PATH_ME_HTML)
  @Produces( ContentTypeConstants.TEXT_HTML )
  public String getModulesPublishedByMeHtml() {
    SessionManager sessionManager = GuiceUtils.getInstance(SessionManager.class);
    checkPersonLoggedIn(sessionManager);
    
    PageDto pageDto = moduleManager.findModulesByOwnerId(GuiceUtils.getOwnerId(), null, 5000);
    
    StringBuilder htmlBuilder = new StringBuilder("Modules created by me : <br>");
    
    List<ModuleDto> list = ((List<ModuleDto>) pageDto.getList());
    if (!isListEmpty(list)) {
      int counter = 1;
      for (ModuleDto currDto : list) {
        htmlBuilder.append(counter++)
        .append(".&nbsp")
        .append("<a href=" + "/rest/module/")
        .append(currDto.getModuleId().getValue())
        .append("/latest/content")
        .append(">")
        .append(currDto.getTitle())
        .append("</a><br>\n");
      }
    }
    
    return htmlBuilder.toString();
  }
}
