package com.google.light.server.dto.search;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.google.light.server.dto.DtoInterface;

/**
 * DTO for Search Result Items
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
@JsonSerialize(include = Inclusion.NON_NULL)
public class SearchResultItemDto implements DtoInterface<SearchResultItemDto> {

  String title;
  String description;
  String link;

  /**
   * Title
   * 
   * Can contain HTML (eg. <b> tags)
   */
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Description
   * 
   * Can contain HTML (eg. <b> tags)
   */
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toJson() {
    throw new UnsupportedOperationException(
        "This DTO is not meant to be directly serialized."
            + "Use SearchResultDto to wrap it instead.");
  }

  @Override
  public String toXml() {
    throw new UnsupportedOperationException(
        "This DTO is not meant to be directly serialized."
            + "Use SearchResultDto to wrap it instead.");
  }

  @Override
  public SearchResultItemDto validate() {
    // TODO(waltercacau): Sanitize the HTML received.
    checkNotNull(title);
    checkNotNull(description);
    // TODO(waltercacau): Add an URL validation here
    checkNotNull(link);
    return this;
  }

  public static class Builder {
    private String title;
    private String description;
    private String link;

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder link(String link) {
      this.link = link;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public SearchResultItemDto build() {
      return new SearchResultItemDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private SearchResultItemDto(Builder builder) {
    this.title = builder.title;
    this.description = builder.description;
    this.link = builder.link;
  }
}
