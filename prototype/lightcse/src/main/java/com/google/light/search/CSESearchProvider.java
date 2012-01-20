package com.google.light.search;

import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexManager;
import com.google.appengine.api.search.IndexManagerFactory;
import com.google.appengine.api.search.IndexSpec;

public class CSESearchProvider implements SearchProvider {

	@Override
	public SearchResult search(String query, int page) {
		SearchResult result = new SearchResult();
		ArrayList<SearchResultItem> items = new ArrayList<SearchResultItem>();
		result.setItems(items);
		
		//Instantiate a Customsearch object with a transport mechanism and json parser    
		Customsearch customsearch = new Customsearch(new NetHttpTransport(), new JacksonFactory());
		//using deprecated setKey method on customsearch to set your API Key
		customsearch.setKey("AIzaSyCrgEkpgUDlBgrBr9YtKLn8RZ6vSU5k8Nw");
		//instantiate a Customsearch.Cse.List object with your search string
		List<Result> cseItems;
		try {
			com.google.api.services.customsearch.Customsearch.Cse.List list = customsearch.cse().list(query);
			// list.setCx("006574055569862991143:tcyk48xpqx8"); // maggies search engine
			list.setCx("001369170667164983739:xqelsiji0y8"); // mine
			list.setNum(""+SearchProvider.RESULTS_PER_PAGE);
			list.setStart(""+(SearchProvider.RESULTS_PER_PAGE*(page-1)+1));
			Search search = list.execute();
			cseItems = search.getItems();
		} catch (Exception e) {
			e.printStackTrace();
			return result;
		}
		//set your custom search engine id
		
		//list.setStart("10");
		//execute method returns a com.google.api.services.customsearch.model.Search object
		
		//getItems() is a list of com.google.api.services.customsearch.model.Result objects which have the items you want
		
		//now go do something with your list of Result objects
		for(Result cseItem : cseItems) {
			SearchResultItem item = new SearchResultItem();
			item.setTitle(cseItem.getHtmlTitle());
			item.setDescription(cseItem.getHtmlSnippet());
			item.setLink(cseItem.getLink());
			items.add(item);
		}
		
		return result;
	}

}
