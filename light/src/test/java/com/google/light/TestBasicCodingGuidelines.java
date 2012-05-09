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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.testingutils.TestingUtils.containsAnnotation;
import static com.google.light.testingutils.TestingUtils.getAnnotationIfExists;
import static com.google.light.testingutils.TestingUtils.getClassesInPackage;
import static com.google.light.testingutils.TestingUtils.getFieldsWithRequiredAnnotation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.google.light.server.annotations.ObjectifyQueryField;
import com.google.light.server.annotations.ObjectifyQueryFieldName;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.dto.AbstractPojo;
import com.google.light.server.dto.pojo.longwrapper.AbstractTypeWrapper;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.TestingUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.Id;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class TestBasicCodingGuidelines {// extends AbstractLightServerTest {

  @Before
  public void setUp() {
    GaeTestingUtils.cheapEnvSwitch(LightEnvEnum.UNIT_TEST);
  }

  @SuppressWarnings("rawtypes")
  /**
   * This test will ensure following things :
   * 1. All files which match Maven's File selection pattern for testing are included.
   * 2. All the methods in these selected files which start with test are annotated with @Test
   * and vice versa.
   * @throws Exception
   */
  @Test
  public void test_ensureTestAnnotation() throws Exception {
    // String testPackage = "com.google.light";
    String testPackage = "com.google.light";
    List<Class> classes = TestingUtils.getClassesInPackage(testPackage, null);

    Set<Class> setOfTestClasses = getSetOfTestClasses(classes);

    // Now ensure that all the methods that start with test are annotated with @Test.
    for (Class currClass : setOfTestClasses) {
      // TODO(arjuns): Change this to getDeclaredMethods.
      for (Method currMethod : currClass.getMethods()) {
        validateMethodAnnotation(currMethod);
        validateMethodName(currMethod);
      }
    }
  }

  private void validateMethodName(Method currMethod) {
    String errorMessage = currMethod.getDeclaringClass().getSimpleName() + "#"
        + currMethod.getName() + " is tagged with " + Test.class.getName() +
        " annotation, but it does not start with test_";

    if (containsAnnotation(Test.class, currMethod.getAnnotations())) {
      assertTrue(errorMessage, currMethod.getName().startsWith("test_"));
    }
  }

  /**
   * @param currMethod
   */
  private void validateMethodAnnotation(Method currMethod) {
    if (currMethod.getName().startsWith("test")) {
      String errorMessage =
          currMethod.getDeclaringClass().getSimpleName() + "#" + currMethod.getName()
              + " is not tagged with "
              + Test.class.getName() + " annotation.";

      boolean result = TestingUtils.containsAnnotation(Test.class, currMethod.getAnnotations());
      Assert.assertTrue(errorMessage, result);
    }
  }

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

  /**
   * This test ensures that AbstractWrappers are not used inside Persistence or DTOs.
   */
  @SuppressWarnings({ "rawtypes" })
  @Test
  public void test_persistenceEntitiesAndDTOs() {
    List<Class> classes = getClassesInPackage("com.google.light", null);

    for (Class currClass : classes) {
      if (AbstractPojo.class.isAssignableFrom(currClass)) {
        validateClassFields(currClass);
      }
    }
  }

  /**
   * @param currClass
   */
  @SuppressWarnings("rawtypes")
  private void validateClassFields(Class currClass) {
    for (Field currField : currClass.getDeclaredFields()) {
      if (currField.isSynthetic() || currField.getType().isAssignableFrom(Logger.class)) {
        // for switch blocks.
        continue;
      } else if (Modifier.isStatic(currField.getModifiers())) {
        assertTrue("Invalid " + currClass.getSimpleName() + "#" + currField.getName()
            + ". Only Fields which are marked with "
            + ObjectifyQueryFieldName.class.getSimpleName()
            + " are allowed to be static fields as they are used by Objectify.",
            containsAnnotation(ObjectifyQueryFieldName.class, currField.getDeclaredAnnotations()));

        validateQueryStringAndFields(currClass, currField);
      } else {
        assertTrue(currClass.getSimpleName() + "#" + currField.getName()
            + " should be either private/protected.",
            Modifier.isPrivate(currField.getModifiers())
                || Modifier.isProtected(currField.getModifiers()));

        if (containsAnnotation(Id.class, currField.getAnnotations())) {
          assertTrue("Invalid " + currClass.getSimpleName() + "#" + currField.getName()
              + " as Id fields can be only Long or String.",
              Long.class.isAssignableFrom(currField.getType())
                  || String.class.isAssignableFrom(currField.getType()));
        } else if (containsAnnotation(Parent.class, currField.getAnnotations())) {
          assertTrue("Invalid " + currClass.getSimpleName() + "#" + currField.getName()
              + " as fields marked with Parent should be of type " + Key.class.getName(),
              Key.class.isAssignableFrom(currField.getType()));
        }

        validateWrapperIfRequired(currClass, currField);

      }
    }
  }

  @SuppressWarnings("rawtypes")
  private void validateWrapperIfRequired(Class currClass, Field currField) {
    String errorMessage = "Invalid field " + currClass.getSimpleName() + "#" + currField.getName()
        + ", as for DTOs and Persistence, un-wrapped values should be used.";
    
    assertFalse(errorMessage, AbstractTypeWrapper.class.isAssignableFrom(currField.getType()));
  }

  @SuppressWarnings("rawtypes")
  private void validateQueryStringAndFields(Class currClass, Field ofyQueryStringField) {
    String queryStringValue = getObjectifyQueryStringValue(ofyQueryStringField.getAnnotations());
    checkNotBlank(queryStringValue, currClass.getSimpleName() + "#"
        + ofyQueryStringField.getName() + " is used for ObjectifyQuery, but annotation does not "
        + "have the value which points to that field.");

    // Now find a field in current class which is annotated with @ObjectifyQueryField
    List<Field> listOfFields = getFieldsWithRequiredAnnotation(
        ObjectifyQueryField.class, currClass);
    assertTrue("Inside " + currClass.getSimpleName()
        + ", expectation is at least one field should be tagged with "
        + ObjectifyQueryField.class.getSimpleName(), listOfFields.size() > 0);

    Field requiredField = null;

    for (Field currField : listOfFields) {
      if (currField.getName().equals(queryStringValue)) {
        requiredField = currField;
        break;
      }
    }

    checkNotNull(requiredField, "Did not find any field "
        + currClass.getSimpleName() + "#" + queryStringValue);

    String queryFieldValue = getObjectifyQueryFieldValue(requiredField.getAnnotations());

    assertEquals("For class " + currClass.getSimpleName() + ", " + ofyQueryStringField.getName()
        + " does not match with annotation for " + requiredField.getName(),
        ofyQueryStringField.getName(), queryFieldValue);

  }

  private String getObjectifyQueryStringValue(Annotation[] annotations) {
    Annotation annotation = getAnnotationIfExists(ObjectifyQueryFieldName.class, annotations);
    if (annotation == null) {
      return null;
    }

    ObjectifyQueryFieldName queryString = (ObjectifyQueryFieldName) annotation;

    return queryString.value();
  }

  private String getObjectifyQueryFieldValue(Annotation[] annotations) {
    Annotation annotation = getAnnotationIfExists(ObjectifyQueryField.class, annotations);
    if (annotation == null) {
      return null;
    }

    ObjectifyQueryField queryString = (ObjectifyQueryField) annotation;

    return queryString.value();
  }
}
