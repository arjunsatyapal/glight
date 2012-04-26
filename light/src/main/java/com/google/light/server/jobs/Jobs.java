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
package com.google.light.server.jobs;

import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Job2;
import com.google.appengine.tools.pipeline.Job3;
import com.google.appengine.tools.pipeline.PromisedValue;
import com.google.appengine.tools.pipeline.Value;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.servlets.path.ServletPathEnum;
import java.util.logging.Logger;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class Jobs {
  static final Logger logger = Logger.getLogger(Jobs.class.getName());

  @SuppressWarnings("serial")
  public static class ComplexJob extends Job3<Integer, Integer, Integer, Integer> {
    @Override
    public Value<Integer> run(Integer x, Integer y, Integer z) {
      logger.info("Complex Job created with parameters x[" + x + "], y[" + y + "], z[" + z + "].");
      logger.info("Creating diffjob");
      DiffJob diffJob = new DiffJob();
      logger.info("Creating multJob");
      MultJob multJob = new MultJob();
      FutureValue<Integer> r = futureCall(diffJob, immediate(x), immediate(y));
      FutureValue<Integer> s = futureCall(diffJob, immediate(x), immediate(z));
      FutureValue<Integer> t = futureCall(multJob, r, s);
      FutureValue<Integer> u = futureCall(diffJob, t, immediate(2));
      logger.info("Complex Job completed.");
      return u;
    }
  }

  @SuppressWarnings("serial")
  public static class DiffJob extends Job2<Integer, Integer, Integer> {
    @Override
    public Value<Integer> run(Integer a, Integer b) {
      logger.info("DiffJob created with parameters a[" + a + "], b[" + b + "].");
      return immediate(a - b);
    }
  }

  @SuppressWarnings("serial")
  public static class MultJob extends Job2<Integer, Integer, Integer> {
    @Override
    public Value<Integer> run(Integer a, Integer b) {
      logger.info("MultJob created with parameters a[" + a + "], b[" + b + "].");
      return immediate(a * b);
    }
  }

  @SuppressWarnings("serial")
  public static class ExternalAgentJob extends Job0<Integer> {
    @Override
    public Value<Integer> run() {
      // Invoke ComplexJob on three promised values
      PromisedValue<Integer> x = newPromise(Integer.class);
      PromisedValue<Integer> y = newPromise(Integer.class);
      PromisedValue<Integer> z = newPromise(Integer.class);
      
      FutureValue<Integer> intermediate = futureCall(new ComplexJob(), x, y, z);

      // Kick off the process of retrieving the data from the external agent
      getIntFromUser("Please give 1st int", x.getHandle());
      getIntFromUser("Please give 2nd int", y.getHandle());
      getIntFromUser("Please give 3rd int", z.getHandle());

//      // Send the user the intermediate result and ask for one more integer
//      FutureValue<Integer> oneMoreInt =
//          futureCall(new PromptJob(), intermediate, immediate(userEmail));

      // Invoke MultJob on intermediate and oneMoreInt
//      return futureCall(new MultJob(), intermediate, oneMoreInt);
      return intermediate;
    }

    public static void getIntFromUser(String prompt, String promiseHandle) {
      StringBuilder builder = new StringBuilder();
      String url = ServletPathEnum.USER_INPUT.get() + "?" 
          + RequestParamKeyEnum.PROMISE_HANDLE.get() + "=" + promiseHandle + "&"
          + RequestParamKeyEnum.PROMISE_VALUE.get() + "=";
      builder.append("Visit this url : \nhttp://localhost:8080" + url);
      logger.info(builder.toString());
    }
  }

  @SuppressWarnings("serial")
  public static class PromptJob extends Job2<Integer, Integer, String> {
    @Override
    public Value<Integer> run(Integer intermediate, String userEmail) {
      String prompt =
          "The intermediate result is " + intermediate + "."
              + " Please give one more int";
      PromisedValue<Integer> oneMoreInt = newPromise(Integer.class);
      ExternalAgentJob.getIntFromUser(prompt, oneMoreInt.getHandle());
      return oneMoreInt;
    }
  }
}
