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

import static org.junit.Assert.*;
import com.google.common.collect.Lists;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.utils.LightUtils;
import java.util.List;
import org.junit.Test;

/**
 * Test for {@link YouTubeUrl}
 * 
 * @author Arjun Satyapal
 */
public class YouTubeUrlTest {
  private String YOUTUBE_VIDEO_IN_PLAYLIST = "http://www.youtube.com/watch?v=AlPhA&list=dummyList1234&index=1&feature=plpp_video";
  @Test
  public void test_youtubeUrl() {
    List<String> urls = Lists.newArrayList(
        "http://www.youtube.com/watch?v=AlPhA",
        "http://www.youtube.com/watch?v=AlPhA&feature=gamma",
        YOUTUBE_VIDEO_IN_PLAYLIST);
    
//    http://www.youtube.com/v/CCzXXy4L-DQ?version=3&amp;f=playlists&amp;c=light&amp;app=youtube_gdata

    for (String curr : urls) {
      doTest(curr, "AlPhA");
    }
  }

  /**
   * @param curr
   * @param string
   */
  private void doTest(String url, String expectedKey) {
    String failureMsg = "Failed for : " + url;
    YouTubeUrl ytUrl = new YouTubeUrl(LightUtils.getURL(url));
    assertFalse(failureMsg, ytUrl.isPlaylist());
    assertEquals(failureMsg, expectedKey, ytUrl.getVideoId());
    assertEquals(failureMsg, ModuleType.YOU_TUBE_VIDEO, ytUrl.getModuleType());
  }
  
  @Test
  public void test_videoInplaylist() {
    YouTubeUrl ytUrl = new YouTubeUrl(LightUtils.getURL(YOUTUBE_VIDEO_IN_PLAYLIST));
    assertNotNull(ytUrl.getPlaylistId());
    assertEquals("dummyList1234", ytUrl.getPlaylistId());
  }
  
  @Test
  public void test_playlist() {
    List<String> urls = Lists.newArrayList(
        "http://www.youtube.com/course?list=PLdummyList1234&category_name=University&feature=edu",
        "http://www.youtube.com/playlist?list=PLdummyList1234&category_name=University&feature=edu",
        "http://www.youtube.com/view_play_list?p=dummyList1234"
        );
    for (String curr : urls) {
      YouTubeUrl ytUrl = new YouTubeUrl(LightUtils.getURL(curr));
      assertTrue(ytUrl.isPlaylist());
      assertEquals("Failed for " + curr, "dummyList1234", ytUrl.getPlaylistId());
    }
  }
}
