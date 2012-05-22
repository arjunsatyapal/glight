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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.gdata.data.youtube.PlaylistEntry;

import com.google.common.collect.Lists;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.exception.unchecked.YouTubeException;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.XmlUtils;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class YouTubeUtils {
  private static final String VIDEO_FEED_BASE_URL = "http://gdata.youtube.com/feeds/api/videos/";

  private static final String PLAYLIST_FEED_BASE_URL =
      "http://gdata.youtube.com/feeds/api/playlists";

  private static final String PLAYLIST_SNIPPET = PLAYLIST_FEED_BASE_URL + "/snippets";
  
  /**
   * This returns a GDATA URL which can be used to fetch details of a Video.
   * e.g. http://gdata.youtube.com/feeds/api/videos/<videoId>
   * 
   * @param videoId
   * @return
   */
  public static URL getVideoEntryUrl(String videoId) {
    return LightUtils.getURL(VIDEO_FEED_BASE_URL + videoId);
  }

  /**
   * This returns a GDATA URL which can be used to fetch a Playlist Feed.
   * e.g. http://gdata.youtube.com/feeds/api/playlists/<playlistId> <br>
   * Note that the URLs that are given by YouTube
   * (e.g. http://www.youtube.com/playlist?list=PL1FB965FD592C00C1), contains PL as part of the
   * PlaylistId. But in APIs you are expected to use without PL. i.e. in above case, PlaylistId to
   * be used is 1FB965FD592C00C1.
   * 
   * @param playlist
   * @return
   */
  public static URL getPlaylistFeedUrl(String playlistId, int maxResults) {
    return LightUtils.getURL(PLAYLIST_FEED_BASE_URL + "/" + playlistId + "?max-results=" + maxResults);
  }

  /**
   * This returns a GDATA URL which can be used to fetch a Playlist Snippet. Playlist Feed does not
   * containt details for Playlist. Not sure why.
   * 
   * e.g. http://gdata.youtube.com/feeds/api/playlists/snippets/<playlistId> <br>
   * Note that the URLs that are given by YouTube
   * (e.g. http://www.youtube.com/playlist?list=PL1FB965FD592C00C1), contains PL as part of the
   * PlaylistId. But in APIs you are expected to use without PL. i.e. in above case, PlaylistId to
   * be used is 1FB965FD592C00C1.
   * 
   * @param playlist
   * @return
   */
  public static URL getPlaylistSnippetUrl(String playlistId) {
    return LightUtils.getURL(PLAYLIST_SNIPPET + "/" + playlistId);

  }

  public static URL getPlayListSnippetUrl(String query, String startIndex, int maxResults) {
    StringBuilder queryParams = new StringBuilder("q=").append(query);

    if (StringUtils.isNotBlank(startIndex)) {
      queryParams.append("&start-index=").append(startIndex);
    }

    queryParams.append("&max-results=").append(maxResults);

    return LightUtils.getURL(PLAYLIST_SNIPPET + "?" + queryParams.toString());
  }

  public static List<ContentLicense> getContentLicenseFromVideoEntry(VideoEntry videoEntry) {
    try {
      String xml = XmlUtils.getXmlEntry(videoEntry);
      SAXBuilder builder = new SAXBuilder();
      Document document = builder.build(new StringReader(xml));
      Element root = document.getRootElement();

      Namespace mediaGroupNS = Namespace.getNamespace("http://search.yahoo.com/mrss/");
      Element mediaGroupElement = root.getChild("group", mediaGroupNS);
      checkNotNull(mediaGroupElement, "group was not found");

      Element license = mediaGroupElement.getChild("license", mediaGroupNS);
      checkNotNull(license, "license was not found");

      ContentLicense contentLicense = ContentLicense.getLicenseByIdentifier(license.getText());
      return Lists.newArrayList(contentLicense);
    } catch (Exception e) {
      throw new YouTubeException(e);
    }
  }

  public static String getPlaylistIdFromPlaylistEntry(PlaylistEntry playlistEntry) {
    try {
      String xml = XmlUtils.getXmlEntry(playlistEntry);
      SAXBuilder builder = new SAXBuilder();
      Document document = builder.build(new StringReader(xml));
      Element root = document.getRootElement();

      Namespace youTubeNS = Namespace.getNamespace("http://gdata.youtube.com/schemas/2007");
      Element playlistIdElement = root.getChild("playlistId", youTubeNS);
      checkNotNull(playlistIdElement, "group was not found");
      return playlistIdElement.getValue();
    } catch (Exception e) {
      throw new YouTubeException(e);
    }
  }
}
