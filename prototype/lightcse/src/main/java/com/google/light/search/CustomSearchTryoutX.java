package com.google.light.search;

import java.io.IOException;
import java.util.List;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

public class CustomSearchTryoutX {
	public static void main(String[] args) throws IOException {
		// Set up the HTTP transport and JSON factory
		/*HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		
		String clientId = "894466405958.apps.googleusercontent.com";
		String clientSecret = "GW9jdjMaCZR7sEhoK_gZqc5O";
		String accessToken = "AIzaSyCrgEkpgUDlBgrBr9YtKLn8RZ6vSU5k8Nw";
		
		GoogleAccessProtectedResource requestInitializer = new GoogleAccessProtectedResource(
				accessToken, httpTransport, jsonFactory, clientId,
				clientSecret, refreshToken);
		
		GoogleClient 
		
		Builder builder = Customsearch.builder(httpTransport, jsonFactory);
		Customsearch cs = builder.build();
		cs.setKey("AIzaSyCrgEkpgUDlBgrBr9YtKLn8RZ6vSU5k8Nw");*/
		
		//Instantiate a Customsearch object with a transport mechanism and json parser    
		Customsearch customsearch = new Customsearch(new NetHttpTransport(), new JacksonFactory());
		//using deprecated setKey method on customsearch to set your API Key
		//customsearch.setKey("AIzaSyCrgEkpgUDlBgrBr9YtKLn8RZ6vSU5k8Nw");
		//instantiate a Customsearch.Cse.List object with your search string
		com.google.api.services.customsearch.Customsearch.Cse.List list = customsearch.cse().list("whatever");
		//set your custom search engine id
		//list.setCx("001369170667164983739:xqelsiji0y8");
		//list.setCx("006574055569862991143:tcyk48xpqx8");
		list.setStart("10");
		//execute method returns a com.google.api.services.customsearch.model.Search object
		Search results = list.execute();
		//getItems() is a list of com.google.api.services.customsearch.model.Result objects which have the items you want
		List<Result> items = results.getItems();
		//now go do something with your list of Result objects
		for(Result item : items) {
			System.out.println(item.getHtmlTitle());
			System.out.println(item.getHtmlSnippet());
			System.out.println(item.getLink());
			System.out.println("...xsfg");
		}
		
	}
}
