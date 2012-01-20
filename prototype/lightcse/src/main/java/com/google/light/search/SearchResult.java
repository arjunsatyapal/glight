package com.google.light.search;

import java.util.List;

public class SearchResult {
	private List<SearchResultItem> items;

	public List<SearchResultItem> getItems() {
		return items;
	}

	public void setItems(List<SearchResultItem> items) {
		this.items = items;
	}
	
	// FIXME, just heuristics ...
	public boolean hasNextPage() {
		return items.size() == SearchProvider.RESULTS_PER_PAGE;
	}

}
