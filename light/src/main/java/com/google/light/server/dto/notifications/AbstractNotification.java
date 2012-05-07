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
package com.google.light.server.dto.notifications;

import com.google.common.base.Preconditions;
import com.google.light.server.constants.NotificationType;
import com.google.light.server.dto.AbstractDto;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public abstract class AbstractNotification<T> extends AbstractDto<T> {
  private NotificationType type;

  @SuppressWarnings("unchecked")
  @Override
  public T validate() {
    Preconditions.checkNotNull(type, "type");
    return ((T) this);
  }
  
  public NotificationType getType() {
    return type;
  }

  @SuppressWarnings("rawtypes")
  public static class Builder<T extends Builder> extends AbstractDto.BaseBuilder<T> {
    private NotificationType type;

    @SuppressWarnings("unchecked")
    public T type(NotificationType type) {
      this.type = type;
      return ((T) this);
    }
  }

  @SuppressWarnings({ "rawtypes", "synthetic-access" })
  protected AbstractNotification(Builder builder) {
    super(builder);
    
    if (builder == null) {
      return;
    }
    
    this.type = builder.type;
  }
}
