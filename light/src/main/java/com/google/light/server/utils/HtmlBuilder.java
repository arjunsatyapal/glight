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
package com.google.light.server.utils;

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.light.server.exception.ExceptionType;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class HtmlBuilder {
  private StringBuilder builder;

  public HtmlBuilder() {
    this.builder = new StringBuilder();
  }
  
  @Override
  public String toString() {
    appendEnd();
    return builder.toString();
  }
  
  
  public void appendString(String str) {
    builder.append(str);
  }
  
  public void appendPre(String str) {
    appendNodeStart(HtmlNode.PRE);
    builder.append(str);
    appendNodeEnd(HtmlNode.PRE);
  }
  

  public void appendHref(String id, URI uri, String text) {
    checkNotNull(uri, ExceptionType.SERVER, "uri cannot be null");
    checkNotBlank(text, ExceptionType.SERVER, "text cannot be empty");

    Map<String, String> attributes = new ImmutableMap.Builder<String, String>()
        .put("href", "\"" + uri.toString() + "\"")
        .build();

    appendNode(HtmlNode.ANCHOR, id, attributes, text);
  }

  public void appendNode(HtmlNode node, String id, Map<String, String> attributes, String text) {
    checkNotNull(node, ExceptionType.SERVER, "node cannot be null");
    // Adding starting node.
    builder.append("<");
    builder.append(node.getName());

    // appending id if required.
    appendId(id);

    // appending attributes if required.
    appendAttributes(attributes);

    builder.append(">");

    if (text != null) {
      builder.append(text);
    }

    // Adding ending node.
    builder.append("</");
    builder.append(node.getName());
    builder.append(">");
  }

  public void appendId(String id) {
    if (id == null) {
      return;
    }
    builder.append(" id=").append(id);
  }

  public void appendAttributes(Map<String, String> attributes) {
    if (attributes == null) {
      return;
    }

    for (String currKey : attributes.keySet()) {
      String value = LightPreconditions.checkNotBlank(attributes.get(currKey),
          ExceptionType.SERVER, "value cannot be null for key[" + currKey + "].");
      builder.append(" ").append(currKey).append("=").append(value);
    }
  }
  
  public void appendDoctype() {
    builder.append("<!DOCTYPE html>");
    appendAsciiNewLine();
  }
  public void appendHeadStart() {
    appendNodeStart(HtmlNode.HEAD);
  }
  
  public void appendHeadEnd() {
    appendNodeEnd(HtmlNode.HEAD);
  }
  
  public void appendSectionHeader(String sectionHeader) {
    builder.append("<br><br><b>").append(sectionHeader).append(": </b><br>");
  }
  
  public void appendCanonicalUri(URI uri) {
    appendLink("canonical", uri);
  }
  
  public void appendStyle(String style) {
    appendNodeStart(HtmlNode.STYLE);
    builder.append(style);
    appendNodeEnd(HtmlNode.STYLE);
  }
  
  public void appendBodyStart() {
    appendNodeStart(HtmlNode.BODY);
  }
  
  public void appendBodyEnd() {
    appendNodeEnd(HtmlNode.BODY);
  }
  
  public void appendIFrame(URI uri, String height, String width) {
    builder.append("<iframe src=")
           .append(uri.toString())
           .append(" height=\"").append(height).append("\" width=\"").append(width).append("\">")
           .append("</iframe>");
  }
  
  public void appendTableStart(String style) {
    builder.append("<table style=" + style + ">");
  }
  
  public void appendTableEnd() {
    appendNodeEnd(HtmlNode.TABLE);
  }
  
  public void appendTableHeaderRow(Collection<String> tableHeaders) {
    for (String curr : tableHeaders) {
      builder.append("<th>").append(curr).append("</th>");
      appendAsciiNewLine();
    }
  }
  
  public void appendTableRow(Collection<Object> tableRowValues) {
    builder.append("<tr>");
    for (Object curr : tableRowValues) {
      builder.append("<td>").append(curr).append("</td>");
    }
    builder.append("</tr>");
    appendAsciiNewLine();
  }
  
  public void appendLink(String rel, URI href) {
    builder.append("<link rel=\"").append(rel).append("\" href=\"").append(href.toString()).append("\"");
    appendAsciiNewLine();
  }
  
  public void appendAsciiNewLine() {
    builder.append("\n");
  }
  
  public void appendNewLine() {
    builder.append("<br/>");
    appendAsciiNewLine();
  }
  
  
  public void appendNodeStart(HtmlNode htmlNode) {
    builder.append("<").append(htmlNode.getName()).append(">");
    appendAsciiNewLine();
  }
  
  public void appendNodeEnd(HtmlNode htmlNode) {
    builder.append("</").append(htmlNode.getName()).append(">");
    appendAsciiNewLine();
  }
  
  public void appendOL(String item) {
    builder.append("<ol>").append(item).append("</ol>");
    appendNewLine();
  }

  private void appendEnd() {
    builder.append("</body>");
  }
  
  
  public static enum HtmlNode {
    ANCHOR("a"),
    BODY("body"),
    HEAD("head"),
    LINK("link"),
    PRE("pre"),
    STYLE("style"),
    TABLE("table");
    

    private String name;

    private HtmlNode(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}
