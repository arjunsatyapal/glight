package com.google.light.server.dto.search;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.xml.bind.annotation.XmlElementWrapper;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;

import com.google.light.server.dto.AbstractDto;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO to hold the final search result
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "searchResult")
@XmlRootElement(name = "searchResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchResultDto extends AbstractDto<SearchResultDto> {
  @XmlElementWrapper(name = "items")
  @XmlElement(name = "searchResultItem")
  @JsonProperty(value = "items")
  private List<SearchResultItemDto> items;

  @XmlElement(name = "hasNextPage")
  @JsonProperty(value = "hasNextPage")
  private boolean hasNextPage;

  @XmlElement(name = "suggestion")
  @JsonProperty(value = "suggestion")
  private String suggestion;

  @XmlElement(name = "suggestionQuery")
  @JsonProperty(value = "suggestionQuery")
  private String suggestionQuery;

  public List<SearchResultItemDto> getItems() {
    return items;
  }

  public void setItems(List<SearchResultItemDto> items) {
    this.items = items;
  }

  public boolean getHasNextPage() {
    return hasNextPage;
  }

  public void setHasNextPage(boolean hasNextPage) {
    this.hasNextPage = hasNextPage;
  }

  public String getSuggestion() {
    return suggestion;
  }

  public void setSuggestion(String suggestion) {
    this.suggestion = suggestion;
  }

  public String getSuggestionQuery() {
    return suggestionQuery;
  }

  public void setSuggestionQuery(String suggestionQuery) {
    this.suggestionQuery = suggestionQuery;
  }

  @Override
  public SearchResultDto validate() {
    checkNotNull(items);
    checkState((suggestion != null && suggestionQuery != null)
        || (suggestion == null && suggestionQuery == null));
    return this;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private List<SearchResultItemDto> items;
    private boolean hasNextPage;
    private String suggestion;
    private String suggestionQuery;

    public Builder items(List<SearchResultItemDto> items) {
      this.items = items;
      return this;
    }

    public Builder hasNextPage(boolean hasNextPage) {
      this.hasNextPage = hasNextPage;
      return this;
    }

    public Builder suggestion(String suggestion) {
      this.suggestion = suggestion;
      return this;
    }

    public Builder suggestionQuery(String suggestionQuery) {
      this.suggestionQuery = suggestionQuery;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public SearchResultDto build() {
      return new SearchResultDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private SearchResultDto(Builder builder) {
    super(builder);
    this.items = builder.items;
    this.hasNextPage = builder.hasNextPage;
    this.suggestion = builder.suggestion;
    this.suggestionQuery = builder.suggestionQuery;
  }

  // For JAXB.
  private SearchResultDto() {
    super(null);
  }
}
