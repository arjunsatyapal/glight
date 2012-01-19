package com.google.light.search;

import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexManager;
import com.google.appengine.api.search.IndexManagerFactory;
import com.google.appengine.api.search.IndexSpec;

public class CSESearchProvider implements SearchProvider {

	@Override
	public List<SearchResult> search(String query) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		
		//Instantiate a Customsearch object with a transport mechanism and json parser    
		Customsearch customsearch = new Customsearch(new NetHttpTransport(), new JacksonFactory());
		//using deprecated setKey method on customsearch to set your API Key
		customsearch.setKey("AIzaSyCrgEkpgUDlBgrBr9YtKLn8RZ6vSU5k8Nw");
		//instantiate a Customsearch.Cse.List object with your search string
		List<Result> items;
		try {
			com.google.api.services.customsearch.Customsearch.Cse.List list = customsearch.cse().list(query);
			list.setCx("006574055569862991143:tcyk48xpqx8"); // maggies search engine
			items = list.execute().getItems();
		} catch (Exception e) {
			e.printStackTrace();
			return results; 
		}
		//set your custom search engine id
		
		//list.setStart("10");
		//execute method returns a com.google.api.services.customsearch.model.Search object
		
		//getItems() is a list of com.google.api.services.customsearch.model.Result objects which have the items you want
		
		//now go do something with your list of Result objects
		for(Result item : items) {
			SearchResult result = new SearchResult();
			result.setTitle(item.getHtmlTitle());
			result.setDescription(item.getHtmlSnippet());
			result.setLink(item.getLink());
			results.add(result);
		}
		return results;
	}

}
