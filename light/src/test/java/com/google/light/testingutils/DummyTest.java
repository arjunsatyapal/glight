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

import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.utils.JsonUtils;
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
        .externalId("null")
        .build();

    CollectionTreeNodeDto child0 = new CollectionTreeNodeDto.Builder()
        .title("child0")
        .nodeType(TreeNodeType.LEAF_NODE)
        .moduleId(new ModuleId(1234L))
        .moduleType(ModuleType.GOOGLE_DOCUMENT)
        .externalId("1234")
        .build();
    root.addChildren(child0);

    CollectionTreeNodeDto child1 = new CollectionTreeNodeDto.Builder()
        .title("child1")
        .nodeType(TreeNodeType.INTERMEDIATE_NODE)
        .moduleType(ModuleType.GOOGLE_COLLECTION)
        .externalId("1234")
        .build();

    root.addChildren(child1);

    String string = JsonUtils.toJson(root);
    CollectionTreeNodeDto root1 = JsonUtils.getDto(string, CollectionTreeNodeDto.class);
    assertEquals(root, root1);
  }
}
