package com.google.light.search;

import java.util.ArrayList;
import java.util.List;

public interface SearchProvider {
	List<SearchResult> search(String query);
}
