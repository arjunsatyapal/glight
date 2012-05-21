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

import static org.junit.Assert.assertEquals;

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
  @Test
  public void test_youtubeUrl() {
    List<String> urls = Lists.newArrayList(
        "http://www.youtube.com/watch?v=AlPhA",
        "http://www.youtube.com/watch?v=AlPhA&feature=gamma");

    for (String curr : urls) {
      doTest(curr, "AlPhA");
    }
  }

  /**
   * @param curr
   * @param string
   */
  private void doTest(String url, String expectedKey) {
    YouTubeUrl ytUrl = new YouTubeUrl(LightUtils.getURL(url));
    assertEquals(expectedKey, ytUrl.getVideoKey());
    assertEquals(ModuleType.YOU_TUBE_VIDEO, ytUrl.getModuleType());
  }
}
