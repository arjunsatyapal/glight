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
package com.google.light.server.constants;

import com.google.common.collect.Lists;

import java.util.List;

import java.io.File;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class asdfasdf {
  public static void main(final java.lang.String[] args) throws Exception {
    String list = System.getProperty("java.class.path");

    List<String> listOfFiles = Lists.newArrayList();
    for (final String path : list.split(":")) {
      if (path.contains("light")) {
        File object = new File(path);
        
        addFilesToList(object.getParentFile().getParentFile(), listOfFiles);
      }
    }

    for (String curr : listOfFiles) {
      if (curr.contains("webapp") && curr.endsWith(".html"))
        System.out.println(curr);
    }
  }

  public static void addFilesToList(File self, List<String> files) {
    if (self.isDirectory()) {
      
      for (String child : self.list()) {
        File childFile = new File(self.getAbsolutePath() + "/" + child);

        if (childFile.isDirectory()) {
          addFilesToList(childFile, files);
        } else {
          files.add(childFile.getAbsolutePath());
        }
      }
    } else {
      files.add(self.getAbsolutePath());
    }
  }
}
