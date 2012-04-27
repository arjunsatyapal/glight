package com.google.light.server.dto.search;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.dto.AbstractDto;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * DTO for Search Result Items
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
@JsonSerialize(include = Inclusion.NON_NULL)
public class SearchResultItemDto extends AbstractDto<SearchResultItemDto> {

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
    checkNotNull(title);
    checkNotNull(description);
    // TODO(waltercacau): Add an URL validation here
    checkNotNull(link);
    return this;
  }
  
  /**
   * Sanitizes the HTML content in this Dto.
   * TODO(waltercacau): Implement this method.
   * 
   * @return
   */
  public SearchResultItemDto sanitize() {
    return this;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
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
      return new SearchResultItemDto(this).validate().sanitize();
    }
  }

  @SuppressWarnings("synthetic-access")
  private SearchResultItemDto(Builder builder) {
    super(builder);
    this.title = builder.title;
    this.description = builder.description;
    this.link = builder.link;
  }
}
