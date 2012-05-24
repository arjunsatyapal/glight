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
package com.google.light.server.servlets.thirdparty.google.youtube;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.light.server.constants.LightConstants.GDATA_YOU_TUBE_PLAYLIST_MAX_RESULTS;
import static com.google.light.server.constants.LightStringConstants.LIGHT_APPLICATION_NAME;
import static com.google.light.server.servlets.thirdparty.google.youtube.YouTubeUtils.getPlaylistFeedUrl;
import static com.google.light.server.servlets.thirdparty.google.youtube.YouTubeUtils.getPlaylistSnippetUrl;

import com.google.light.server.utils.LightUtils;

import com.google.light.server.dto.thirdparty.google.youtube.YouTubePlaylistInfo;

import com.google.gdata.data.youtube.PlaylistFeed;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.PlaylistEntry;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.inject.Inject;
import com.google.light.server.dto.thirdparty.google.youtube.YouTubeVideoInfo;
import com.google.light.server.exception.unchecked.YouTubeException;
import com.google.light.server.urls.YouTubeUrl;
import java.net.URL;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class YouTubeServiceWrapper extends YouTubeService {
  /**
   * @param applicationName
   */
  @Inject
  public YouTubeServiceWrapper() {
    super("light-demo");
    getRequestFactory().setHeader("GData-Version", "2");
  }

  public YouTubeVideoInfo getYouTubeVideoInfo(YouTubeUrl ytUrl) {
    try {
      URL url = YouTubeUtils.getVideoEntryUrl(ytUrl.getVideoId());
      VideoEntry videoEntry = getEntry(url, VideoEntry.class);
      YouTubeVideoInfo videoInfo = new YouTubeVideoInfo.Builder()
          .withVideoEntry(videoEntry)
          .build();
      return videoInfo;
    } catch (Exception e) {
      throw new YouTubeException(e);
    }
  }

  public YouTubePlaylistInfo getYouTubePlayListInfo(YouTubeUrl ytUrl) {
    checkArgument(ytUrl.isPlaylist(), "This method should be called only for playlists."
        + ytUrl.getExternalId());
    try {
      String playlistId = ytUrl.getPlaylistId();
      URL playlistSnippetUrl = getPlaylistSnippetUrl(playlistId);
      PlaylistEntry playlistEntry = this.getEntry(playlistSnippetUrl, PlaylistEntry.class);
      YouTubePlaylistInfo youTubePlaylistInfo = new YouTubePlaylistInfo.Builder()
          .withPlaylistEntry(playlistEntry)
          .build();
      
      return youTubePlaylistInfo;
    } catch (Exception e) {
      throw new YouTubeException(e);
    }
  }

  public YouTubePlaylistInfo getYouTubePlayListDetailedInfo(YouTubeUrl ytUrl) {
    checkArgument(ytUrl.isPlaylist(), "This method should be called only for playlists."
        + ytUrl.getExternalId());
    try {
      String playlistId = ytUrl.getPlaylistId();
      YouTubePlaylistInfo youTubePlaylistInfo = getYouTubePlayListInfo(ytUrl);

      URL playlistFeedUrl = getPlaylistFeedUrl(playlistId, GDATA_YOU_TUBE_PLAYLIST_MAX_RESULTS);

      PlaylistFeed playlistFeed = null;

      do {
        playlistFeed = this.getFeed(playlistFeedUrl, PlaylistFeed.class);
        for (PlaylistEntry currEntry : playlistFeed.getEntries()) {
          YouTubeVideoInfo youTubeVideoInfo = new YouTubeVideoInfo.Builder()
              .withVideoEntry(currEntry)
              .build();

          youTubePlaylistInfo.addYouTubeVideo(youTubeVideoInfo);
        }

        if (playlistFeed.getNextLink() != null) {
          playlistFeedUrl = LightUtils.getURL(playlistFeed.getNextLink().getHref());
        } else {
          break;
        }

      } while (playlistFeed.getNextLink() != null);

      return youTubePlaylistInfo;
    } catch (Exception e) {
      throw new YouTubeException(e);
    }
  }
}
