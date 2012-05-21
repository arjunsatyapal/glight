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

import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.codehaus.jackson.annotate.JsonTypeName;

import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId.JobIdDeserializer;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId.JobIdSerializer;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId.JobIdXmlAdapter;
import java.io.IOException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Wrapper for JobId.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonSerialize(using = JobIdSerializer.class)
@JsonDeserialize(using = JobIdDeserializer.class)
@XmlJavaTypeAdapter(JobIdXmlAdapter.class)
@JsonTypeName(value = "jobId")
@XmlRootElement(name = "jobId")
@XmlAccessorType(XmlAccessType.FIELD)
public class JobId extends AbstractTypeWrapper<Long, JobId> {

  public JobId(String value) {
    this(Long.parseLong(value));
  }

  /**
   * @param value
   */
  public JobId(Long value) {
    super(value);
    validate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobId validate() {
    checkPositiveLong(getValue(), "Invalid InvalidJobId");

    return this;
  }

  // For Objectify and JAXB.
  @SuppressWarnings("unused")
  private JobId() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobId createInstance(Long value) {
    return new JobId(value);
  }

  // Adding Custom Serializer/Deserializer and XmlAdapter.

  /**
   * Xml Adapter for {@link JobId}.
   */
  public static class JobIdXmlAdapter extends XmlAdapter<Long, JobId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public JobId unmarshal(Long value) throws Exception {
      return new JobId(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long marshal(JobId value) throws Exception {
      return value.getValue();
    }
  }

  /**
   * {@link JsonDeserializer} for {@link JobId}.
   */
  public static class JobIdDeserializer extends JsonDeserializer<JobId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public JobId deserialize(JsonParser jsonParser, DeserializationContext ctxt)
        throws IOException,
        JsonProcessingException {
      String value = jsonParser.getText();
      return new JobId(value);
    }
  }

  /**
   * {@link JsonSerializer} for {@link JobId}.
   */
  public static class JobIdSerializer extends JsonSerializer<JobId> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(JobId value, JsonGenerator jsonGenerator, SerializerProvider provider)
        throws IOException, JsonProcessingException {
      if (value == null) {
        return;
      }
      jsonGenerator.writeString("" + value.getValue());
    }
  }
}
