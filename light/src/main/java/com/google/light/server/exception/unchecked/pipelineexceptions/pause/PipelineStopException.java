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
package com.google.light.server.exception.unchecked.pipelineexceptions.pause;



/**
 * This indicates that there was some JSON exception while serializing/de-serializing.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class PipelineStopException extends RuntimeException {
  private final String pipelineId;
  
  public String getPipelineId() {
    return pipelineId;
  }
  
  /**
   * {@inheritDoc}
   * @param message
   */
  public PipelineStopException(String pipelineId, String message) {
      super(message);
      this.pipelineId = pipelineId;
  }

  /**
   * {@inheritDoc}
   */
  public PipelineStopException(String pipelineId, String message, Throwable cause) {
      super(message, cause);
      this.pipelineId = pipelineId;
  }

  /**
   * {@inheritDoc}
   */
  public PipelineStopException(String pipelineId, Throwable cause) {
      super(cause);
      this.pipelineId = pipelineId;
  }
}
