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
package com.google.light.server.dto.pojo;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNull;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import com.google.light.server.constants.LightStringConstants;
import com.google.light.server.dto.AbstractPojo;

/**
 * Wrapper for Version.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class Version extends AbstractPojo<Version>{
  /**
   * Version should be either null, or a positive value.
   */
  private Long version;
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
    this.version = version;
    this.state = state;
    validate();
  }

  public Long get() {
    return version;
  }
  
  public Version getNextVersion() {
    return new Version(version + 1);
  }

  public State getState() {
    return state;
  }
  
  public boolean isLatest() {
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
    return "version:" + version + ", State:" + state;
  }

  public static enum State {
    /** Refer to Latest version. */
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
        checkNull(version, "For LatestState, Version should be null.");
        break;

      case NO_VERSION:
        checkArgument(0 == version, "For State=NO_VERSION, version should be zero.");
        break;

      case SPECIFIC:
        checkPositiveLong(version, "Invalid version.");
        break;

      default:
        throw new IllegalStateException("Invalid State[" + state + "].");
    }
    return this;
  }

  // For JAXB and Objectify.
  @SuppressWarnings("unused")
  private Version() {
  }
}
