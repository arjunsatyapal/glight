package com.google.light.search;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringEscapeUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import web.IndexServlet;

import com.google.light.util.ServletUtils;

public class GSSSearchProvider implements SearchProvider {

	private static final String LINK_OF_RESULT_TAG = "U";
	private static final String SNIPPET_OF_RESULT_TAG = "S";
	private static final String TITLE_OF_RESULT_TAG = "T";
	private static final String RESULT_TAG = "R";
	private static final String RESULTS_COUNT_TAG = "M";
	private static final String RESULTS_TAG = "RES";
	private static final Logger log = LoggerFactory
		    .getLogger(GSSSearchProvider.class);
	
	@Override
	public SearchResult search(String query, int page) {
		// Dummy result
		SearchResult result = new SearchResult();
		ArrayList<SearchResultItem> items = new ArrayList<SearchResultItem>();
		result.setItems(items);
		result.setHasNextPage(false);
		
		//http://www.google.com/cse?cx=001369170667164983739:xqelsiji0y8&start=10&num=10&output=xml&q=addition
		
		String url;
		int start = (page-1)*RESULTS_PER_PAGE;
		try {
			url = ServletUtils.createUrl("http://www.google.com/cse",
					"cx", "006574055569862991143:tcyk48xpqx8",
					"output", "xml",
					"num", RESULTS_PER_PAGE,
					"start", start,
					"q", escapeQuery(query));
		} catch (URIException e) {
			e.printStackTrace();
			return result;
		}
		
		Document doc;
		try {
			
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(new URL(url).openStream());

		} catch (Exception e) {
			e.printStackTrace();
			return result;
		}
		
		log.debug(new XMLOutputter().outputString(doc));

		return buildResultListFromDOM(doc, start);
	}

	protected String escapeQuery(String unescaped) {
		return unescaped.replaceAll("\\s", "+");
	}
	
	@SuppressWarnings("unchecked")
	protected SearchResult buildResultListFromDOM(Document doc, int start) {
		SearchResult result = new SearchResult();
		ArrayList<SearchResultItem> items = new ArrayList<SearchResultItem>();
		result.setItems(items);
		result.setHasNextPage(false);
		
		Element root = doc.getRootElement();
		
		Element spellingTag = root.getChild("Spelling");
		if(spellingTag != null) {
			Element suggestionTag = spellingTag.getChild("Suggestion");
			if(suggestionTag!=null) {
				result.setSuggestion(suggestionTag.getText());
				result.setSuggestionQuery(suggestionTag.getAttributeValue("q"));
			}
		}
		
		Element resultsTag = root.getChild(RESULTS_TAG);
		if(resultsTag == null) {
			return result;
		}
		
		Element resultCountTag = resultsTag.getChild(RESULTS_COUNT_TAG);
		if(resultCountTag != null) {
			Long resultCount = Math.min(1000, Long.parseLong(resultCountTag.getText()));
			result.setHasNextPage(resultCount>start+RESULTS_PER_PAGE);
		}
		
		for(Element r : (List<Element>)(resultsTag.getChildren(RESULT_TAG))) {
			SearchResultItem item = new SearchResultItem();
			item.setTitle(r.getChild(TITLE_OF_RESULT_TAG).getText());
			item.setDescription(r.getChild(SNIPPET_OF_RESULT_TAG).getText());
			item.setLink(r.getChild(LINK_OF_RESULT_TAG).getText());
			items.add(item);
		}
		
		return result;
	}
	
}
