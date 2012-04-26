/*
 * Copyright (C) Google Inc.
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
package com.google.light.server.constants.http;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.http.HttpStatusCategoryEnum.CLIENT_ERROR;
import static com.google.light.server.constants.http.HttpStatusCategoryEnum.INFORMATIONAL;
import static com.google.light.server.constants.http.HttpStatusCategoryEnum.REDIRECTION;
import static com.google.light.server.constants.http.HttpStatusCategoryEnum.SERVER_ERROR;
import static com.google.light.server.constants.http.HttpStatusCategoryEnum.SUCCESS;

/**
 * Enum to wrap Http Codes.
 * 
 * Usually Enums are sorted in Alphabetical order, but here we will be sorting on the basis 
 * of the Code.
 * 
 * Source : http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 * 
 * @author Arjun Satyapal
 */
public enum HttpStatusCodesEnum {
  CONTINUE(100, INFORMATIONAL),
  SWITCHING_PROTOCOLS(101, INFORMATIONAL),
  OK(200, SUCCESS),
  CREATED(201, SUCCESS),
  ACCEPTED(202, SUCCESS),
  NON_AUTHORITATIVE_INFORMATION(203, SUCCESS),
  NO_CONTENT(204, SUCCESS),
  RESET_CONTENT(205, SUCCESS),
  PARTIAL_CONTENT(206, SUCCESS),
  MULTIPLE_CHOICES(300, REDIRECTION),
  MOVED_PERMANENTLY(301, REDIRECTION),
  FOUND(302, REDIRECTION),
  SEE_OTHER(303, REDIRECTION),
  NOT_MODIFIED(304, REDIRECTION),
  USE_PROXY(305, REDIRECTION),
  UNUSED_REDIRECTION(306, REDIRECTION),
  TEMPORARY_REDIRECT(307, REDIRECTION),
  BAD_REQUEST(400, CLIENT_ERROR),
  UNAUTHORIZED(401, CLIENT_ERROR),
  PAYMENT_REQUIRED(402, CLIENT_ERROR),
  FORBIDDEN(403, CLIENT_ERROR),
  NOT_FOUND(404, CLIENT_ERROR),
  METHOD_NOT_ALLOWED(405, CLIENT_ERROR),
  NOT_ACCEPTABLE(406, CLIENT_ERROR),
  PROXY_AUTHENTICATION_REQUIRED(407, CLIENT_ERROR),
  REQUEST_TIMEOUT(408, CLIENT_ERROR),
  CONFLICT(409, CLIENT_ERROR),
  GONE(410, CLIENT_ERROR),
  LENGTH_REQUIRED(411, CLIENT_ERROR),
  PRECONDITIONS_FAILED(411, CLIENT_ERROR),
  REQUEST_ENTITY_TOO_LARGE(413, CLIENT_ERROR),
  REQUEST_URI_TOO_LONG(414, CLIENT_ERROR),
  UNSUPPORTED_MEDIA_TYPE(415, CLIENT_ERROR),
  REQUESTED_RANGE_NOT_SATISFIABLE(416, CLIENT_ERROR),
  EXPECTATION_FAILED(417, CLIENT_ERROR),
  INTERNAL_SERVER_ERROR(500, SERVER_ERROR),
  NOT_IMPLEMENTED(501, SERVER_ERROR),
  BAD_GATEWAY(502, SERVER_ERROR),
  SERVICE_UNAVAILABLE(503, SERVER_ERROR),
  GATEWAY_TIMEOUT(504, SERVER_ERROR),
  HTTP_VERSION_NOT_SUPPORTED(505, SERVER_ERROR);
  
  private int statusCode;
  private HttpStatusCategoryEnum category;
  
  private HttpStatusCodesEnum(int code, HttpStatusCategoryEnum category) {
    this.statusCode = code;
    this.category = checkNotNull(category);
  }
  
  public int getStatusCode() {
    return statusCode;
  }
  
  public HttpStatusCategoryEnum getCategory() {
    return category;
  }
}
