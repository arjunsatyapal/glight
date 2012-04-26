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
package com.google.light.server.dto.pojo;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.appengine.api.datastore.Text;
import com.google.light.server.dto.AbstractPojo;
import com.google.light.server.utils.LightUtils;
import org.joda.time.DateTime;
import org.joda.time.Instant;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class ChangeLogEntryPojo extends AbstractPojo<ChangeLogEntryPojo>{
  private Instant instant;
  private Text detail;

  public Instant getInstant() {
    return instant;
  }

  public String getDetail() {
    return detail.getValue();
  }
  
  // TODO(arjuns): Support locale.
  // TODO(arjuns) : Add formatter.
  public String toLineItem() {
    DateTime dateTime = new DateTime(instant);
    return dateTime.toString() + " : " + getDetail(); 
  }
  
  public ChangeLogEntryPojo(String detail) {
    this(LightUtils.getNow(), new Text(detail));
  }
  
  public ChangeLogEntryPojo(Instant instant, Text detail) {
    this.instant = instant;
    this.detail = detail;
    validate();
  }
  
  // For Objectify.
  @SuppressWarnings("unused")
  private ChangeLogEntryPojo() {
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public ChangeLogEntryPojo validate() {
    checkNotNull(instant, "instant");
    checkNotNull(detail, "detail");
    return this;
  }
}
