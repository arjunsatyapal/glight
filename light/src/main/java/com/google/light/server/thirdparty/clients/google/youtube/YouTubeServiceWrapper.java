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

import static com.google.light.server.constants.LightStringConstants.LIGHT_APPLICATION_NAME;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.data.geo.impl.GeoRssWhere;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.media.mediarss.MediaPlayer;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YouTubeMediaRating;
import com.google.gdata.data.youtube.YtPublicationState;
import com.google.gdata.data.youtube.YtStatistics;

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
  public YouTubeServiceWrapper() {
    super(LIGHT_APPLICATION_NAME);
  }
  
  public static void printVideoEntry(VideoEntry videoEntry, boolean detailed) {
    System.out.println("Title: " + videoEntry.getTitle().getPlainText());

    if(videoEntry.isDraft()) {
      System.out.println("Video is not live");
      YtPublicationState pubState = videoEntry.getPublicationState();
      if(pubState.getState() == YtPublicationState.State.PROCESSING) {
        System.out.println("Video is still being processed.");
      }
      else if(pubState.getState() == YtPublicationState.State.REJECTED) {
        System.out.print("Video has been rejected because: ");
        System.out.println(pubState.getDescription());
        System.out.print("For help visit: ");
        System.out.println(pubState.getHelpUrl());
      }
      else if(pubState.getState() == YtPublicationState.State.FAILED) {
        System.out.print("Video failed uploading because: ");
        System.out.println(pubState.getDescription());
        System.out.print("For help visit: ");
        System.out.println(pubState.getHelpUrl());
      }
    }

    if(videoEntry.getEditLink() != null) {
      System.out.println("Video is editable by current user.");
    }

    if(detailed) {

      YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();

      System.out.println("Uploaded by: " + mediaGroup.getUploader());

      System.out.println("Video ID: " + mediaGroup.getVideoId());
      System.out.println("Description: " + 
        mediaGroup.getDescription().getPlainTextContent());

      MediaPlayer mediaPlayer = mediaGroup.getPlayer();
      System.out.println("Web Player URL: " + mediaPlayer.getUrl());
      MediaKeywords keywords = mediaGroup.getKeywords();
      System.out.print("Keywords: ");
      for(String keyword : keywords.getKeywords()) {
        System.out.print(keyword + ",");
      }

      GeoRssWhere location = videoEntry.getGeoCoordinates();
      if(location != null) {
        System.out.println("Latitude: " + location.getLatitude());
        System.out.println("Longitude: " + location.getLongitude());
      }

      Rating rating = videoEntry.getRating();
      if(rating != null) {
        System.out.println("Average rating: " + rating.getAverage());
      }

      YtStatistics stats = videoEntry.getStatistics();
      if(stats != null ) {
        System.out.println("View count: " + stats.getViewCount());
      }
      System.out.println();

      System.out.println("\tThumbnails:");
      for(MediaThumbnail mediaThumbnail : mediaGroup.getThumbnails()) {
        System.out.println("\t\tThumbnail URL: " + mediaThumbnail.getUrl());
        System.out.println("\t\tThumbnail Time Index: " +
        mediaThumbnail.getTime());
        System.out.println();
      }

      System.out.println("\tMedia:");
      for(YouTubeMediaContent mediaContent : mediaGroup.getYouTubeContents()) {
        System.out.println("\t\tMedia Location: "+ mediaContent.getUrl());
        System.out.println("\t\tMedia Type: "+ mediaContent.getType());
        System.out.println("\t\tDuration: " + mediaContent.getDuration());
        System.out.println();
      }

      for(YouTubeMediaRating mediaRating : mediaGroup.getYouTubeRatings()) {
        System.out.println("Video restricted in the following countries: " +
          mediaRating.getCountries().toString());
      }
    }
  }

}
