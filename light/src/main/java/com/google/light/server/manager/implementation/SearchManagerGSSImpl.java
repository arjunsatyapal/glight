package com.google.light.server.manager.implementation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringEscapeUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.constants.GSSSupportedLanguagesEnum;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.dto.search.OnDemandIndexingRequest;
import com.google.light.server.dto.search.SearchRequestDto;
import com.google.light.server.dto.search.SearchResultDto;
import com.google.light.server.dto.search.SearchResultItemDto;
import com.google.light.server.exception.checked.FailedToIndexException;
import com.google.light.server.manager.interfaces.GSSClientLoginTokenManager;
import com.google.light.server.manager.interfaces.SearchManager;
import com.google.light.server.utils.GaeUtils;

/**
 * An implementation of {@link SearchManager} using Google Site Search.
 * 
 * @author Walter Cacau
 */
public class SearchManagerGSSImpl implements SearchManager {
  private static final Logger logger = Logger.getLogger(SearchManagerGSSImpl.class.getName());

  private static final String GSS_CSE_URL = "http://www.google.com/cse";
  
  private static final String GSS_CSE_ID;
  private static final String GSS_CSE_OWNER_ID;
  static {
    // TODO(waltercacau): Extract this to a property file
    if(!GaeUtils.isDevServer() && "light-demo".equals(GaeUtils.getAppIdFromSystemProperty())) {
      // CSE Owned by search-admin@myopenedu.com
      GSS_CSE_ID = "tpzezwcekzm";
      GSS_CSE_OWNER_ID = "010876134375682122369";
    } else {
      // CSE Owned by waltercacau@google.com
      GSS_CSE_ID = "xqelsiji0y8";
      GSS_CSE_OWNER_ID = "001369170667164983739";
      
    }
  }
  private static final String SUGGESTION_QUERY_TAG = "q";
  private static final String SUGGESTION_TAG = "Suggestion";
  private static final String SPELLING_TAG = "Spelling";
  private static final String LINK_OF_RESULT_TAG = "U";
  private static final String SNIPPET_OF_RESULT_TAG = "S";
  private static final String TITLE_OF_RESULT_TAG = "T";
  private static final String RESULT_TAG = "R";
  private static final String RESULTS_COUNT_TAG = "M";
  private static final String RESULTS_TAG = "RES";
  private Provider<HttpTransport> httpTransportProvider;

  private GSSClientLoginTokenManager tokenManager;

  @Inject
  public SearchManagerGSSImpl(Provider<HttpTransport> httpTransportProvider,
      GSSClientLoginTokenManager tokenManager) {
    this.tokenManager = tokenManager;
    this.httpTransportProvider = checkNotNull(httpTransportProvider);
  }

  @Override
  public SearchResultDto search(SearchRequestDto searchRequest) {
    String query = searchRequest.getQuery();
    int page = searchRequest.getPage();

    // Dummy result to be returned when some error occurs
    // TODO(waltercacau): Find an HTTP Exception to map errors into, instead of just returning a
    // empty list of results.
    SearchResultDto result =
        new SearchResultDto.Builder().items(new ArrayList<SearchResultItemDto>())
            .hasNextPage(false).build();

    logger.info("Searching for " + query);
    int start =
        (page - LightConstants.FIRST_SEARCH_PAGE_NUMBER) * LightConstants.SEARCH_RESULTS_PER_PAGE;

    HttpTransport httpTransport = this.httpTransportProvider.get();
    GenericUrl url = new GenericUrl(GSS_CSE_URL);
    url.put("cx", GSS_CSE_OWNER_ID + ":" + GSS_CSE_ID);
    url.put("output", "xml_no_dtd");
    url.put("num", LightConstants.SEARCH_RESULTS_PER_PAGE);
    url.put("start", start);
    url.put("q", query);
    url.put("hl", GSSSupportedLanguagesEnum.getClosestGSSSupportedLanguage(
        searchRequest.getClientLanguageCode()).getLanguageCode());

    Document doc;
    try {

      SAXBuilder builder = new SAXBuilder();

      HttpRequest request = httpTransport.createRequestFactory().buildGetRequest(url);
      HttpResponse response = request.execute();

      doc = builder.build(response.getContent());

    } catch (Exception e) {

      logger.severe(Throwables.getStackTraceAsString(e));
      return result;
    }

    logger.info(new XMLOutputter().outputString(doc));

    return buildResultListFromDOM(doc, start);
  }

