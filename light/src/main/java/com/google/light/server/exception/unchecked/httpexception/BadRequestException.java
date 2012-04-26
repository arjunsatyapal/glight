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

import com.google.light.server.constants.http.HttpStatusCodesEnum;

/**
 * HTTP Bad Request Exception.
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
public class BadRequestException extends LightHttpException {
  public BadRequestException(String errString) {
    super(HttpStatusCodesEnum.BAD_REQUEST, errString);
  }
}
