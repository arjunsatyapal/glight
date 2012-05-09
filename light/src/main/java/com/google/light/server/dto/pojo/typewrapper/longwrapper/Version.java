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
package com.google.light.server.dto.pojo.typewrapper.longwrapper;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNull;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import com.google.light.server.constants.LightStringConstants;
import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;

/**
 * Wrapper for Version.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class Version extends AbstractTypeWrapper<Long, Version> {
  private State state;

  public Version(Long version) {
    this(version, State.SPECIFIC);
  }

  public static Version createVersion(String version) {
    if (version.equals(LightStringConstants.VERSION_LATEST_STR)) {
      return new Version(null, State.LATEST);
    }

    return new Version(Long.parseLong(version), State.SPECIFIC);
  }

  public Version(Long version, State state) {
    super(version);
    this.state = state;
    validate();
  }

  public Version getNextVersion() {
    return new Version(getValue() + 1);
  }

  public State getState() {
    return state;
  }
  
  public boolean isLatestVersion() {
    return state == State.LATEST;
  }
  
  public boolean isNoVersion() {
    return state == State.NO_VERSION;
  }
  
  public boolean isSpecificVersion() {
    return state == State.SPECIFIC;
  }
  
  @Override
  public String toString() {
    return "version:" + getValue() + ", State:" + state;
  }

  public static enum State {
    /** Refers to Latest by User */
    LATEST, 
    
    /** Refers to a state when there is no version */
    NO_VERSION,
    
    /** Refers to a specific version */
    SPECIFIC;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Version validate() {
    checkNotNull(state, "state cannot be null.");
    switch (state) {
      case LATEST:
        checkNull(getValue(), "For LatestState, Version should be null.");
        break;
        
      case NO_VERSION:
        checkArgument(0 == getValue(), "For State=NO_VERSION, version should be zero.");
        break;

      case SPECIFIC:
        checkPositiveLong(getValue(), "Invalid version.");
        break;

      default:
        throw new IllegalStateException("Invalid State[" + state + "].");
    }
    return this;
  }

  // For Objectify and JAXB.
  private Version() {
    super(null);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public Version createInstance(Long value) {
    return new Version(value);
  }
}