  @SuppressWarnings("unchecked")
  @VisibleForTesting
  /**
   * Takes a JDOM parsed Document and return's the SearchResultDto
   * it represents.
   * @param doc
   * @param start Expected position of the first record used to calculate
   *              the boolean value hasNextPage.
   * @return Search result
   */
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
      result.setHasNextPage(resultCount > start + LightConstants.SEARCH_RESULTS_PER_PAGE);
    }

    for (Element resultTag : (List<Element>) (resultsTag.getChildren(RESULT_TAG))) {
      SearchResultItemDto.Builder builder = new SearchResultItemDto.Builder();

      String link = resultTag.getChild(LINK_OF_RESULT_TAG).getText();

      // Google CSE might not return the title tag, so we need to figure
      // out a title to show.
      Element titleTag = resultTag.getChild(TITLE_OF_RESULT_TAG);
      if (titleTag != null) {
        builder.title(titleTag.getText());
      } else {
        builder.title(StringEscapeUtils.escapeHtml(link));
      }

      items.add(builder
          .description(resultTag.getChild(SNIPPET_OF_RESULT_TAG).getText())
          .link(link).build());
    }

    return result;
  }

  @Override
  public void index(OnDemandIndexingRequest request) throws FailedToIndexException {
    // Getting the token and trying to authenticate if necessary
    String authorizationToken = null;
    try {
      authorizationToken = tokenManager.getCurrentToken();
      if (authorizationToken == null) {
        authorizationToken = tokenManager.authenticate();
      }
    } catch (Exception e) {
      throw new FailedToIndexException("Failed to authenticate", e);
    }

    // Transforming the request into HttpContent
    byte[] contentBytes = null;
    try {
      contentBytes = request.toXmlString().getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new FailedToIndexException("Failed to encode request in UTF-8", e);
    }
    InputStream stream = new ByteArrayInputStream(contentBytes);
    HttpContent content = new InputStreamContent("application/xml; charset=utf-8", stream);

    // Preparing the request
    try {
      HttpRequest httpRequest =
          httpTransportProvider
              .get()
              .createRequestFactory()
              .buildPostRequest(
                  new GenericUrl("https://www.google.com/cse/api/" + GSS_CSE_OWNER_ID + "/index/"
                      + GSS_CSE_ID),
                  content);

      httpRequest.setEnableGZipContent(false);
      httpRequest.getHeaders().setAuthorization(authorizationToken);
      httpRequest.getHeaders().setAcceptEncoding("utf-8");

      httpRequest.setThrowExceptionOnExecuteError(false);
      HttpResponse response = httpRequest.execute();

      if (!response.isSuccessStatusCode()) {
        throw new FailedToIndexException("Failed to index. Received from GSS/CSE: "
            + response.getStatusCode() + " " + response.getStatusMessage());
      }

      // Reading the response
      InputStream contentStream = response.getContent();
      Document doc = new SAXBuilder().build(contentStream);
      logger.info(new XMLOutputter().outputString(doc));
      Element root = doc.getRootElement();
      Attribute refreshStatusAttribute = root.getAttribute("refresh_status");
      if (refreshStatusAttribute == null) {
        throw new FailedToIndexException("GSS/CSE response format has changed.");
      }

      if (!"SUCCESS".equals(refreshStatusAttribute.getValue())) {
        throw new FailedToIndexException("Failed to index. Received from GSS/CSE refresh_status = "
            + refreshStatusAttribute.getValue());
      }

    } catch (IOException e) {
      throw new FailedToIndexException("Failed to index", e);
    } catch (JDOMException e) {
      throw new FailedToIndexException("Failed to parse XML response", e);
    }

  }

}
