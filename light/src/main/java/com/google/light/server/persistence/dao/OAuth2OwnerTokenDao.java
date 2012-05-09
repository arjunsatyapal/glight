/*
 * Copyright (C) Google Inc.
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
package com.google.light.server.persistence.dao;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.GuiceUtils.getProvider;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;

import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;

import com.google.light.server.persistence.entity.person.PersonEntity;


import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.dto.pojo.RequestScopedValues;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import java.util.List;

/**
 * DAO for {@link OAuth2OwnerTokenEntity}.
 * 
 * @author Arjun Satyapal
 */
public class OAuth2OwnerTokenDao extends AbstractBasicDao<OAuth2OwnerTokenDto, OAuth2OwnerTokenEntity> {
  static {
    ObjectifyService.register(OAuth2OwnerTokenEntity.class);
  }

  private Provider<RequestScopedValues> requestScopedValuesProvider;

  @Inject
  public OAuth2OwnerTokenDao(Provider<RequestScopedValues> requestScopedValuesProvider) {
    super(OAuth2OwnerTokenEntity.class);
    requestScopedValuesProvider = checkNotNull(requestScopedValuesProvider, 
        "requestScopedValuesProvider");
  }

  /**
   * Fetch OAuth2OwnerTokenEntity by ProviderUserId. The underlying assumption is that only one
   * entry exists with combination of {@link OAuth2ProviderService} and providerUserId.
   * <p>
   * It does not matter if providerUserId is reused by different Provider.
   * 
   * TODO(arjuns) : Add test for this.
   * 
   * @param email
   * @return
   */
  public OAuth2OwnerTokenEntity getByProviderServiceAndProviderUserId(
      OAuth2ProviderService providerService, String providerUserId) {
    Objectify ofy = ObjectifyUtils.nonTransaction();

    Query<OAuth2OwnerTokenEntity> query = ofy.query(OAuth2OwnerTokenEntity.class)
        .filter(OAuth2OwnerTokenEntity.OFY_PROVIDER_USER_ID, providerUserId);

    List<OAuth2OwnerTokenEntity> ownerTokens = Lists.newArrayList(query.iterator());

    // TODO(arjuns): Add test for this.
    if (ownerTokens == null || ownerTokens.size() <= 0) {
      return null;
    }

    /*
     * First validate that all the records returned belongs to same person. Under following
     * circumstances (though very rare), it is possible that Light has more then one PersonEntity
     * with same providerUserId.
     * 1. Due to some kind of race condition, Light ended up creating two PersonEntity for same
     * real Human with same ProviderUserId for same Provider.
     * 2. Provider has a bug and two different Persons end up sharing same PersonId. If this happens
     * then Light will be in big trouble as Light is relying on Providers with the assumption that
     * Providers will not reuse PersonIds.
     * NOTE : Two different providers are allowed to have same providerUserId. e.g. Google and
     * Yahoo can reuse same providerUserId. And they can be two different persons. Light's
     * requirement is that combination of Provider & ProviderUserId is unique.
     */
    PersonId currPersonId = null;
    for (int index = 0; index < ownerTokens.size() - 1; index++) {
      OAuth2OwnerTokenEntity currToken = ownerTokens.get(index);
      currPersonId = currToken.getPersonId();
      checkNotNull(currPersonId, "PersonId is missing.");
      checkPersonId(currPersonId);

      for (int futureIndex = index + 1; futureIndex < ownerTokens.size(); futureIndex++) {
        OAuth2OwnerTokenEntity otherToken = ownerTokens.get(futureIndex);
        PersonId otherPersonId = otherToken.getPersonId();

        if (currPersonId == otherPersonId) {
          /*
           * Same person can have multiple providerService for same providerUserId. e.g.
           * A person can have GOOGLE_DOC and GOOGLE_SITES as providerServices for same provider
           * GOOGLE.
           */
          continue;
        }
        
        if (currToken.getProviderService() == otherToken.getProviderService()) {
          /*
           * Though we may optimize and not fail by chekcing if the currently asked
           * providerService is different from one for which this error has occurred. But at
           * present we want to find out these potential issues, and therefore we will fail
           * even if we may have proceeded.
           */
          String errMessage = "Two different Persons [" + currPersonId
              + ", " + otherPersonId + "] share same providerUserId[" + providerUserId
              + "for providerService[" + providerService + "].";
          throw new IllegalStateException(errMessage);
        }
      }
    }

    /*
     * Objectify does not allow filtering on Id field for child Entities. Since
     * OAuth2OwnerTokenEntity is child of PersonEntity, so we cannot apply filter on
     * providerServiceName. So now we have to fetch all the records for the given providerUserId
     * and then filter for the record we are looking.
     */

    if (ownerTokens != null && ownerTokens.size() > 0) {
      for (OAuth2OwnerTokenEntity currToken : ownerTokens) {
        if (currToken.getProviderService() == providerService) {
          return currToken;
        }
      }
    }

    return null;
  }

  /**
   * Fetch OAuth2 Owner token from Datastore using {@link OAuth2ProviderService}.
   * 
   * TODO(arjuns): Add test for this.
   * TODO(arjuns): Rename this method to get.
   * TODO(arjuns): See how more validation can be done to avoid misuse.
   * 
   * @param personId
   * @param providerService
   * @return
   */
  public OAuth2OwnerTokenEntity getByProviderService(OAuth2ProviderService providerService) {
    requestScopedValuesProvider = getProvider(RequestScopedValues.class);
    RequestScopedValues requestScopedValues = requestScopedValuesProvider.get();

    PersonEntity owner = requestScopedValues.getOwner();
    
    if (owner == null) {
      throw new PersonLoginRequiredException("Owner is not logged in.");
    }
    
    
    Key<OAuth2OwnerTokenEntity> fetchKey = OAuth2OwnerTokenEntity.generateKey(
        owner.getKey(), providerService.name());

    return super.get(fetchKey);
  }
  
  // TODO(arjuns): Add test for this.
  /**
   * Delete a OAuth2 Owner Tooken by ProviderService.
   */
  public void deleteByProviderService(OAuth2ProviderService providerService) {
    RequestScopedValues participants = getProvider(RequestScopedValues.class).get();
    
    Key<OAuth2OwnerTokenEntity> fetchKey = OAuth2OwnerTokenEntity.generateKey(
        participants.getOwner().getKey(), providerService.name());
    
    super.delete(fetchKey);
  }
}
