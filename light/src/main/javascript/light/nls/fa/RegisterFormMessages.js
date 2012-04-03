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
/**
 * Calling window.define instead of define because of dojo build bug.
 * @see http://bugs.dojotoolkit.org/ticket/15057
 */
window.define({
  instructions: "قبل از ادامه شما نیاز به پر کردن این فرم ثبت نام کوچک و شرایط نور از خدمات را بپذیرند.",
  firstNameTextBoxLabel: "نام و نام خانوادگی:",
  lastNameTextBoxLabel: "نام خانوادگی:",
  tosTextareaLabel: "شرایط و ضوابط خدمات",
  tosTextareaContent: "اصطلاحات حقوقی",
  tosCheckboxLabel: "من با شرایط استفاده از خدمات موافقم",
  submitButton: "ثبت نام",
  tosTooltipError: "شما باید به شرایط و ضوابط خدمات قبل از اقدام به توافق برسند"
});