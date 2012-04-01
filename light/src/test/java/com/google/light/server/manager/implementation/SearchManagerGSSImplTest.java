package com.google.light.server.manager.implementation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.light.server.dto.search.SearchRequestDto;
import com.google.light.testingutils.TestingUtils;

public class SearchManagerGSSImplTest {

  private static final String SAMPLE_QUERY = "query";
  static final ArrayList<String> SAMPLE_TEST_FILES_PREFIXES = Lists.newArrayList("addition_query",
      "addxition_query");
  SearchManagerGSSImpl searchManager;

  /**
   * Temp variable to hold the content that the mocked httpTransport will return.
   */
  InputStream fakeContent;

  @Before
  public void setUp() {
    final HttpTransport httpTransport = createHttpTransportMockForGetRequest();
    searchManager = new SearchManagerGSSImpl(new Provider<HttpTransport>() {
      @Override
      public HttpTransport get() {
        return httpTransport;
      }
    });
  }

  @Test
  public void test_search() {

    for (String name : SAMPLE_TEST_FILES_PREFIXES) {
      fakeContent = TestingUtils.getResourceAsStream("/search/gss/" + name + "_input.xml");

      String stringOutput =
          TestingUtils.getResourceAsString("/search/gss/" + name + "_output.json");

      // Deliberately comparing using the json string representation because it eases
      // debugging.
      assertEquals("Unexpected output for " + name + " input", stringOutput, searchManager
          .search(new SearchRequestDto.Builder().query(SAMPLE_QUERY).build()).toJson());
    }
  }

  /**
   * Utility method that will create an mocked HttpTransport which will return for a GET request an
   * HttpResponse with the content set to {@link SearchManagerGSSImplTest#fakeContent}.
   */
  private HttpTransport createHttpTransportMockForGetRequest() {
    return new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContent(fakeContent);
            return result;
          }
        };
      }
    };
  }

}
