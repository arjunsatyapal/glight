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
package com.google.light.server.exception.unchecked.httpexception;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.light.server.constants.http.HttpStatusCodesEnum;

/**
 * Exceptions for covering HTTP Codes.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class LightHttpException extends WebApplicationException {
  private HttpStatusCodesEnum httpCode;

  public LightHttpException(HttpStatusCodesEnum httpCode, String errString) {
    // TODO(waltercacau): Create a Dto for returning errors to the client
    super(Response.status(httpCode.getStatusCode())
        .entity("\"" + StringEscapeUtils.escapeJavaScript(errString) + "\"")
        .type(MediaType.APPLICATION_JSON).build());
    this.httpCode = checkNotNull(httpCode);
  }

  public HttpStatusCodesEnum getHttpCode() {
    return httpCode;
  }
}
