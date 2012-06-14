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

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;

public class SeleniumUtils {
  // TODO(waltercacau): Add a command line flag to change which browser to use.
  public static WebDriver getWebDriver() {
    return new ChromeDriver();
    //return new FirefoxDriver();
  }
  
  /**
   * Click's at the center of the element. This helps when
   * testing a page with Dojo widget's.
   * @param driver
   * @param by
   */
  public static void clickAtCenter(WebDriver driver, By by) {
    WebElement element = driver.findElement(by);
    Actions builder = new Actions(driver);
    builder
        .moveToElement(element)
        .click()
        .perform();
  }
  
  /**
   * Click's in an element if it exists.
   * 
   * @param driver
   * @param by
   * @return true if the element existed
   */
  public static boolean clickIfExists(WebDriver driver, By by) {
    WebElement element;
    try {
      element = driver.findElement(by);
    } catch (NoSuchElementException e) {
      return false;
    }
    element.click();
    return true;
  }
}
