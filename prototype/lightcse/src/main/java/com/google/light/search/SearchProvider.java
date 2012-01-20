package com.google.light.search;

import java.util.ArrayList;
import java.util.List;

public interface SearchProvider {
	static final int RESULTS_PER_PAGE = 10;
	
	/**
	 * Searchs for materials for lessons
	 * Page starts at 1 and it indicates what page of results we are seeing
	 * 
	 * @param query
	 * @param page
	 * @return
	 */
	SearchResult search(String query, int page);
}
