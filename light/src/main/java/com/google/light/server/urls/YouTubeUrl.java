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
import static com.google.light.server.utils.LightUtils.getParamValueFromQueryString;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.utils.LightUtils;
import java.net.URI;
import java.net.URL;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class YouTubeUrl implements ExternalIdUrlInterface {
  private static final String VIDEO_KEY = "v";
  private static final String VIDEO_KEY_ID = VIDEO_KEY + "=";
  
  private static final String PLAYLIST_KEY = "list";
  private static final String PLAYLIST_KEY_ID = PLAYLIST_KEY + "=";
  
  private static final String PLAYLIST_VIEW_KEY = "p";
  private static final String PLAYLIST_VIEW_KEY_ID = PLAYLIST_VIEW_KEY + "=";
  private static final String PLAYLIST_ID_PREFIX = "PL";

  private String videoId;
  private String playlistId;
  private boolean isPlayList;

  public YouTubeUrl(URL url) {
    init(url);
  }

  public YouTubeUrl(ExternalId externalId) {
    this(externalId.getURL());
  }

  public boolean isPlaylist() {
    return isPlayList;
  }

  /**
   * This will return the unique value for a video no matter what feature, partnership, playlist
   * it belongs to.
   * 
   * @return
   */
  public ExternalId getExternalId() {
    return new ExternalId(getNormalizedHref());
  }

  public String getNormalizedHref() {
    if (isPlayList) {
      checkNotBlank(playlistId, "playlistId");
      return "http://www.youtube.com/playlist?list=" + PLAYLIST_ID_PREFIX + playlistId;
    } else {
      checkNotBlank(videoId, "videoId");
      return "http://www.youtube.com/watch?v=" + videoId;
    }

  }

  public URI getEmbedUri() {
    return LightUtils.getURI("http://www.youtube.com/embed/" + videoId);
  }

  @SuppressWarnings("cast")
  public URL getUrl() {
    return LightUtils.getURL((String) getWrapperValue(getExternalId()));
  }

  /**
   * @param url
   */
  private void init(URL url) {
    String query = url.getQuery();

    if (url.getFile().startsWith("/view_play_list")) {
      checkArgument(query.contains(PLAYLIST_VIEW_KEY_ID), "PLAYLIST_VIEW_KEY_ID Key is missing");
      checkArgument(!query.contains("&"), "At present no query parameters are supported.");
      setPlaylistId(getParamValueFromQueryString(url, PLAYLIST_VIEW_KEY), url);
      isPlayList = true;
    } else if (url.getFile().startsWith("/playlist") 
        || url.getFile().startsWith("/course")) {
      checkArgument(query.contains(PLAYLIST_KEY_ID), "PLAYLIST_KEY_ID is missing");
      setPlaylistId(getParamValueFromQueryString(url, PLAYLIST_KEY), url);
      isPlayList = true;
    } else if (url.getFile().startsWith("/watch")) {
      isPlayList = false;
      if (query.contains(VIDEO_KEY_ID)) {
        videoId = getParamValueFromQueryString(url, VIDEO_KEY);
      }
      
      if (query.contains(PLAYLIST_KEY_ID)) {
        setPlaylistId(getParamValueFromQueryString(url, PLAYLIST_KEY), url);
      }
    } else {
      throw new IllegalArgumentException("Invalid YouTube URL : " + url.toString());
    }

    if (isPlayList) {
      checkNotBlank(playlistId, "playlistId");
    } else {
      checkNotBlank(videoId, "videoId");
    }
  }

//  /**
//   * @param query
//   * @return
//   */
//  private String extractVideoId(String videoKeyValue, URL url) {
//    String errMsg = "Invalid YouTube URL : " + url.toString();
//    checkArgument(videoKeyValue.startsWith(VIDEO_KEY_ID), errMsg);
//    String parts[] = videoKeyValue.split("=");
//    checkArgument(parts.length == 2, errMsg);
//    return parts[1];
//  }
//
//  private String extractPlaylist(String playlistKeyValue, URL url) {
//    String errMsg = "Invalid YouTube URL : " + url.toString();
//    checkArgument(playlistKeyValue.startsWith(PLAYLIST_KEY_ID), errMsg);
//    String parts[] = playlistKeyValue.split("=");
//    checkArgument(parts.length == 2, errMsg);
//    return parts[1];
//  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleType getModuleType() {
    if (isPlaylist()) {
      return ModuleType.YOU_TUBE_PLAYLIST;
    } else {
      return ModuleType.YOU_TUBE_VIDEO;
    }
  }

  public String getVideoId() {
    return videoId;
  }

  public String getPlaylistId() {
    checkArgument(!playlistId.startsWith(PLAYLIST_ID_PREFIX), 
        "PlayListId at rest should not be prefixed with " + PLAYLIST_ID_PREFIX);
    return playlistId;
  }
  
  public void setPlaylistId(String playlistId, URL url) {
    checkNotBlank(playlistId, "Failed for : " + url);
    
    if (playlistId.startsWith(PLAYLIST_ID_PREFIX)) {
      this.playlistId = playlistId.substring(PLAYLIST_ID_PREFIX.length());
    } else {
      this.playlistId = playlistId;
    }
  }
}
