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
package com.google.light.server.thirdparty.clients.google.youtube;

import static com.google.light.server.utils.LightUtils.getURL;

import com.google.light.server.dto.thirdparty.google.youtube.YouTubePlaylistInfo;

import com.google.light.server.dto.thirdparty.google.youtube.YouTubeVideoInfo;
import com.google.light.server.servlets.thirdparty.google.youtube.YouTubeServiceWrapper;
import com.google.light.server.urls.YouTubeUrl;
import org.junit.Test;

/**
 * Test for {@link YouTubeServiceWrapper}
 * 
 * @author Arjun Satyapal
 */
public class YouTubeServiceWrapperITCase {
  private YouTubeUrl ytUrl =
      new YouTubeUrl(
          getURL(
          "http://www.youtube.com/watch?v=iytllF9MHko&list=PL1FB965FD592C00C1&index=6&feature=plpp_video"));
  private YouTubeServiceWrapper youTubeService = new YouTubeServiceWrapper();

  @Test
  public void test_youtubeVideo() throws Exception {
    YouTubeVideoInfo ytInfo = youTubeService.getYouTubeVideoInfo(ytUrl);
    System.out.println(ytInfo.toJson());
  }
  
  @Test
  public void test_youTubePlaylistAsCourse() throws Exception {
    String url = "http://www.youtube.com/course?list=PL546CD09EA2399DAB&category_name=University&feature=edu";
    YouTubeUrl ytUrl = new YouTubeUrl(getURL(url));
    YouTubePlaylistInfo playlistInfo = youTubeService.getYouTubePlayListDetailedInfo(ytUrl);
    System.out.println(playlistInfo.toJson());
  }
  
  @Test
  public void test_youTubePlaylist() throws Exception {
    String url = "http://www.youtube.com/playlist?list=PL273328E3F4C2353A&feature=view_all";
    YouTubeUrl ytUrl = new YouTubeUrl(getURL(url));
    YouTubePlaylistInfo playlistInfo = youTubeService.getYouTubePlayListDetailedInfo(ytUrl);
    System.out.println(playlistInfo.toJson());
    System.out.println(playlistInfo.getListOfVideos().size());
    
  }
}
