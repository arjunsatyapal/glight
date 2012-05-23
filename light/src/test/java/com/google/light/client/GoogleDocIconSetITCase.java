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
package com.google.light.client;

import static com.google.light.server.constants.http.HttpStatusCodesEnum.NOT_FOUND;
import static com.google.light.server.constants.http.HttpStatusCodesEnum.OK;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.light.server.dto.module.ModuleType;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * Tests in this class should ensure we are in sync
 * with Google Docs' icon set.
 * 
 * @author Walter Cacau
 */
public class GoogleDocIconSetITCase {
  String[] CURRENT_ICON_LINKS = {
          "https://ssl.gstatic.com/docs/doclist/images/collectionsprite_1.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_10_spreadsheet_list.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_10_presentation_list.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_10_generic_list.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_10_form_list.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_10_drawing_list.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_10_document_list.png"
  };
  String[] CURRENT_ICON_NAMES = {
          ModuleType.GOOGLE_COLLECTION.name() + "_ORIGINAL",
          ModuleType.GOOGLE_SPREADSHEET.name(),
          ModuleType.GOOGLE_PRESENTATION.name(),
          ModuleType.GOOGLE_FILE.name(),
          ModuleType.GOOGLE_FORM.name(),
          ModuleType.GOOGLE_DRAWING.name(),
          ModuleType.GOOGLE_DOCUMENT.name()
  };

  String[] NEXT_ITERATION_ICON_LINKS = {
          "https://ssl.gstatic.com/docs/doclist/images/collectionsprite_2.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_11_spreadsheet_list.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_11_presentation_list.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_11_generic_list.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_11_form_list.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_11_drawing_list.png",
          "https://ssl.gstatic.com/docs/doclist/images/icon_11_document_list.png"
  };

  private final String serverUrl = "http://localhost:8080";

  @Test
  public void test_counters() throws Exception {
    assertEquals("You should add/remove an Google Docs Icon if you add/remove a"
        + " Google Docs type in the ModuleType changes", 14,
        ModuleType.values().length);
    assertEquals(CURRENT_ICON_LINKS.length, 7);
    assertEquals(CURRENT_ICON_NAMES.length, CURRENT_ICON_LINKS.length);
    assertEquals(NEXT_ITERATION_ICON_LINKS.length, 7);
  }

  @Test
  public void test_currentIconLinksAreLive() throws IOException {
    HttpTransport httpTransport = new NetHttpTransport();
    assertEquals(CURRENT_ICON_LINKS.length, CURRENT_ICON_NAMES.length);
    for (int i = 0; i < CURRENT_ICON_LINKS.length; i++) {
      String iconLink = CURRENT_ICON_LINKS[i];
      String iconName = CURRENT_ICON_NAMES[i];
      if (iconName == null)
        continue;

      HttpRequest request =
          httpTransport.createRequestFactory().buildGetRequest(new GenericUrl(iconLink));
      request.setThrowExceptionOnExecuteError(false);
      HttpResponse response = request.execute();
      assertEquals(OK.getStatusCode(), response.getStatusCode());

      HttpRequest request2 =
          httpTransport.createRequestFactory().buildGetRequest(
              new GenericUrl(serverUrl + "/images/gdoc/" + iconName + ".png"));
      request2.setThrowExceptionOnExecuteError(false);
      HttpResponse response2 = request2.execute();
      assertEquals(OK.getStatusCode(), response2.getStatusCode());

      assertTrue(
          "Icon " + iconName + " changed",
          Arrays.equals(IOUtils.toByteArray(response2.getContent()),
              IOUtils.toByteArray(response.getContent())));
    }
  }

  @Test
  public void test_nextIterationIconLinksWasNotDeployed() throws IOException {
    HttpTransport httpTransport = new NetHttpTransport();
    for (String iconLink : NEXT_ITERATION_ICON_LINKS) {
      HttpRequest request =
          httpTransport.createRequestFactory().buildGetRequest(new GenericUrl(iconLink));
      request.setThrowExceptionOnExecuteError(false);
      HttpResponse response = request.execute();
      assertEquals(NOT_FOUND.getStatusCode(), response.getStatusCode());
    }
  }
}
