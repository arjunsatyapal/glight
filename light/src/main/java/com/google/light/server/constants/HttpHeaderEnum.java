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

import com.google.light.server.utils.LightPreconditions;

/**
 * Enum to encapsulate HTTP Headers.
 * 
 * TODO(arjuns): Add test for this class.
 * @author Arjun Satyapal
 */
public enum HttpHeaderEnum {

  /*
   * HTTP Headers common for Both HTTP Requests Response Headers.
   * Source : http://en.wikipedia.org/wiki/List_of_HTTP_header_fields
   */
  ACCEPT("Accept"),
  ACCEPT_CHARSET("Accept-Charset"),
  ACCEPT_ENCODING("Accept-Encoding"),
  ACCEPT_LANGUAGE("Accept-Language"),
  ACCEPT_DATETIME("Accept-Datetime"),
  ACCEPT_RANGES("Accept-Ranges"),
  AGE("Age"),
  ALLOW("Allow"),
  AUTHORIZATION("Authorization"),
  
  CACHE_CONTROL("Cache-Control"),
  CONNECTION("Connection"),
  COOKIE("Cookie"),
  CONTENT_ENCODING("Content-Encoding"),
  CONTENT_LANGUAGE("Content-Language"),
  CONTENT_LENGTH("Content-Length"),
  CONTENT_LOCATION("Content-Location"),
  CONTENT_MD5("Content-MD5"),
  CONTENT_DISPOSITION("content-disposition"),
  CONTENT_RANGE("Content-Range"),
  CONTENT_TYPE("Content-Type"),

  DATE("Date"),

  ETAG("ETag"),
  EXPECT("Expect"),
  EXPIRES("Expires"),
  
  FROM("From"),
  
  HOST("Host"),
  
  IF_MATCh("If-Match"),
  IF_MODIFIED_SINCE("If-Modified-Since"),
  IF_NONE_MATCH("If-None-Match"),
  IF_RANGE("If-Range"),
  IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
  
  LAST_MODIFIED("Last-Modified"),
  LINK("Link"),
  LOCATION("Location"),
  
  MAX_FORWARDS("Max-Forwards"),

  P3P("P3P"),
  PRAGMA("Pragma"),
  PROXY_AUTHORIZATION("Proxy-Authorization"),
  
  RANGE("Range"),
  REFERER("Referer"),
  REFRESH("Refresh"),
  RETRY_AFTER("Retry-After"),
  
  SERVER("Server"),
  SET_COOKIE("Set-Cookie"),
  STRICT_TRANSPORT_SECURITY("Strict-Transport-Security"),
  
  TE("TE"),
  TRAILER("Trailer"),
  TRANSFER_ENCODING("Transfer-Encoding"),
  
  UPGRADE("Upgrade"),
  USER_AGENT("User-Agent"),
  
  VARY("Vary"),
  VIA("Via"),
  
  WARNING("Warning"),
  WWW_AUTHENTICATE("WWW-Authenticate"),

  // Headers set by GAE
  GAE_CITY("X-AppEngine-City"),
  GAE_CITY_LAT_LONG("X-Appengine-CityLatLong"),
  GAE_QUEUE_NAME("X-AppEngine-QueueName"),
  GAE_REGION("X-AppEngine-Region"),
  GAE_TASK_NAME("X-AppEngine-TaskName"),
  GAE_TASK_RETRY_COUNT("X-AppEngine-TaskRetryCount"),
  GAE_FAILFAST("X-AppEngine-FailFast"),
  GAE_TASK_ETA("X-AppEngine-TaskETA"),

// TODO(arjuns) : See what this does.

  LIGHT_ACTOR_HEADER("light-actor");

  private String name;

  private HttpHeaderEnum(String name) {
    this.name = LightPreconditions.checkNotBlank(name, "name");
  }

  public String get() {
    return name;
  }
}
