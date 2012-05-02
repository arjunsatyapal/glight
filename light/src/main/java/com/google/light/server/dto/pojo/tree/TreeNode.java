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
package com.google.light.server.dto.pojo.tree;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.collect.Lists;
import com.google.light.server.dto.AbstractDto;
import java.util.List;
import javax.persistence.Embedded;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
public class TreeNode<T extends TreeNode<T>> extends AbstractDto<T> {
  @XmlElement
  private String title;
  @XmlElement
  private TreeNodeType type;

  @Embedded
  private List<T> children;

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public T validate() {
    checkNotNull(type, "type");
    checkNotNull(title, "title");
    switch (type) {
      case ROOT_NODE:
        break;

      case INTERMEDIATE_NODE:
        break;

      case LEAF_NODE:
        checkNull(children, "LEAF_NODE should have no child");
        break;

      default:
        throw new IllegalStateException("Add more validations for unsupported type : " + type);
    }

    return ((T) this);
  }

  public String getTitle() {
    return title;
  }

  public TreeNodeType getType() {
    return type;
  }
  
  public boolean isLeafNode() {
    return type == TreeNodeType.LEAF_NODE;
  }

  public void addChildren(T treeNode) {
    if (children == null) {
      children = Lists.newArrayList();
    }

    children.add(treeNode);
    validate();
  }

  public List<T> getChildren() {
    return children;
  }

  public boolean hasChildren() {
    if (children == null || children.size() == 0) {
      return false;
    }

    return true;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class Builder<N extends TreeNode, T extends Builder> extends
      AbstractDto.BaseBuilder<Builder> {
    private String title;
    private TreeNodeType type;
    // private N parent;
    private List<N> children;

    public T title(String title) {
      this.title = title;
      return ((T) this);
    }

    public T type(TreeNodeType type) {
      this.type = type;
      return ((T) this);
    }

    // public T parent(N parent) {
    // this.parent = parent;
    // return ((T) this);
    // }

    public T children(List<N> children) {
      this.children = children;
      return ((T) this);
    }
  }

  @SuppressWarnings({ "synthetic-access", "rawtypes", "unchecked" })
  protected TreeNode(Builder builder) {
    super(builder);

    if (builder == null) {
      return;
    }

    this.title = builder.title;
    this.type = builder.type;
    // this.parent = builder.parent;
    this.children = builder.children;
  }

  // For Jaxb and objectify.
  private TreeNode() {
    super(null);
  }

  public static enum TreeNodeType {
    INTERMEDIATE_NODE,
    LEAF_NODE,
    ROOT_NODE;
  }
}
