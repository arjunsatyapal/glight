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
package com.google.light.server.exception.unchecked.taskqueue;





/**
 * This happens when incorrect KeyType is passed. e.g. Instead of String Id, a Long Id was passed.
 * Most probably this is going to be a server side issue.
 *  
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class GoogleDocArchivalWaitingException extends TaskQueueRetriableException {
  /**
   * {@inheritDoc}
   */
  public GoogleDocArchivalWaitingException() {
      super();
  }

  /**
   * {@inheritDoc}
   * @param message
   */
  public GoogleDocArchivalWaitingException(String message) {
      super(message);
  }

  /**
   * {@inheritDoc}
   */
  public GoogleDocArchivalWaitingException(String message, Throwable cause) {
      super(message, cause);
  }

  /**
   * {@inheritDoc}
   */
  public GoogleDocArchivalWaitingException(Throwable cause) {
      super(cause);
  }
}
