package com.google.light.server.manager.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.dto.search.SearchRequestDto;
import com.google.light.server.dto.search.SearchResultDto;
import com.google.light.testingutils.TestingUtils;
import com.google.light.testingutils.TestingXmlUtils;

public class SearchManagerGSSImplITCase {

  private static final String GOOGLE_DTD_URL = "http://www.google.com/google.dtd";
  /**
   * This test query needs to be such a generic query that brings enougth results to fill the first
   * and second pages
   */
  private static final String TEST_QUERY = "Addition";
  SearchManagerGSSImpl searchManager;

  @Before
  public void setUp() {
    searchManager =
        TestingUtils.getInjectorByEnv(LightEnvEnum.DEV_SERVER, null).getInstance(
            SearchManagerGSSImpl.class);
  }

  @Test
  public void test_search() {
    for (int i = 1; i <= 2; i++) {
      SearchResultDto search =
          searchManager.search(new SearchRequestDto.Builder().query(TEST_QUERY).page(i).build());
      assertNotNull("Page " + i + " has a null items list.", search.getItems());
      assertEquals(
          "The test query ("
              + TEST_QUERY
              + ") didn't returned enough results to fill page " + i + ".",
          SearchManagerGSSImpl.RESULTS_PER_PAGE,
          search.getItems().size());
    }
  }

  /*
   * CSE output is not passing Google's DTD validation. Deactivating this test for now.
   * TODO(waltercacau): Follow up with CSE team to check if the problem was resolved.
   */
  // @Test
  public void test_sampleInputsConformToPublicDTD() throws Exception {
    for (String name : SearchManagerGSSImplTest.SAMPLE_TEST_FILES_PREFIXES) {
      TestingXmlUtils.assertValidXmlAccordingToDTD(
          TestingUtils.getResourceAsStream("/search/gss/" + name + "_input.xml"), GOOGLE_DTD_URL);
    }
  }
}
