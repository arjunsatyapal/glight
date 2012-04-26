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
package com.google.light.server.jobs.importjobs.google;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.light.server.constants.google.cloudstorage.GoogleCloudStorageBuckets.CONTENT;
import static com.google.light.server.constants.google.cloudstorage.GoogleCloudStorageBuckets.WORKSPACE;
import static com.google.light.server.utils.GoogleCloudStorageUtils.getAbsolutePathOnBucket;
import static com.google.light.server.utils.GoogleCloudStorageUtils.writeFileOnGCS;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightUtils.getRandomFileName;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.GSFileOptions;
import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Value;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.light.jobs.LightJob2;
import com.google.light.jobs.LightJob3;
import com.google.light.jobs.LightJob4;
import com.google.light.server.constants.FileExtensions;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.constants.google.cloudstorage.GoogleCloudStorageBuckets;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.GoogleDocArchivePojo;
import com.google.light.server.dto.pojo.LightJobContextPojo;
import com.google.light.server.dto.pojo.ModuleId;
import com.google.light.server.dto.pojo.Version;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.exception.unchecked.GoogleDocException;
import com.google.light.server.exception.unchecked.pipelineexceptions.ignore.GoogleDocArchivalWaitingException;
import com.google.light.server.jobs.importjobs.PipelineJobs;
import com.google.light.server.jobs.lightjobs.ModuleJobs;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.interfaces.ImportManager;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.GoogleCloudStorageUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightUtils;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ImportGoogleDocJobs {
  static final Logger logger = Logger.getLogger(ImportGoogleDocJobs.class.getName());

  @SuppressWarnings("serial")
  public static class CreateArchive extends LightJob2<LightJobContextPojo, String> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<LightJobContextPojo> handler(String importEntityId) {
      ImportManager importManager = getInstance(ImportManager.class);
      ImportJobEntity entity = importManager.get(importEntityId);

      GoogleDocResourceId resourceId = new GoogleDocResourceId(entity.getResourceId());

      DocsServiceWrapper docsService = getInstance(DocsServiceWrapper.class);
      // GoogleDocArchivePojo pojo = docsService.archiveResource(resourceId);

      String archiveId =
          "vax2Nuv9Oq4VIPQEBTcxlJDkNbRFv7KVNL`55qVoitx7rtrJHru1T6ljtTjMW26L3ygWrvW1tMZHHg6ARCy3Uj1iCedCKgI-aCe_1-3S8hBATsyi7KAFDUPl9DojxAJkJY8leCmCujQQ";

      System.out.println("ArchiveId = \n" + archiveId);

      updateChangeLog(importManager, entity, "Asking Google to create an archive.");

      return futureCall(new ImportGoogleDocJobs.WaitForArchiveToComplete(),
          immediate(getContext()), immediate(importEntityId), immediate(archiveId));
    }
  }

  @SuppressWarnings("serial")
  public static class WaitForArchiveToComplete extends
      LightJob3<LightJobContextPojo, String, String> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<LightJobContextPojo> handler(String importEntityId, String archiveId) {
      ImportManager importManager = getInstance(ImportManager.class);
      ImportJobEntity entity = importManager.get(importEntityId);

      DocsServiceWrapper docsService = getInstance(DocsServiceWrapper.class);
      GoogleDocArchivePojo pojo = docsService.getArchiveStatus(archiveId);
      System.out.println(JsonUtils.toJson(pojo));

      switch (pojo.getArchiveStatus()) {
        case ARCHIVING:
          //$FALL-THROUGH$
        case FLATTENING:
          updateChangeLog(importManager, entity,
              "Google is prepairing archive. Will retry after some time.");
          throw new GoogleDocArchivalWaitingException();

        case FINISHED:
          updateChangeLog(importManager, entity,
              "Google finished prepairing archive. Handing over to downloader.");
          return futureCall(new ImportGoogleDocJobs.DonwloadArchive(),
              immediate(getContext()), immediate(importEntityId),
              immediate(pojo.getContentLocation()));

        default:
          throw new IllegalStateException("Unsupported State : " + pojo.getArchiveStatus());
      }
    }
  }

  @SuppressWarnings("serial")
  public static class DonwloadArchive extends LightJob3<LightJobContextPojo, String, String> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Value<LightJobContextPojo> handler(String importEntityId, String downloadUrl) {
      ImportManager importManager = getInstance(ImportManager.class);
      ImportJobEntity entity = importManager.get(importEntityId);

      updateChangeLog(importManager, entity, "Beginning Downloading.");

      try {
        HttpTransport transport = getInstance(HttpTransport.class);
        HttpRequest request = transport.createRequestFactory().buildGetRequest(
            new GenericUrl(downloadUrl));

        OAuth2OwnerTokenManagerFactory tokenManagerFactory =
            getInstance(OAuth2OwnerTokenManagerFactory.class);
        OAuth2OwnerTokenManager tokenManager =
            tokenManagerFactory.create(OAuth2ProviderService.GOOGLE_DOC);
        OAuth2OwnerTokenEntity token = tokenManager.get();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaderEnum.AUTHORIZATION.get(), token.getAuthorizationToken());
        request.setHeaders(httpHeaders);

        HttpResponse response = request.execute();

        String randomFileName = getRandomFileName(FileExtensions.ZIP);
        updateChangeLog(importManager, entity,
            "Finished Downloading. Now storing on GCS as [" +
                WORKSPACE.getAbsoluteFilePath(randomFileName) + "].");

        GSFileOptions gsFileOptions = GoogleCloudStorageUtils.getGCSFileOptionsForCreate(
            GoogleCloudStorageBuckets.WORKSPACE,
            ContentTypeEnum.APPLICATION_ZIP, randomFileName);

        // TODO(arjuns): Ensure that file does not exist.
        writeFileOnGCS(response.getContent(), gsFileOptions);

        String gcsFilePath = getAbsolutePathOnBucket(WORKSPACE, randomFileName);
        System.out.println(gcsFilePath);

        updateChangeLog(importManager, entity,
            "Finished storing on Blobstore. Handing over to zipdeflater with path[" + gcsFilePath
                + "].");

        return futureCall(new ImportGoogleDocJobs.ReserveModule(),
            immediate(getContext()), immediate(importEntityId),
            immediate(randomFileName));
      } catch (Exception e) {
        throw new GoogleDocException(e);
      }
    }
  }

  @SuppressWarnings("serial")
  public static class ReserveModule extends LightJob3<LightJobContextPojo, String, String> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<LightJobContextPojo> handler(String importEntityId, String fileName) {
      try {
        ImportManager importManager = getInstance(ImportManager.class);
        ImportJobEntity entity = importManager.get(importEntityId);
        updateChangeLog(importManager, entity, "Reserving a ModuleId.");

        FutureValue<ModuleId> moduleId = futureCall(new ModuleJobs.ReserveModule(),
            immediate(getContext()), immediate(importEntityId));

        return futureCall(new ImportGoogleDocJobs.InflateArchive(),
            immediate(getContext()), immediate(importEntityId), immediate(fileName), moduleId);
      } catch (Exception e) {
        throw new GoogleDocException(e);
      }
    }
  }

  @SuppressWarnings("serial")
  public static class InflateArchive extends
      LightJob4<LightJobContextPojo, String, String, ModuleId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<LightJobContextPojo> handler(String importEntityId, String fileName,
        ModuleId moduleId) {
      FileService fileService = FileServiceFactory.getFileService();
      Map<String, GSBlobInfo> resourceMap = Maps.newConcurrentMap();

      try {
        ImportManager importManager = getInstance(ImportManager.class);
        ImportJobEntity entity = importManager.get(importEntityId);
        updateChangeLog(importManager, entity, "Created Module with Id[" + moduleId.get() + "]");

        String readFileName = getAbsolutePathOnBucket(WORKSPACE, fileName);
        updateChangeLog(importManager, entity, "Starting unarchiving files from [" + readFileName
            + "].");

        AppEngineFile file = new AppEngineFile(readFileName);
        FileReadChannel readChannel = fileService.openReadChannel(file, true /* lock */);

        InputStream inputStream = Channels.newInputStream(readChannel);
        ZipInputStream zipIn = new ZipInputStream(inputStream);

        ZipEntry zipEntry = null;
        do {
          zipEntry = zipIn.getNextEntry();
          if (zipEntry == null) {
            continue;
          }
          logger.info(zipEntry.getName());

          // TODO(arjuns): Get contentType from File extension.
          // Storing file on Cloud Storage.
          String nameFromGoogleDoc = zipEntry.getName();

          checkArgument(nameFromGoogleDoc.contains("/"), "Unexpected name from GoogleDoc : "
              + nameFromGoogleDoc);
          String parts[] = nameFromGoogleDoc.split("/");

          String newFileName = "";
          // Parts[0] is always the folder whose name is same as prettified title for a GoogleDoc.
          if (parts.length == 2) {
            // This will be true for HTML file.
            newFileName = parts[1];
          } else {
            Preconditions.checkArgument(parts.length == 3, "Google Docs will return files " +
                "name of format <pretty title/images/image names>");
            // This will be true for images/resources.
            newFileName = parts[1] + "/" + parts[2];
          }

          if (newFileName.endsWith(FileExtensions.HTML.get())) {
            newFileName = moduleId.get() + "." + FileExtensions.HTML.get();
          }

          ContentTypeEnum contentType = FileExtensions.getFileExtension(newFileName).getContentType();
          String storeFileName = moduleId.get() + "/" + newFileName;
          String storeAbsFilePath = CONTENT.getAbsoluteFilePath(storeFileName); 

          GSBlobInfo gsBlobInfo = new GSBlobInfo.Builder()
              .contentType(contentType)
              .creationTime(LightUtils.getNow())
              .fileName(parts[parts.length - 1])
              .gsKey(storeAbsFilePath)
              .sizeInBytes(zipEntry.getSize())
              .creationTime(LightUtils.getNow())
              .lastUpdateTime(LightUtils.getNow())
              .build();
          resourceMap.put(newFileName, gsBlobInfo);

          updateChangeLog(importManager, entity, "Storing [" + zipEntry.getName() + "] as [" +
                  storeAbsFilePath + "].");

          GSFileOptions gsFileOptions = GoogleCloudStorageUtils.getGCSFileOptionsForCreate(
              GoogleCloudStorageBuckets.CONTENT, contentType, storeFileName);
          writeFileOnGCS(zipIn, gsFileOptions);

          updateChangeLog(importManager, entity, "Successfully stored");
        } while (zipEntry != null);

        zipIn.close();
        inputStream.close();
        readChannel.close();

        updateChangeLog(importManager, entity,
            "Finished unarchiving files from [" + fileName + "].");

        // Now lets add the moduleVersion. This will require adding html and resources.
        // First adding Html.
        FutureValue<Version> moduleVersionFV = futureCall(new AddContent(),
            immediate(getContext()), immediate(importEntityId), immediate(moduleId));

        futureCall(new PipelineJobs.DummyJob(), immediate(getContext()), waitFor(moduleVersionFV));

        int htmlCount = 0;
        List<FutureValue> jobsToWait = Lists.newArrayList();
        // Now moduleVersion is created. So adding resources for that.
        for (String currKey : resourceMap.keySet()) {
          // We dont want to store HTML files as resources. They are handled separately.
          if (currKey.endsWith(FileExtensions.HTML.get())) {
            checkArgument(htmlCount == 0, "Found more then one HtmlFiles for ImportId["
                + importEntityId + "].");
            htmlCount++;
            continue;
          }

          GSBlobInfo gsBlobInfo = resourceMap.get(currKey);
          logger.info(gsBlobInfo.toJson());

          FutureValue<Void> fv = futureCall(
              new ModuleJobs.AddModuleResources(),
              immediate(getContext()),
              immediate(moduleId),
              moduleVersionFV,
              immediate(importEntityId),
              immediate(currKey),
              immediate(gsBlobInfo));
          jobsToWait.add(fv);
        }

        for (FutureValue<Long> curr : jobsToWait) {
          futureCall(new PipelineJobs.DummyJob(), immediate(getContext()), waitFor(curr));
        }

        return immediate(getContext());
      } catch (Exception e) {
        throw new GoogleDocException(e);
      }
    }
  }

  @SuppressWarnings("serial")
  public static class AddContent extends LightJob3<Version, String, ModuleId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<Version> handler(String importEntityId, ModuleId moduleId) {
      try {
        ImportManager importManager = getInstance(ImportManager.class);
        ImportJobEntity entity = importManager.get(importEntityId);
        updateChangeLog(importManager, entity, "Adding html for moduleId[" + moduleId + "].");

        String filePath = GoogleCloudStorageUtils.getAbsoluteModuleHtmlPath(CONTENT, moduleId);
        FileService fileService = FileServiceFactory.getFileService();
        AppEngineFile file = new AppEngineFile(filePath);
        FileReadChannel readChannel = fileService.openReadChannel(file, true /* lock */);
        InputStream inputStream = Channels.newInputStream(readChannel);
        String htmlContent = LightUtils.getInputStreamAsString(inputStream);

        return futureCall(new ModuleJobs.AddModuleVersion(), immediate(getContext()),
            immediate(moduleId), immediate(importEntityId), immediate(htmlContent));
      } catch (Exception e) {
        throw new GoogleDocException(e);
      }
    }

  }

  // TODO(arjuns): Move it to a better location.
  public static void
      updateChangeLog(ImportManager importManager, ImportJobEntity entity, String changeMessage) {
    ChangeLogEntryPojo changeLog = new ChangeLogEntryPojo(changeMessage);
    importManager.put(entity, changeLog);
  }
}
