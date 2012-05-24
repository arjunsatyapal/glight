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
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.XmlUtils;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        .build1();

    CollectionTreeNodeDto child0 = new CollectionTreeNodeDto.Builder()
        .title("child0")
        .nodeType(TreeNodeType.LEAF_NODE)
        .moduleId(new ModuleId(1234L))
        .version(new Version(5678L))
        .moduleType(ModuleType.GOOGLE_DOCUMENT)
        .externalId(TestingConstants.TEST_EXTERNAL_ID)
        .build1();
    root.addChildren(child0);

    CollectionTreeNodeDto child1 = new CollectionTreeNodeDto.Builder()
        .title("child1")
        .nodeType(TreeNodeType.INTERMEDIATE_NODE)
        .moduleType(ModuleType.GOOGLE_COLLECTION)
        .externalId(TestingConstants.TEST_EXTERNAL_ID)
        .build1();

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

  @SuppressWarnings("unused")
  @Test
  public void test_import_listExternalIdDto() {
    ImportExternalIdDto dtoFolderWithCustomTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://drive.google.com/a/myopenedu.com/?pli=1#folders/0B15KDir5QLAcYXBRMzJCWUhYWEE"))
            .title("Google Folder with Custom Title")
            .build1();
    
    ImportExternalIdDto dtoFolderWithoutTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://drive.google.com/a/myopenedu.com/?pli=1#folders/0B15KDir5QLAcQlpiM1hVS25RUUdxcVAwQlNYcXZDQQ"))
            .build1();
    
    ImportExternalIdDto dtoEmptyFolderWithoutTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://drive.google.com/a/myopenedu.com/?pli=1#folders/0B15KDir5QLAceG9zeVVPNDBlWnc"))
            .build1();
    
    ImportExternalIdDto dtoEmptyFolderWithTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://drive.google.com/a/myopenedu.com/?pli=1#folders/0B15KDir5QLAceG9zeVVPNDBlWnc"))
            .title("Empty Google Folder with Custom Title.")
            .build1();

    ImportExternalIdDto dtoDocumentWithCustomTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://docs.google.com/a/myopenedu.com/document/d/1tJZGzv_2sjMpvs4jtwxg18PGuSG-6nlfmx8Hlqa-_58/edit"))
            .title("Google Document with custom title")
            .build1();

    ImportExternalIdDto dtoDocumentWithoutTitle =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://docs.google.com/a/myopenedu.com/document/d/1SpnvIapiaT9MfvD2gmEFpz6vtHahwdzexMgX1tR5LyM/edit?pli=1"))
            .build1();

    ImportExternalIdDto dtoPresentation =
        new ImportExternalIdDto.Builder()
            .externalId(
                new ExternalId(
                    "https://docs.google.com/a/myopenedu.com/presentation/d/16PiVnXJvg1CbIB2jwvv73B0nwmpNwk584jMjW-wsPY8/edit#slide=id.p"))
            .title("Some presentation")
            .build1();

    ImportExternalIdDto dtoSyntheticWithCustomTitle =
        new ImportExternalIdDto.Builder()
            .externalId(new ExternalId("http://en.wikipedia.org/wiki/Google"))
            .title("Synthetic module with custom title")
            .build1();

    ImportExternalIdDto dtoSyntheticWithoutTitle =
        new ImportExternalIdDto.Builder()
            .externalId(new ExternalId("http://en.wikipedia.org/wiki/Gmail"))
            .build1();
    
    ImportExternalIdDto youTubeVideoWithCustomTitle =
        new ImportExternalIdDto.Builder()
            .externalId(new ExternalId("http://www.youtube.com/watch?v=iytllF9MHko&list=PL1FB965FD592C00C1&index=6&feature=plpp_video"))
            .title("You tube video with custom title")
            .build1();
    
    ImportExternalIdDto youtubePlaylist =
        new ImportExternalIdDto.Builder()
            .externalId(new ExternalId("http://www.youtube.com/playlist?list=PL1FB965FD592C00C1"))
            .build1();

    ImportBatchWrapper listWrapper = new ImportBatchWrapper();
    listWrapper.addImportModuleDto(dtoFolderWithCustomTitle);
    listWrapper.addImportModuleDto(dtoFolderWithoutTitle);
    listWrapper.addImportModuleDto(dtoEmptyFolderWithoutTitle);
    listWrapper.addImportModuleDto(dtoEmptyFolderWithTitle);
    
    
    listWrapper.addImportModuleDto(dtoDocumentWithCustomTitle);
    listWrapper.addImportModuleDto(dtoDocumentWithoutTitle);
    listWrapper.addImportModuleDto(dtoSyntheticWithCustomTitle);
    listWrapper.addImportModuleDto(dtoSyntheticWithoutTitle);

    listWrapper.addImportModuleDto(youTubeVideoWithCustomTitle);
    listWrapper.addImportModuleDto(youtubePlaylist);

    listWrapper.setCollectionTitle("New expected collection.");
    listWrapper.setBaseVersion(new Version(Version.LATEST_VERSION));

    System.out.println(JsonUtils.toJson(listWrapper));
  }
  
  @Test
  public void test_lightUrl() {
//    String MODULE_IDENTIFIER = "/rest/content/general/module/";
//    String pattern = "^" + MODULE_IDENTIFIER + "(\\d+)/((latest)|(\\d+)){1}";
//    System.out.println("modulePattern = " + pattern);
//    Pattern modulePattern = Pattern.compile(pattern);
//    
//    String url = MODULE_IDENTIFIER + "1234/latest";
//    Matcher matcher = modulePattern.matcher(url);
//    System.out.println(url + " : " + matcher.matches());
//    System.out.println("GroupCount = " + matcher.groupCount());
//    for (int i = 0; i < matcher.groupCount(); i++) {
//      System.out.println(matcher.group(i));
//    }
//
//    
//    url = MODULE_IDENTIFIER + "1234/5678";
//    matcher = modulePattern.matcher(url);
//    System.out.println("\n" + url + " : " + matcher.matches());
//    System.out.println("GroupCount = " + matcher.groupCount());
//    for (int i = 0; i < matcher.groupCount(); i++) {
//      System.out.println(matcher.group(i));
//    }
    
    String COLLECTION_IDENTIFIER = "/rest/content/general/collection/";
    String pattern2 =  COLLECTION_IDENTIFIER + "(\\d+)/(latest|\\d+)/((\\d+))";
    System.out.println("collectionPattern = " + pattern2);
    Pattern collectionPattern = Pattern.compile(pattern2);
    System.out.println(collectionPattern);
    
    String url2 = COLLECTION_IDENTIFIER + "1234/9875/5678";
    Matcher matcher2 = collectionPattern.matcher(url2);
    System.out.println("\n" + url2 + " : " + matcher2.matches());
    System.out.println("GroupCount = " + matcher2.groupCount());
    for (int i = 0; i < matcher2.groupCount(); i++) {
      System.out.println(matcher2.group(i));
    }
  }
  
  @Test
  public void test_matcher() {
    String pattern = "foo/(\\d+)/(\\d+)/(\\d+)";
    Pattern pt = Pattern.compile(pattern);
    
    Matcher matcher = pt.matcher("foo/11/12/13");
    System.out.println(matcher.matches());
  }
  
  @Test
  public void test_importExternalId() {
    ImportExternalIdDto dto = LightUtils.createImportExternalIdDto(
        ContentLicense.DEFAULT_LIGHT_CONTENT_LICENSES,
        new ExternalId("http://gdata.youtube.com/feeds/api/videos/4FSdMqhQvBI"),
        new JobId(1234L),
        JobState.ALL_CHILDS_COMPLETED,
        new ModuleId(1234L),
        ModuleType.YOU_TUBE_VIDEO,
        ModuleState.IMPORTING,
        "random",
        new Version(1234L));
    
    System.out.println(XmlUtils.toXml(dto));
    
    System.out.println(JsonUtils.toJson(dto));
  }
}
