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
package com.google.light.server.constants.http;

/**
 * Used in Jersey to set the output content type.
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class ContentTypeConstants {

  public static final String APPLICATION_JSON = "application/json; charset=UTF-8";
  public static final String APPLICATION_XML = "application/xml; charset=UTF-8";
  public static final String APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";

  public static final String TEXT_HTML = "text/html; charset=UTF-8";
  public static final String TEXT_PLAIN = "text/plain; charset=UTF-8";

}
