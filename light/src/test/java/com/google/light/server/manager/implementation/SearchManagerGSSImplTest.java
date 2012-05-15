package com.google.light.server.manager.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;

import java.io.InputStream;

import org.junit.Test;

import com.google.api.client.http.HttpTransport;
import com.google.common.collect.ImmutableMap;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.dto.search.SearchRequestDto;
import com.google.light.testingutils.TestingConstants;
import com.google.light.testingutils.TestingUtils;
import com.google.light.testingutils.XmlTestingUtils;

/**
 * Test for {@link SearchManagerGSSImpl}.
 * 
 * @author Walter Cacau
 */
public class SearchManagerGSSImplTest {
  private static final String SAMPLE_LANGUAGE_CODE = "en";
  private static final String GOOGLE_DTD_URL = "http://www.google.com/google.dtd";

  /**
   * Checks if {@link LightConstants.SEARCH_RESULTS_PER_PAGE} is greater
   * then 0 and if it is less then or equal to the maximum number of results
   * that CSE XML API supports.
   * 
   * @see https://developers.google.com/custom-search/docs/xml_results#numsp
   */
  @Test
  public void test_SEARCH_RESULTS_PER_PAGE_config() {
    assertTrue("LightConstants.SEARCH_RESULTS_PER_PAGE should be greater then 0",
        LightConstants.SEARCH_RESULTS_PER_PAGE > 0);
    assertTrue("LightConstants.SEARCH_RESULTS_PER_PAGE should be less then or equal to 20",
        LightConstants.SEARCH_RESULTS_PER_PAGE <= 20);
  }

  /**
   * Test for {@link SearchManagerGSSImpl#search(SearchRequestDto)}
   */
  @Test
  public void test_search() {

    for (int i = 0; i < TestingConstants.SAMPLE_GSS_LIST_QUERIES.size(); i++) {
      // Building searchManager
      InputStream fakeContent =
          TestingUtils.getResourceAsStream(TestingConstants.SAMPLE_GSS_LIST_INPUTS.get(i));
      HttpTransport mockedHttpTransport =
          TestingUtils.createHttpTransportMockForGetRequest(fakeContent);
      SearchManagerGSSImpl searchManager =
          new SearchManagerGSSImpl(TestingUtils.getMockProviderFor(mockedHttpTransport), null);

      String stringOutput =
          TestingUtils.getResourceAsString(TestingConstants.SAMPLE_GSS_LIST_OUTPUTS.get(i));

      // Deliberately comparing using the json string representation because it eases
      // debugging.
      SearchRequestDto searchRequest =
          new SearchRequestDto.Builder()
              .query(TestingConstants.SAMPLE_GSS_LIST_QUERIES.get(i))
              .clientLanguageCode(SAMPLE_LANGUAGE_CODE)
              .build();
      assertEquals(
          "Unexpected output for " + TestingConstants.SAMPLE_GSS_LIST_QUERIES.get(i) + " input",
          stringOutput,
          searchManager.search(searchRequest).toJson());
    }
  }

  /**
   * Tests if the test inputs we are using conform to Google's public DTD.
   * 
   * CSE output is not passing Google's DTD validation. Deactivating this test for now.
   * TODO(waltercacau): Follow up with CSE team to check if the problem was resolved.
   */
  @Ignore(value = "TODO(waltercacau):Fix this.")
  @Test
  public void test_sampleInputsConformToPublicDTD() throws Exception {
    for (String inputPath : TestingConstants.SAMPLE_GSS_LIST_INPUTS) {
      XmlTestingUtils.assertValidXmlAccordingToDTD(
          TestingUtils.getResourceAsStream(inputPath), GOOGLE_DTD_URL);
    }
  }
  
}
