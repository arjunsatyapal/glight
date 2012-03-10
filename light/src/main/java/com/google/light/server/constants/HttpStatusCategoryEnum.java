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
package com.google.light.server.constants;

import static com.google.common.base.Preconditions.checkArgument;


/**
 * General Category for Http Statues.
 * 
 * This Enum list is sorted by the range of Status Category.
 * e.g. Informational category is 1xx and Successful is 2xx.
 * So Informational will come before Successful.
 * 
 * Source : http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 * 
 * @author Arjun Satyapal
 */
enum HttpStatusCategoryEnum {
  INFORMATIONAL(100, 199),
  SUCCESS(200, 299),
  REDIRECTION(300, 399),
  CLIENT_ERROR(400, 499),
  SERVER_ERROR(500, 599);
  
  private int min;
  private int max;
  
  private HttpStatusCategoryEnum(int min, int max) {
    checkArgument(min >= 100 && min < 600);
    this.min = min;
    
    checkArgument(max > min && max < 600);
    this.max = max;
  }
  
  public int getMin() {
    return min;
  }
  
  public int getMax() {
    return max;
  }
}
