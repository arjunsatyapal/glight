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
package com.google.light.server.utils;

import static com.google.light.server.utils.LightUtils.getRandomFileName;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;

import com.google.light.server.constants.FileExtensions;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.GSFileOptions;
import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;
import com.google.common.io.ByteStreams;
import com.google.light.server.constants.google.cloudstorage.GoogleCloudStorageBuckets;
import com.google.light.server.constants.http.ContentTypeEnum;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import org.joda.time.DateTime;
import org.joda.time.Instant;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class GoogleCloudStorageUtils {
  public static GSFileOptions getGCSFileOptionsForCreate(GoogleCloudStorageBuckets bucket,
      ContentTypeEnum contentType, String relativeFilePath) {
    GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
        .setBucket(bucket.getBucketName())
        .setMimeType(contentType.get())
        .setKey(relativeFilePath);

    return optionsBuilder.build();
  }

  public static String getAbsolutePathOnBucket(GoogleCloudStorageBuckets bucket,
      String relativeFilePath) {
    return bucket.getAbsoluteFilePath(relativeFilePath);
  }

  public static String getAbsoluteModuleHtmlPath(GoogleCloudStorageBuckets bucket,
      ModuleId moduleId) {

    return bucket.getAbsoluteFilePath("" + moduleId.getValue() + "/" + moduleId.getValue()
        + ".html");
  }

  public static String writeFileOnGCS(InputStream inputStream, GSFileOptions gsFileOptions)
      throws IllegalStateException, IOException {
    FileService fileService = FileServiceFactory.getFileService();
    AppEngineFile appengineFile = fileService.createNewGSFile(gsFileOptions);

    FileWriteChannel writeChannel = fileService.openWriteChannel(appengineFile, true /* lock */);
    OutputStream outputStream = Channels.newOutputStream(writeChannel);
    ByteStreams.copy(inputStream, outputStream);
    outputStream.close();
    writeChannel.closeFinally();

    return appengineFile.getFullPath();
  }

  public static String getFolderForToday() {
    Instant now = LightUtils.getNow();

    DateTime dateTime = new DateTime(now);

    return dateTime.getYear() + "/" + dateTime.getMonthOfYear() + "/" + dateTime.getDayOfMonth();
  }

  public static String getDestinationFileNameForGDoc() {
    return getFolderForToday() + "/" + "google_doc_archive/"
        + getRandomFileName(FileExtensions.ZIP);
  }

  // Utility class.
  private GoogleCloudStorageUtils() {
  }
}
