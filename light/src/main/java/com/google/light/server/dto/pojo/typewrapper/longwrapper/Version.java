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
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import com.google.light.server.constants.LightStringConstants;
import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version.VersionDeserializer;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version.VersionSerializer;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version.VersionXmlAdapter;
import java.io.IOException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Wrapper for Version.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonSerialize(using = VersionSerializer.class)
@JsonDeserialize(using = VersionDeserializer.class)
@XmlJavaTypeAdapter(VersionXmlAdapter.class)
@XmlRootElement(name = "version")
public class Version extends AbstractTypeWrapper<Long, Version> {
  @XmlTransient
  @JsonIgnore
  public static final long LATEST_VERSION = -1L;
  @XmlTransient
  @JsonIgnore
  public static final long NO_VERSION = 0L;
  @XmlTransient
  @JsonIgnore  
  public static final long DEFAULT_NEXT_VERSION = 1L;

  public Version(String version) {
    super();
    checkNotNull(version, "version");
    if (version.equals(LightStringConstants.VERSION_LATEST_STR)) {
      setValue(-1L);
    } else {
      setValue(Long.parseLong(version));
    }
    validate();
  }
  
  public Version(Long version) {
    this(Long.toString(checkNotNull(version)));
  }

  public Version getNextVersion() {
    return new Version(getValue() + 1);
  }

  public State getState() {
    if (getValue() > 0L) {
      return State.SPECIFIC;
    } else if (getValue() == NO_VERSION) {
      return State.NO_VERSION;
    } else if (getValue() == LATEST_VERSION) {
      return State.LATEST;
    } else if (getValue() < -1L) {
      throw new IllegalArgumentException("Invalid version : " + getValue());
    }
    throw new IllegalStateException("Code should not reach here. Reached for : " + getValue());
  }

  public boolean isLatestVersion() {
    return getState() == State.LATEST;
  }

  public boolean isNoVersion() {
    return getState() == State.NO_VERSION;
  }

  public boolean isSpecificVersion() {
    return getState() == State.SPECIFIC;
  }

  @Override
  public String toString() {
    return "version:" + getValue() + ", State:" + getState();
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
    String errMsg = "Invalid version : " + getValue();

    switch (getState()) {
      case LATEST:
        checkArgument(getValue() == LATEST_VERSION,
            errMsg + "For State=LATEST_VERSION, Version should be -1L.");
        break;

      case NO_VERSION:
        checkArgument(getValue() == NO_VERSION, 
          errMsg + "For State=NO_VERSION, version should be zero.");
        break;

      case SPECIFIC:
        checkPositiveLong(getValue(), errMsg);
        break;

      default:
        throw new IllegalStateException("Invalid State[" + getState() + "] for " + errMsg);
    }
    return this;
  }

  // For Objectify and JAXB.
  @SuppressWarnings("unused")
  private Version() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Version createInstance(Long value) {
    return new Version(value);
  }
  

  /**
   * Xml Adapter for {@link Version}.
   */
  public static class VersionXmlAdapter extends XmlAdapter<Long, Version> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Version unmarshal(Long value) throws Exception {
      return new Version(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long marshal(Version value) throws Exception {
      return value.getValue();
    }
  }

  /**
   * {@link JsonDeserializer} for {@link Version}.
   */
  public static class VersionDeserializer extends JsonDeserializer<Version> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Version deserialize(JsonParser jsonParser, DeserializationContext ctxt)
        throws IOException,
        JsonProcessingException {
      String value = jsonParser.getText();
      return new Version(value);
    }
  }

  /**
   * {@link JsonSerializer} for {@link Version}.
   */
  public static class VersionSerializer extends JsonSerializer<Version> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Version value, JsonGenerator jsonGenerator, SerializerProvider provider)
        throws IOException, JsonProcessingException {
      if (value == null) {
        return;
      }
      jsonGenerator.writeString("" + value.getValue());
    }
  }
}
