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
package com.google.light.server.exception.unchecked;

/**
 * This indicates that in order to perform the requested task, user has to login.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class PersonLoginRequiredException extends LightRuntimeException {
  /**
   * {@inheritDoc}
   */
  public PersonLoginRequiredException() {
      super();
  }

  /**
   * {@inheritDoc}
   * @param message
   */
  public PersonLoginRequiredException(String message) {
      super(message);
  }

  /**
   * {@inheritDoc}
   */
  public PersonLoginRequiredException(String message, Throwable cause) {
      super(message, cause);
  }

  /**
   * {@inheritDoc}
   */
  public PersonLoginRequiredException(Throwable cause) {
      super(cause);
  }
}
