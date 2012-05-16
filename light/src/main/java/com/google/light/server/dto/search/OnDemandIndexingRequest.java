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
package com.google.light.server.dto.search;

import java.util.ArrayList;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.google.common.collect.ImmutableMap;
import com.google.light.server.dto.AbstractDto;

/**
 * Builder for a On Demand Indexing Request.
 * 
 * TODO(waltercacau): Add test for this
 * 
 * @author Walter Cacau
 */
public class OnDemandIndexingRequest {
  private static final ImmutableMap<Object, Object> EMPTY_MAP =
      new ImmutableMap.Builder<Object, Object>().build();

  public static class Page {
    @SuppressWarnings("synthetic-access")
    public Page(String url, Map<Object, Object> metadata) {
      super();
      this.url = url;
      if (metadata != null) {
        this.metadata = metadata;
      } else {
        this.metadata = EMPTY_MAP;
      }
    }

    private String url;
    private Map<Object, Object> metadata;

    public String getUrl() {
      return url;
    }

    public Map<Object, Object> getMetadata() {
      return metadata;
    }
  }

  private ArrayList<Page> pages = new ArrayList<Page>();

  public OnDemandIndexingRequest add(String url) {
    return add(url, (Map<Object, Object>) null);
  }

  @SuppressWarnings({ "unchecked", "cast" })
  public <T extends AbstractDto<T>> OnDemandIndexingRequest add(String url, T dto) {
    return add(url, ((Map<Object, Object>) new ObjectMapper().convertValue(dto, Map.class)));
  }

  public OnDemandIndexingRequest add(String url, Map<Object, Object> metadata) {
    pages.add(new Page(url, metadata));
    return this;
  }

  // TODO(waltercacau): Might be useful to move it
  private static class ElementBuilder {
    private Element element;

    public Element build() {
      return element;
    }

    public ElementBuilder addChild(Element child) {
      element.addContent(child);
      return this;
    }

    public ElementBuilder text(String text) {
      element.setText(text);
      return this;
    }

    public ElementBuilder attr(String key, String value) {
      element.setAttribute(key, value);
      return this;
    }

    public ElementBuilder(String name) {
      this.element = new Element(name);
    }
  }

  public String toXmlString() {
    ElementBuilder pagesBuilder = new ElementBuilder("Pages");
    
    for(Page page : pages) {
      ElementBuilder pageBuilder =
          new ElementBuilder("Page").attr("url", page.getUrl());

      Map<Object, Object> metadata = page.getMetadata();
      if (metadata.size() > 0) {
        ElementBuilder dataObjectBuilder = new ElementBuilder("DataObject").attr("type", "document");
        for (Map.Entry<Object, Object> entry : metadata.entrySet()) {
          dataObjectBuilder.addChild(
              new ElementBuilder("Attribute")
                  .attr("name", entry.getKey().toString())
                  .text(entry.getValue().toString())
                  .build()
              );
        }

        pageBuilder.addChild(
            new ElementBuilder("PageMap")
                .addChild(dataObjectBuilder.build())
                .build()
            );
      }
      pagesBuilder.addChild(pageBuilder.build());
    }
    
    Document doc = new Document(
        new ElementBuilder("OnDemandIndex")
            .addChild(
                pagesBuilder.build()
            )
            .build()
        );
    XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
    return xmlOutputter.outputString(doc);
  }
}
