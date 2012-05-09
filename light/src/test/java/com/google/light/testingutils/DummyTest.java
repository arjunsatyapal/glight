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

import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;

import com.google.common.collect.Lists;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.CollectionTreeNodeDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceIdListWrapperDto;
import com.google.light.server.exception.unchecked.JsonException;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.XmlUtils;
import java.util.List;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
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
        .type(TreeNodeType.ROOT_NODE)
        .moduleType(ModuleType.LIGHT_COLLECTION)
        .externalId("null")
        .build();

    CollectionTreeNodeDto child0 = new CollectionTreeNodeDto.Builder()
        .title("child0")
        .type(TreeNodeType.LEAF_NODE)
        .moduleId(new ModuleId(1234L))
        .moduleType(ModuleType.GOOGLE_DOC)
        .externalId("1234")
        .build();
    root.addChildren(child0);

    CollectionTreeNodeDto child1 = new CollectionTreeNodeDto.Builder()
        .title("child1")
        .type(TreeNodeType.INTERMEDIATE_NODE)
        .moduleType(ModuleType.GOOGLE_COLLECTION)
        .externalId("1234")
        .build();

    root.addChildren(child1);

    String string = JsonUtils.toJson(root);
    CollectionTreeNodeDto root1 = JsonUtils.getDto(string, CollectionTreeNodeDto.class);
    assertEquals(root, root1);
  }
  
  @Test
  public void test_GoogleDocResourceIdList() throws Exception {
    List<GoogleDocResourceId> list = Lists.newArrayList();
    list.add(new GoogleDocResourceId("document:1mXX53OtXIhq2XbdQkk-utxO9pHdQ_dQsSsPE_HNtN_s"));
    list.add(new GoogleDocResourceId("folder:0B15KDir5QLAcQlpiM1hVS25RUUdxcVAwQlNYcXZDQQ"));

    GoogleDocResourceIdListWrapperDto resourceList1 = new GoogleDocResourceIdListWrapperDto();
    resourceList1.setGoogleDocResourceList(list);
    resourceList1.setCollectionTitle("Title");
    resourceList1.setCollectionId(new CollectionId(103L));
    
    String json = JsonUtils.toJson(resourceList1);
    System.out.println(json);
    System.out.println(XmlUtils.toXml(resourceList1));
    
    GoogleDocResourceIdListWrapperDto resourceList2 = JsonUtils.getDto(json, GoogleDocResourceIdListWrapperDto.class);
    assertEquals(JsonUtils.toJson(resourceList1), JsonUtils.toJson(resourceList2));
    assertEquals(list, resourceList2.getList());
  }
  
  
//  @Test
//  public void test_post_json_xml() throws Exception {
//    List<GoogleDocResourceId> list = Lists.newArrayList();
//    list.add(new GoogleDocResourceId("document:1tJZGzv_2sjMpvs4jtwxg18PGuSG-6nlfmx8Hlqa-_58"));
//    list.add(new GoogleDocResourceId("document:1sEYJiCJEGogZ0bIY2BoyIVB9lPIbD5CjiTsBfZKrn3I"));
//
//    GoogleDocResourceIdList resourceList1 = new GoogleDocResourceIdList();
//    resourceList1.setGoogleDocResourceList(list);
//    
//    String json = JsonUtils.toJson(resourceList1);
//    String xml = XmlUtils.toXml(resourceList1);
//    
//    LightHttpClient httpClient = new LightHttpClient();
//    
//    ByteArrayContent content = new ByteArrayContent(ContentTypeEnum.APPLICATION_JSON.get(), json.getBytes(Charsets.UTF_8));
//    HttpResponse response = httpClient.post("http://localhost:8080/rest/thirdparty/google/gdoc/import", 
//        content);
//    
//    String result = getInputStreamAsString(response.getContent());
//    System.out.println(result);
//    assertEquals(result, xml);
//    
//    content = new ByteArrayContent(ContentTypeEnum.APPLICATION_XML.get(), xml.getBytes(Charsets.UTF_8));
//    response = httpClient.post("http://localhost:8080/rest/thirdparty/google/gdoc/import", 
//        content);
//    result = getInputStreamAsString(response.getContent());
//    System.out.println(result);
//    assertEquals(result, json);
//  }
  
  
  public static <T> String toJson(T object, boolean prettyPrint) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
    // make serializer use JAXB annotations (only)
    mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
    mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    
    ObjectWriter writer = null;

    if (prettyPrint) {
      writer = mapper.writerWithDefaultPrettyPrinter();
    } else {
      writer = mapper.writer();
    }
    try {
      return writer.writeValueAsString(object);
    } catch (Exception e) {
      throw new JsonException(e);
    }
  }
}
