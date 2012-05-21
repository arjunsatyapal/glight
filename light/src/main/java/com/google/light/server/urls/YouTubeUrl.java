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
package com.google.light.server.urls;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.dto.module.ModuleType;
import java.net.URL;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class YouTubeUrl implements ExternalIdUrlInterface {
  private String videoKey;
  
  public YouTubeUrl(URL url) {
    init(url);
  }

  /**
   * @param url
   */
  private void init(URL url) {
    String query = url.getQuery();
    String videoKey = null;
    if (query.contains("&")) {
      String[] parts = query.split("&");
      for (String currPart : parts) {
        if (currPart.startsWith("v=")) {
          videoKey = extractVideoKey(currPart, url);
        }
      }
    } else if (query.startsWith("v=")) {
      videoKey = extractVideoKey(query, url);
    }
    
    this.videoKey = checkNotBlank(videoKey, "Invalid url : " + url.toString());
    
  }

  /**
   * @param query
   * @return
   */
  private String extractVideoKey(String videoKeyValue, URL url) {
    String errMsg = "Invalid YouTube URL : " + url.toString();
    checkArgument(videoKeyValue.startsWith("v="), errMsg);
    String parts[] = videoKeyValue.split("=");
    checkArgument(parts.length == 2, errMsg);
    return parts[1];
    
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public ModuleType getModuleType() {
    return ModuleType.YOU_TUBE_VIDEO;
  }
  
  public String getVideoKey() {
    return videoKey;
  }
}
