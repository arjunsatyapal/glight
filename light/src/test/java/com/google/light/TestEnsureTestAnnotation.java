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
package com.google.light;

import static com.google.light.testingutils.TestingUtils.containsAnnotation;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import com.google.light.server.AbstractLightServerTest;
import com.google.light.testingutils.TestingUtils;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import org.junit.Test;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class TestEnsureTestAnnotation extends AbstractLightServerTest {
  @SuppressWarnings("rawtypes")
  @Test
  public void test_ensureTestAnnotation() throws Exception {
    // String testPackage = "com.google.light";
    String testPackage = "com.google.light.server.implementation";
    List<Class> classes = TestingUtils.getClassesInPackage(testPackage, null);

    Set<Class> setOfTestClasses = getSetOfTestClasses(classes);

    StringBuilder builder = new StringBuilder();
    // Now ensure that all the methods that start with test are annotated with @Test.
    for (Class currClass : setOfTestClasses) {
      for (Method currMethod : currClass.getMethods()) {
        validateMethodAnnotation(currMethod, builder);
      }
    }
    assertEquals(builder.toString(), 0, builder.toString().length());
    
    for (Class currClass : setOfTestClasses) {
      for (Method currMethod : currClass.getMethods()) {
        validateMethodName(currMethod, builder);
      }
    }
    assertEquals(builder.toString(), 0, builder.toString().length());
  }

  private void validateMethodName(Method currMethod, StringBuilder builder) {
    String errorMessage = currMethod.getDeclaringClass().getSimpleName() + "#" 
        + currMethod.getName() + " is tagged with " + Test.class.getName() + 
        " annotation, but it does not start with test_";
    if (containsAnnotation(Test.class, currMethod.getAnnotations())) {
      if (currMethod.getName().startsWith("test_")) {
        return;
      } else {
        builder.append("\n").append(errorMessage);
      }
    }
  }

  /**
   * @param currMethod
   */
  private void validateMethodAnnotation(Method currMethod, StringBuilder builder) {
    if (currMethod.getName().startsWith("test")) {
      String errorMessage =
          currMethod.getDeclaringClass().getSimpleName() + "#" + currMethod.getName()
              + " is not tagged with "
              + Test.class.getName() + " annotation.";
      
      if (!containsAnnotation(Test.class, currMethod.getAnnotations())) {
        builder.append("\n").append(errorMessage);
      }
    }
  }

  /**
   * @param classes
   * @return
   */
  @SuppressWarnings("rawtypes")
  private Set<Class> getSetOfTestClasses(List<Class> classes) {
    Set<Class> set = Sets.newHashSet();

    for (Class currClass : classes) {
      // Ignore abstract/interface classes.
      int modifier = currClass.getModifiers();
      if (Modifier.isAbstract(modifier)
          || Modifier.isInterface(modifier)) {
        continue;
      }

      if (isTestClass(currClass)) {
        set.add(currClass);
      }
    }

    return set;
  }

  /**
   * @param currClass
   * @return
   */
  @SuppressWarnings("rawtypes")
  private boolean isTestClass(Class currClass) {
    String className = currClass.getName().toLowerCase();
    // See : http://maven.apache.org/plugins/maven-surefire-plugin/examples/inclusion-exclusion.html
    // See : http://maven.apache.org/plugins/maven-failsafe-plugin/examples/inclusion-exclusion.html
    if (className.startsWith("test")
        || className.endsWith("test")
        || className.endsWith("testcase")
        || className.startsWith("it")
        || className.endsWith("it")
        || className.endsWith("itcase")) {
      return true;
    }

    return false;
  }
}
