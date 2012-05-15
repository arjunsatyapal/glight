package com.google.light.server.manager.interfaces;

import com.google.light.server.dto.search.OnDemandIndexingRequest;
import com.google.light.server.dto.search.SearchRequestDto;
import com.google.light.server.dto.search.SearchResultDto;
import com.google.light.server.exception.checked.FailedToIndexException;

/**
 * Manager for operations related to Search. 
 * 
 * @author Walter Cacau
 */
public interface SearchManager {
    /**
     * Searches Light's modules
     * 
     * @param searchRequest
     * @return
     */
    public SearchResultDto search(SearchRequestDto searchRequest);
    
    public void index(OnDemandIndexingRequest request) throws FailedToIndexException;
}
