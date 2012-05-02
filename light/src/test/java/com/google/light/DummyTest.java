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
package com.google.light;

import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.tree.CollectionTreeNode;
import com.google.light.server.dto.pojo.tree.TreeNode.TreeNodeType;
import com.google.light.server.utils.JsonUtils;
import org.junit.Test;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class DummyTest {
  @Test
  public void test_foo() throws Exception {
    CollectionTreeNode root = new CollectionTreeNode.Builder()
        .title("title")
        .type(TreeNodeType.ROOT_NODE)
        .build();

    CollectionTreeNode child0 = new CollectionTreeNode.Builder()
        .title("child0")
        .type(TreeNodeType.LEAF_NODE)
        .moduleId(new ModuleId(1234L))
        .build();
    root.addChildren(child0);

    CollectionTreeNode child1 = new CollectionTreeNode.Builder()
        .title("child1")
        .type(TreeNodeType.INTERMEDIATE_NODE)
        .build();

    root.addChildren(child1);

    // System.out.println(XmlUtils.toXml(root));
    System.out.println(JsonUtils.toJson(root));

    String string = JsonUtils.toJson(root);
    CollectionTreeNode root1 = JsonUtils.getDto(string, CollectionTreeNode.class);
     System.out.println("\n*********" + JsonUtils.toJson(root1));

  }
}
