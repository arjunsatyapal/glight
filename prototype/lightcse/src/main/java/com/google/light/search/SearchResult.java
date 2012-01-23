package com.google.light.search;

import java.util.List;

public class SearchResult {
	private List<SearchResultItem> items;
	
	private boolean hasNextPage;
	
	private String suggestion;
	private String suggestionQuery;

	public List<SearchResultItem> getItems() {
		return items;
	}

	public void setItems(List<SearchResultItem> items) {
		this.items = items;
	}
	
	public boolean hasNextPage() {
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

	
}
