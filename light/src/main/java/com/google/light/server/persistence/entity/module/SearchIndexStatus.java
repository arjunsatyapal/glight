/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.light.server.persistence.entity.module;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.persistence.entity.module.ModuleEntity.OFY_MODULE_SEARCH_INDEX_FTS;

import com.google.light.server.annotations.ObjectifyQueryField;
import com.google.light.server.annotations.ObjectifyQueryFieldName;
import com.google.light.server.dto.AbstractPojo;
import org.joda.time.Instant;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class SearchIndexStatus extends AbstractPojo<SearchIndexStatus> {
  @ObjectifyQueryFieldName("indexForFTS")
  public static final String OFY_MODULE_INDEX_FOR_FTS_QUERY_STRING =
      OFY_MODULE_SEARCH_INDEX_FTS + "indexForFTS = ";
  @ObjectifyQueryField("OFY_MODULE_INDEX_FOR_FTS_QUERY_STRING")
  private Boolean indexForFTS;

  @ObjectifyQueryFieldName("indexForGSS")
  public static final String OFY_MODULE_INDEX_FOR_GSS_QUERY_STRING =
      OFY_MODULE_SEARCH_INDEX_FTS + "indexForGSS = ";
  @ObjectifyQueryField("OFY_MODULE_INDEX_FOR_GSS_QUERY_STRING")
  private Boolean indexForGSS;

  private Long lastVisitedByGoogleBotInMillis;

  /**
   * {@inheritDoc}
   */
  @Override
  public SearchIndexStatus validate() {
    checkNotNull(indexForFTS, "indexForFTS");
    checkNotNull(indexForGSS, "indexForGSS");

    return this;
  }

  public Boolean getIndexForFTS() {
    return indexForFTS;
  }

  public void setIndexForFTS(boolean indexForFTS) {
    this.indexForFTS = indexForFTS;
  }

  public Boolean getIndexForGSS() {
    return indexForGSS;
  }

  public void setIndexForGSS(boolean indexForGSS) {
    this.indexForGSS = indexForGSS;
  }

  public Instant getLastVisitedByGoogleBot() {
    if (lastVisitedByGoogleBotInMillis == null) {
      return null;
    }
    return new Instant(lastVisitedByGoogleBotInMillis);
  }

  public void setLastVisitedByGoogleBot(Instant lastVisitedByGoogleBot) {
    checkNotNull(lastVisitedByGoogleBot, "lastVisitedByGoogleBot");
    this.lastVisitedByGoogleBotInMillis = lastVisitedByGoogleBot.getMillis();
  }

  public static class Builder {
    private Boolean indexForFTS;
    private Boolean indexForGSS;
    private Instant lastVisitedByGoogleBot;

    public Builder indexForFTS(Boolean indexForFTS) {
      this.indexForFTS = indexForFTS;
      return this;
    }

    public Builder indexForGSS(Boolean indexForGSS) {
      this.indexForGSS = indexForGSS;
      return this;
    }

    public Builder lastVisitedByGoogleBot(Instant lastVisitedByGoogleBot) {
      this.lastVisitedByGoogleBot = lastVisitedByGoogleBot;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public SearchIndexStatus build() {
      return new SearchIndexStatus(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private SearchIndexStatus(Builder builder) {
    this.indexForFTS = builder.indexForFTS;
    this.indexForGSS = builder.indexForGSS;

    if (builder.lastVisitedByGoogleBot != null) {
      this.lastVisitedByGoogleBotInMillis = builder.lastVisitedByGoogleBot.getMillis();
    }
  }

  public static SearchIndexStatus forNewSearchableVersion = new SearchIndexStatus.Builder()
      .indexForFTS(true)
      .indexForGSS(true)
      .build();

  public static SearchIndexStatus forReserveVersion = new SearchIndexStatus.Builder()
      .indexForFTS(false)
      .indexForGSS(false)
      .build();

  // For Objectify.
  private SearchIndexStatus() {
  }
}
