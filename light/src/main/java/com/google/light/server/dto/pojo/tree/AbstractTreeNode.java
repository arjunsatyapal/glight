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

import com.google.common.collect.Lists;
import com.google.light.server.dto.AbstractDto;
import java.util.List;
import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public abstract class AbstractTreeNode<T extends AbstractTreeNode<T>> extends AbstractDto<T> {
  @XmlElement(name = "title")
  @JsonProperty(value = "title")
  protected String title;

  @XmlElement(name = "description")
  @JsonProperty(value = "description")
  protected String description;

  @XmlElement(name = "nodeId")
  @JsonProperty(value = "nodeId")
  protected String nodeId;

  @XmlElement(name = "nodeType")
  @JsonProperty(value = "nodeType")
  protected TreeNodeType nodeType;

  // TODO(arjuns): See if this embedded can be removed. At present both Dto and persistence are
  // using this.
  // This is bad.
  @Embedded
  @XmlElementWrapper(name = "list")
  @XmlAnyElement
  @JsonProperty(value = "list")
  protected List<T> list;

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public T validate() {

    checkNotNull(nodeType, "nodeType");
    checkNotNull(title, "title");
    switch (nodeType) {
      case ROOT_NODE:
        break;

      case INTERMEDIATE_NODE:
        break;

      case LEAF_NODE:
        checkNull(list, "LEAF_NODE should have no child");
        break;

      default:
        throw new IllegalStateException("Add more validations for unsupported type : " + nodeType);
    }

    return ((T) this);
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getNodeId() {
    return nodeId;
  }

  public TreeNodeType getNodeType() {
    return nodeType;
  }

  public boolean isLeafNode() {
    return nodeType == TreeNodeType.LEAF_NODE;
  }

  public boolean isIntermediateNode() {
    return nodeType == TreeNodeType.INTERMEDIATE_NODE;
  }

  public void addChildren(T treeNode) {
    getChildren().add(treeNode);
    validate();
  }

  public List<T> getChildren() {
    if (list == null) {
      list = Lists.newArrayList();
    }

    return list;
  }
  
  @SuppressWarnings("unchecked")
  public List<T> getLeafNodes() {
    List<T> list = Lists.newArrayList();
    
    if (getNodeType() == TreeNodeType.LEAF_NODE) {
      list.add(((T) this));
    } else if (hasChildren()) {
      for (T currChild : getChildren()) {
        list.addAll(currChild.getLeafNodes());
      }
    } 
    
    return list;
  }

  public boolean hasChildren() {
    if (list == null || list.size() == 0) {
      return false;
    }

    return true;
  }

  @SuppressWarnings("unchecked")
  public List<T> getInOrderTraversalOfLeafNodes() {
    if (getNodeType() == TreeNodeType.LEAF_NODE) {
      return ((List<T>) Lists.newArrayList(this));
    }

    List<T> list = Lists.newArrayList();

    if (hasChildren()) {
      for (T currChild : getChildren()) {
        list.addAll(currChild.getInOrderTraversalOfLeafNodes());
      }
    }

    return list;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static class Builder<N extends AbstractTreeNode, T extends Builder> extends
      AbstractDto.BaseBuilder<Builder> {
    private String title;
    private String description;
    private String nodeId;

    private TreeNodeType nodeType;
    // private N parent;
    private List<N> children;

    public T title(String title) {
      this.title = title;
      return ((T) this);
    }

    public T description(String description) {
      this.description = description;
      return ((T) this);
    }

    public T nodeId(String nodeId) {
      this.nodeId = nodeId;
      return ((T) this);
    }

    public T nodeType(TreeNodeType nodeType) {
      this.nodeType = nodeType;
      return ((T) this);
    }

    public T children(List<N> children) {
      this.children = children;
      return ((T) this);
    }
  }

  @SuppressWarnings({ "synthetic-access", "rawtypes", "unchecked" })
  protected AbstractTreeNode(Builder builder) {
    super(builder);

    if (builder == null) {
      return;
    }

    this.title = builder.title;
    this.description = builder.description;
    this.nodeId = builder.nodeId;

    this.nodeType = builder.nodeType;
    // this.parent = builder.parent;
    this.list = builder.children;
  }

  // For Jaxb and objectify.
  private AbstractTreeNode() {
    super(null);
  }

  public static enum TreeNodeType {
    INTERMEDIATE_NODE,
    LEAF_NODE,
    ROOT_NODE;
  }
}
