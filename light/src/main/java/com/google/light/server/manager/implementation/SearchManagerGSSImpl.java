package com.google.light.server.manager.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.dto.search.SearchRequestDto;
import com.google.light.server.dto.search.SearchResultDto;
import com.google.light.server.dto.search.SearchResultItemDto;
import com.google.light.server.manager.interfaces.SearchManager;

/**
 * An implementation of {@link SearchManager} using Google Site Search.
 * 
 * @author Walter Cacau
 */
public class SearchManagerGSSImpl implements SearchManager {

  private static final String GSS_CSE_URL = "http://www.google.com/cse";
  // TODO(waltercacau): Extract this to a property file
  private static final String GSS_CSE_ID = "006574055569862991143:tcyk48xpqx8";
  private static final String SUGGESTION_QUERY_TAG = "q";
  private static final String SUGGESTION_TAG = "Suggestion";
  private static final String SPELLING_TAG = "Spelling";
  private static final String LINK_OF_RESULT_TAG = "U";
  private static final String SNIPPET_OF_RESULT_TAG = "S";
  private static final String TITLE_OF_RESULT_TAG = "T";
  private static final String RESULT_TAG = "R";
  private static final String RESULTS_COUNT_TAG = "M";
  private static final String RESULTS_TAG = "RES";
  private static final Logger log = Logger.getLogger(SearchManagerGSSImpl.class.getName());
  static final int RESULTS_PER_PAGE = 10;
  private Provider<HttpTransport> httpTransportProvider;

  @Inject
  public SearchManagerGSSImpl(Provider<HttpTransport> httpTransportProvider) {
    this.httpTransportProvider = httpTransportProvider;
  }

  public SearchResultDto search(SearchRequestDto searchRequest) {
    String query = searchRequest.getQuery();
    int page = searchRequest.getPage();

    // Dummy result to be returned when some error occurs
    // TODO(waltercacau): Find an HTTP Exception to map errors into, instead of just returning a
    // empty list of results.
    SearchResultDto result =
        new SearchResultDto.Builder().items(new ArrayList<SearchResultItemDto>())
            .hasNextPage(false).build();

    log.info("Searching for " + query);
    int start = (page - 1) * RESULTS_PER_PAGE;

    HttpTransport httpTransport = this.httpTransportProvider.get();
    GenericUrl url = new GenericUrl(GSS_CSE_URL);
    url.put("cx", GSS_CSE_ID);
    url.put("output", "xml_no_dtd");
    url.put("num", RESULTS_PER_PAGE);
    url.put("start", start);
    url.put("q", escapeQuery(query));

    Document doc;
    try {

      SAXBuilder builder = new SAXBuilder();

      doc =
          builder.build(httpTransport.createRequestFactory().buildGetRequest(url).execute()
              .getContent());

    } catch (Exception e) {
      log.severe(Throwables.getStackTraceAsString(e));
      return result;
    }

    log.info(new XMLOutputter().outputString(doc));

    return buildResultListFromDOM(doc, start);
  }

  protected String escapeQuery(String unescaped) {
    return unescaped.replaceAll("\\s", "+");
  }

  @SuppressWarnings("unchecked")
  @VisibleForTesting
  SearchResultDto buildResultListFromDOM(Document doc, int start) {
    ArrayList<SearchResultItemDto> items = new ArrayList<SearchResultItemDto>();
    SearchResultDto result = new SearchResultDto.Builder().items(items).hasNextPage(false).build();

    Element root = doc.getRootElement();

    Element spellingTag = root.getChild(SPELLING_TAG);
    if (spellingTag != null) {
      Element suggestionTag = spellingTag.getChild(SUGGESTION_TAG);
      if (suggestionTag != null) {
        result.setSuggestion(suggestionTag.getText());
        result.setSuggestionQuery(suggestionTag.getAttributeValue(SUGGESTION_QUERY_TAG));
      }
    }

    Element resultsTag = root.getChild(RESULTS_TAG);
    if (resultsTag == null) {
      return result;
    }

    Element resultCountTag = resultsTag.getChild(RESULTS_COUNT_TAG);
    if (resultCountTag != null) {
      Long resultCount = Math.min(1000, Long.parseLong(resultCountTag.getText()));
      result.setHasNextPage(resultCount > start + RESULTS_PER_PAGE);
    }

    for (Element r : (List<Element>) (resultsTag.getChildren(RESULT_TAG))) {
      items.add(new SearchResultItemDto.Builder().title(r.getChild(TITLE_OF_RESULT_TAG).getText())
          .description(r.getChild(SNIPPET_OF_RESULT_TAG).getText())
          .link(r.getChild(LINK_OF_RESULT_TAG).getText()).build());
    }

    return result;
  }

}
