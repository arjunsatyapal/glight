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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.exception.ExceptionType;

/**
 * Exception thrown by {@link LightPreconditions#checkNotBlank(String)} when 
 * <br> String is null.
 * <br> String is empty
 * <br> String consists only of whitespaces.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class BlankStringException extends RuntimeException {
  private ExceptionType type;
  
  /**
   * {@inheritDoc}
   * @param message
   */
  public BlankStringException(String message, ExceptionType type) {
      super(message);
      this.type = checkNotNull(type);
  }
  
  public ExceptionType getType() {
    return type;
  }
}
