package com.google.light.server.dto.search;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.google.light.server.dto.DtoInterface;
import com.google.light.server.utils.JsonUtils;

/**
 * DTO to hold the final search result
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
@JsonSerialize(include = Inclusion.NON_NULL)
public class SearchResultDto implements DtoInterface<SearchResultDto> {
  private List<SearchResultItemDto> items;

  private boolean hasNextPage;

  private String suggestion;
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
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toJson() {
    try {
      return JsonUtils.toJson(this);
    } catch (Exception e) {
      // TODO(waltercacau) : Add exception handling later.
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toXml() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SearchResultDto validate() {
    checkNotNull(items);
    checkState((suggestion != null && suggestionQuery != null)
        || (suggestion == null && suggestionQuery == null));
    return this;
  }

  public static class Builder {
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

    public SearchResultDto build() {
      return new SearchResultDto(this).validate();
    }
  }

  private SearchResultDto(Builder builder) {
    this.items = builder.items;
    this.hasNextPage = builder.hasNextPage;
    this.suggestion = builder.suggestion;
    this.suggestionQuery = builder.suggestionQuery;
  }

  // For JAXB.
  private SearchResultDto() {
  }
}
