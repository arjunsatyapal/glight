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
package com.google.light.testingutils;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Charsets;
import com.google.light.server.dto.importresource.ImportBatchWrapper;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightUtils;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;

/**
 * Tests till the time there are no proper tests.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class DummyTest {
  @Test
  public void test_collectionTreeNode() throws Exception {
    CollectionTreeNodeDto root = new CollectionTreeNodeDto.Builder()
        .title("title")
        .nodeType(TreeNodeType.ROOT_NODE)
        .moduleType(ModuleType.LIGHT_COLLECTION)
        .externalId(null)
        .build();

    CollectionTreeNodeDto child0 = new CollectionTreeNodeDto.Builder()
        .title("child0")
        .nodeType(TreeNodeType.LEAF_NODE)
        .moduleId(new ModuleId(1234L))
        .moduleType(ModuleType.GOOGLE_DOCUMENT)
        .externalId(TestingConstants.TEST_EXTERNAL_ID)
        .build();
    root.addChildren(child0);

    CollectionTreeNodeDto child1 = new CollectionTreeNodeDto.Builder()
        .title("child1")
        .nodeType(TreeNodeType.INTERMEDIATE_NODE)
        .moduleType(ModuleType.GOOGLE_COLLECTION)
        .externalId(TestingConstants.TEST_EXTERNAL_ID)
        .build();

    root.addChildren(child1);

    String string = JsonUtils.toJson(root);
    CollectionTreeNodeDto root1 = JsonUtils.getDto(string, CollectionTreeNodeDto.class);
    assertEquals(root, root1);
  }

  @Test
  public void test_urlEscape() throws Exception {
    String url = "http://www.google.com?q=1&y=2%3";
    System.out.println("URL = " + url);

    String urlEscaped = URLEncoder.encode(url, Charsets.UTF_8.displayName());
    System.out.println("URL  Escaped = " + urlEscaped);

    String urlEscaped2 = URLEncoder.encode(url, Charsets.UTF_8.displayName());
    System.out.println("URL Escaped2 = " + urlEscaped2);
    assertEquals(urlEscaped, urlEscaped2);

    String urlUnEscaped = URLDecoder.decode(urlEscaped, Charsets.UTF_8.displayName());
    System.out.println("UrlUnEscaped = " + urlUnEscaped);
    assertEquals(url, urlUnEscaped);

    String newUrl =
        "http://en.wikipedia.org/wiki/Google\"><script>alert('FooBar');</script><link rel=\"";
    urlEscaped = URLEncoder.encode(newUrl, Charsets.UTF_8.displayName());

    URL netUrl = new URL(newUrl);
    System.out.println("host = " + netUrl.getHost());
    String file = netUrl.getFile();
    System.out.println("File = " + LightUtils.encodeToUrlEncodedString(file));

    String myNewUrl = "http://" + netUrl.getHost() + LightUtils.encodeToUrlEncodedString(file);
    System.out.println("My new UrL = " + myNewUrl);

    URL netUrl2 = new URL(myNewUrl);

    System.out.println("host2 = " + netUrl2.getHost());

    System.out.println(urlEscaped);
  }

  @Test
  public void test_import_listExternalIdDto() {
    ImportExternalIdDto dtoFolderWithCustomTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://drive.google.com/a/myopenedu.com/?pli=1#folders/0B15KDir5QLAcYXBRMzJCWUhYWEE"))
            .title("Google Folder with Custom Title")
            .build();
    
    ImportExternalIdDto dtoFolderWithoutTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://drive.google.com/a/myopenedu.com/?pli=1#folders/0B15KDir5QLAcQlpiM1hVS25RUUdxcVAwQlNYcXZDQQ"))
            .build();
    
    ImportExternalIdDto dtoEmptyFolderWithoutTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://drive.google.com/a/myopenedu.com/?pli=1#folders/0B15KDir5QLAceG9zeVVPNDBlWnc"))
            .build();
    
    ImportExternalIdDto dtoEmptyFolderWithTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://drive.google.com/a/myopenedu.com/?pli=1#folders/0B15KDir5QLAceG9zeVVPNDBlWnc"))
            .title("Empty Google Folder with Custom Title.")
            .build();

    ImportExternalIdDto dtoDocumentWithCustomTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://docs.google.com/a/myopenedu.com/document/d/1tJZGzv_2sjMpvs4jtwxg18PGuSG-6nlfmx8Hlqa-_58/edit"))
            .title("Google Document with custom title")
            .build();

    ImportExternalIdDto dtoDocumentWithoutTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://docs.google.com/a/myopenedu.com/document/d/1SpnvIapiaT9MfvD2gmEFpz6vtHahwdzexMgX1tR5LyM/edit?pli=1"))
            .build();

    ImportExternalIdDto dtoPresentation =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://docs.google.com/a/myopenedu.com/presentation/d/16PiVnXJvg1CbIB2jwvv73B0nwmpNwk584jMjW-wsPY8/edit#slide=id.p"))
            .title("Some presentation")
            .build();

    ImportExternalIdDto dtoSyntheticWithCustomTitle =
        new ImportExternalIdDto.Builder()
            .externalId(new ExternalId("http://en.wikipedia.org/wiki/Google"))
            .title("Synthetic module with custom title")
            .build();

    ImportExternalIdDto dtoSyntheticWithoutTitle =
        new ImportExternalIdDto.Builder()
            .externalId(new ExternalId("http://en.wikipedia.org/wiki/Gmail"))
            .build();

    ImportBatchWrapper listWrapper = new ImportBatchWrapper();
    listWrapper.addImportModuleDto(dtoFolderWithCustomTitle);
    listWrapper.addImportModuleDto(dtoFolderWithoutTitle);
    listWrapper.addImportModuleDto(dtoEmptyFolderWithoutTitle);
    listWrapper.addImportModuleDto(dtoEmptyFolderWithTitle);
    
    
    listWrapper.addImportModuleDto(dtoDocumentWithCustomTitle);
    listWrapper.addImportModuleDto(dtoDocumentWithoutTitle);
    listWrapper.addImportModuleDto(dtoSyntheticWithCustomTitle);
    listWrapper.addImportModuleDto(dtoSyntheticWithoutTitle);
    listWrapper.setCollectionTitle("New expected collection.");
    listWrapper.setBaseVersion(new Version(Version.LATEST_VERSION));

    System.out.println(JsonUtils.toJson(listWrapper));
  }
}
