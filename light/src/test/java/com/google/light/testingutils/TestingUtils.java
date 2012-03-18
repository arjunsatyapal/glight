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
package com.google.light.testingutils;

import com.google.light.server.utils.LightUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author Arjun Satyapal
 */
public class TestingUtils {
  private static AtomicLong atomicLong = new AtomicLong(System.currentTimeMillis());

  /**
   * Get UUID.
   * 
   * @return
   */
  public static UUID getUUID() {
    return UUID.randomUUID();
  }

  /**
   * Get UUID as String.
   * 
   * @return
   */
  public static String getUUIDString() {
    return getUUID().toString();
  }

  /**
   * Get a random string based on UUID.
   * 
   * @return
   */
  public static String getRandomString() {
    return getUUIDString().replace("-", "");
  }

  /**
   * Get a random Email.
   * 
   * @return
   */
  public static String getRandomEmail() {
    return "email." + getRandomLongNumberString() + "@" + getRandomLongNumberString() + ".com";
  }

  /**
   * Get a random Long Number.
   * 
   * @return
   */
  public static Long getRandomLongNumber() {
    return atomicLong.incrementAndGet();
  }

  public static String getRandomLongNumberString() {
    String returnString = Long.toString(getRandomLongNumber());
    return returnString;
  }

  public static String getRandomUserId() {
    return getRandomString();
  }

  public static Long getRandomPersonId() {
    return getRandomLongNumber();
  }

  public static String getRandomFederatedId() {
    return "federatedId:" + getRandomString();
  }

  public static String getRandomFederatedAuthority() {
    return "federatedAuthority:" + getRandomString();
  }

  public static String getResourceAsString(String resourcePath)
      throws IOException {
    InputStream is = System.class.getResourceAsStream(resourcePath);
    return LightUtils.getInputStreamAsString(is);
  }
}
