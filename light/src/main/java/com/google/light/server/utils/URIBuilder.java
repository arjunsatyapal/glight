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
package com.google.light.server.utils;

import com.google.common.base.Preconditions;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import java.net.URI;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class URIBuilder {
  private String scheme = "";
  private String server = "";
  private String port = "";
  private String uri = "";
  
  public URIBuilder() {
  }
  
  public URIBuilder(HttpServletRequest request) {
    Preconditions.checkNotNull(request, "request");
    this.scheme = request.getScheme();
    this.server = request.getServerName();
    
    if (GaeUtils.isDevServer()) {
      this.port = "" + request.getServerPort();
    }
    
    this.uri = request.getRequestURI();
  }
  
  public URIBuilder withScheme(String scheme) {
    this.scheme = scheme;
    return this;
  }
  
  public URIBuilder withServer(String server) {
    if (server.endsWith("/")) {
      this.server = server.substring(0, server.length() - 1);
    } else {
      this.server = server;
    }
    return this;
  }
  
  public URIBuilder withPort(String port) {
    this.port = port;
    return this;
  }
  
  public URIBuilder withUri(String uri) {
    this.uri = uri;
    return this;
  }
  
  public URIBuilder append(String path) {
    if (uri == null) {
      uri = "";
    }

    if (!uri.endsWith("/")) {
      uri = uri + "/";
    }
    
    if (path.startsWith("/")) {
      uri = uri + path.substring(1);
    } else { 
      uri = uri + path;
    }
    
    return this;
  }
  
  public URI build() {
    StringBuilder builder = new StringBuilder();
    
    if (StringUtils.isNotBlank(scheme)) {
      builder.append(scheme).append("://");
    }
    
    builder.append(server);
    
    if (StringUtils.isNotBlank(port)) {
      builder.append(":").append(port);
    }
    
    builder.append("/");
    
    if (uri.startsWith("/")) {
      builder.append(uri.substring(1));
    } else {
      builder.append(uri);
    }
    return LightUtils.getURI(builder.toString());
  }

}
