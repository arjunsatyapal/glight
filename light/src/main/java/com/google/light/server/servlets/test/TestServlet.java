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
package com.google.light.server.servlets.test;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.light.server.utils.GaeUtils;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class TestServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    boolean foo = GaeUtils.isUserAdmin();
    response.getWriter().println(foo);
//    try {
//      String randomFileName = "abc.zip";
//
//      GSFileOptions gsFileOptions = GoogleCloudStorageUtils.getGCSFileOptionsForCreate(
//          GoogleCloudStorageBuckets.CONTENT, ContentTypeEnum.APPLICATION_ZIP, randomFileName);
//
//      FileService fileService = FileServiceFactory.getFileService();
//      AppEngineFile appengineFile = fileService.createNewGSFile(gsFileOptions);
//
//      System.out.println("Full path = " + appengineFile.getFullPath());
//      System.out.println("Name = " + appengineFile.getNamePart());
//      System.out.println("FileSystem = " + appengineFile.getFileSystem());
//      FileWriteChannel writeChannel = fileService.openWriteChannel(appengineFile, true /* lock */);
//
//      // Different standard Java ways of writing to the channel
//      // are possible. Here we use a PrintWriter:
//      PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
//      out.println("The woods are lovely dark and deep.");
//      out.println("But I have promises to keep.");
//      // Close without finalizing and save the file path for writing later
//      out.close();
//      writeChannel.closeFinally();
//
//      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
//      String absPath = getAbsolutePathOnBucket(
//          GoogleCloudStorageBuckets.CONTENT, appengineFile.getNamePart());
//      
//      if (LightEnvEnum.getLightEnv() == LightEnvEnum.DEV_SERVER) {
//        absPath = "/gs/light-dev_server-content-bucket/abc.zip";
//      } else {
//        absPath = "/gs/light-qa-content-bucket/abc.zip";
//      }
//      
//      
//      // System.out.println("abs path = " + absPath);
//      BlobKey blobKey = blobstoreService.createGsBlobKey(absPath);
//
//      BlobInfo info = null;
//      if (LightEnvEnum.getLightEnv() == LightEnvEnum.DEV_SERVER) {
//        info = loadGsFileInfo(blobKey);
//      } else {
//        BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
//        info = blobInfoFactory.loadBlobInfo(blobKey);
//      }
//      System.out.println("BlobKey = " + blobKey);
//      Preconditions.checkNotNull(info, "info is null");
//
//      //
//      // BlobInfoFactory factory = new BlobInfoFactory();
//      // Iterator<BlobInfo> it = factory.queryBlobInfos();
//
//      BlobInfo foo = loadGsFileInfo(blobKey);
//      System.out.println(foo.getFilename());
//
//      // System.out.println(info.getFilename());
//
//    } catch (Exception e) {
//      throw new RuntimeException(e);
//    }
  }

  public BlobInfo loadGsFileInfo(BlobKey blobKey) {
    try {
      Key key = KeyFactory.createKey(null, "__GsFileInfo__",
          blobKey.getKeyString());
      try {
        Entity entity = DatastoreServiceFactory.getDatastoreService().get(key);
        return new BlobInfoFactory().createBlobInfo(entity);
      } catch (EntityNotFoundException ex) {
        return null;
      }
    } finally {
    }
  }
}
