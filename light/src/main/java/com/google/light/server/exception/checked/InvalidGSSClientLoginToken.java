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
package com.google.light.server.exception.checked;

@SuppressWarnings("serial")
/**
 * Exception thrown when the GSS Client Login Token happens to be invalid
 * when being validated.
 * 
 * @author Walter Cacau
 */
public class InvalidGSSClientLoginToken extends LightException {

  public InvalidGSSClientLoginToken() {
    super();
  }

  public InvalidGSSClientLoginToken(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidGSSClientLoginToken(String message) {
    super(message);
  }

  public InvalidGSSClientLoginToken(Throwable cause) {
    super(cause);
  }

}