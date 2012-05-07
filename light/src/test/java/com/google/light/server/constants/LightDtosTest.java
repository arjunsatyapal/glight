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
package com.google.light.server.constants;

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.testingutils.TestingUtils.containsAnnotation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.annotation.XmlTransient;

import javax.xml.bind.annotation.XmlAccessorType;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.TestAbstractDto;
import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.dto.pojo.tree.TreeNode;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.servlets.oauth2.google.pojo.AbstractOAuth2TokenInfo;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleLoginTokenInfo;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleOAuth2TokenInfo;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleUserInfo;
import com.google.light.server.utils.LightPreconditions;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.TestingUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link LightDtos}
 * 
 * TODO(arjuns): Add test for this class.
 * TODO(arjuns): Add check for zero argument constructor.
 * 
 * @author Arjun Satyapal
 */
public class LightDtosTest implements EnumTestInterface {

  @Before
  public void setUp() {
    GaeTestingUtils.cheapEnvSwitch(LightEnvEnum.UNIT_TEST);
  }

  private Set<String> ignoreNameValidationSet = Sets.newHashSet(
      AbstractOAuth2TokenInfo.class.getName(),
      AbstractDtoToPersistence.class.getName(),
      GoogleDocResourceId.class.getName(),
      GoogleOAuth2TokenInfo.class.getName(),
      GoogleLoginTokenInfo.class.getName(),
      GoogleUserInfo.class.getName(),
      GSBlobInfo.class.getName(),
      TreeNode.class.getName());

  /**
   * This test ensures following things :
   * 1. If a JavaClass ends with Dto then it is included for this test.
   * 2. Such included files extend from AbstractDto Class.
   * 3. It should be included in {@link com.google.light.server.constants.JavaDtos}
   */
  @SuppressWarnings("rawtypes")
  @Test
  @Override
  public void test_count() throws Exception {
    // assertEquals("Update tests in this file as required.", 19, LightDtos.values().length);

    String testPackage = "com.google.light";
    List<Class> classes = TestingUtils.getClassesInPackage(testPackage, null);

    Set<Class> dtoClasses = getSetOfDtoClasses(classes);

    // Ensure that all the classes listed in LightDto are present in this list.
    for (Class currClass : LightDtos.getListOfDtoClasses()) {
      String className = currClass.getSimpleName();
      assertTrue(className + " was not found in calculated List.",
          dtoClasses.contains(currClass));
    }

    for (Class currClass : dtoClasses) {
      String className = currClass.getSimpleName();

      LightDtos lightDtoEnum = LightDtos.findByDtoClass(currClass);
      assertNotNull(className + " should be present in " + LightDtos.class.getName(),
          lightDtoEnum);

      if (!ignoreNameValidationSet.contains(currClass.getName())) {
        assertTrue(className + " name should end with Dto.",
            currClass.getSimpleName().endsWith("Dto"));
      }

      assertTrue(className + " should extend from " + AbstractDto.class,
          AbstractDto.class.isAssignableFrom(currClass));

      validateClassAnnotations(currClass, lightDtoEnum);
      validateMethodAnnotations(currClass);
      validateFieldAnnotations(currClass);
    }
  }

  private static Set<String> allowedAnnotationsForClasses = Sets.newHashSet(
      SuppressWarnings.class.getName(),
      XmlAccessorType.class.getName(),
      XmlRootElement.class.getName(),
      XmlType.class.getName());

  @SuppressWarnings("rawtypes")
  private void validateClassAnnotations(Class currClass, LightDtos lightDtoEnum) {
    String currClassName = currClass.getSimpleName();
    if (!Modifier.isAbstract(currClass.getModifiers())) {
      // Abstract Classes do not need XmlRootElement tag.

      assertTrue(currClassName + " should be annotated with "
          + XmlRootElement.class.getSimpleName(),
          containsAnnotation(XmlAccessorType.class, currClass.getAnnotations()));

      assertTrue(currClassName + " should be annotated with "
          + XmlRootElement.class.getSimpleName(),
          containsAnnotation(XmlRootElement.class, currClass.getAnnotations()));

      String name = getXmlRootElementName(currClass.getAnnotations());
      assertTrue(currClassName + " rootName (" + name + ") is invalid.",
          isValidName(name));

      String expectedRootElementName = lightDtoEnum.getXmlRootElementName();
      LightPreconditions.checkNotBlank(expectedRootElementName,
          currClassName + " cannot have blank rootElement name");
      assertTrue(currClassName + " does not have valid root name in LightDto.",
          isValidName(expectedRootElementName));
      assertEquals(currClassName + " root name does not match.",
          expectedRootElementName, name);

    }

    // Ensure that does not have any Json Annotation at class level.
    validateAnnotations(currClass, allowedAnnotationsForClasses);
  }

  private boolean isValidName(String name) {
    if (!StringUtils.isAllLowerCase(name.replace("_", ""))) {
      return false;
    }

    if (name.contains("-")) {
      return false;
    }

    if (name.contains("##default")) {
      return false;
    }

    return true;
  }

  @SuppressWarnings("rawtypes")
  private void validateAnnotations(Class clazz, Set<String> allowedAnnotations) {
    for (Annotation currAnnotation : clazz.getAnnotations()) {
      if (!allowedAnnotations.contains(currAnnotation.annotationType().getName())) {
        throw new RuntimeException(clazz.getSimpleName() + " has invalid annotation "
            + currAnnotation.annotationType().getSimpleName());
      }
    }
  }

  private static Set<String> allowedAnnotationsForFields = Sets.newHashSet(
      // TODO(arjuns): Fix this.
      Embedded.class.getName(),
      XmlElement.class.getName(),
      JsonProperty.class.getName());

  /**
   * @param field
   */
  @SuppressWarnings("rawtypes")
  private void validateFieldAnnotations(Class currClass) {
    String className = currClass.getSimpleName();
    for (Field currField : currClass.getDeclaredFields()) {

      if (isIgnorableField(currField)) {
        continue;
      }

      // Check for XmlElement Annotation.
      assertTrue(className + "#" + currField.getName() + " should be annotated with @XmlElement.",
          containsAnnotation(XmlElement.class, currField.getAnnotations()));

      String errorMsg =
          className + "#" + currField.getName() + " needs to have overriden names for both " 
              + "Json and Xml and these overridden names should be same.";

      String xmlElementName = getXmlElementName(currField.getAnnotations());
      checkNotBlank(xmlElementName, errorMsg);
      
      assertTrue(className + "#" + currField.getName() + " should be annotated with @JsonProperty.",
          containsAnnotation(XmlElement.class, currField.getAnnotations()));
      
      String jsonPropertyValue = getJsonPropertyValue(currField.getAnnotations());
      checkNotBlank(jsonPropertyValue, errorMsg);
      assertEquals(errorMsg, xmlElementName, jsonPropertyValue);

      // Ensure that all the annotations are permitted.
      for (Annotation currAnnotation : currField.getAnnotations()) {
        assertTrue("Annotation " + currAnnotation.annotationType() + " is not allowed",
            allowedAnnotationsForFields.contains(currAnnotation.annotationType().getName()));
      }
    }
  }

  private String getXmlRootElementName(Annotation[] annotations) {
    for (Annotation currAnnotation : annotations) {
      if (TestingUtils.isSameAnnotation(currAnnotation, XmlRootElement.class)) {
        XmlRootElement element = (XmlRootElement) currAnnotation;
        return element.name();
      }
    }

    return null;
  }

  private String getXmlElementName(Annotation[] annotations) {
    for (Annotation currAnnotation : annotations) {
      if (TestingUtils.isSameAnnotation(currAnnotation, XmlElement.class)) {
        XmlElement element = (XmlElement) currAnnotation;
        return element.name();
      }
    }

    return null;
  }

  private String getJsonPropertyValue(Annotation[] annotations) {
    for (Annotation currAnnotation : annotations) {
      if (TestingUtils.isSameAnnotation(currAnnotation, JsonProperty.class)) {
        JsonProperty property = (JsonProperty) currAnnotation;
        return property.value();
      }
    }

    return null;
  }

  private boolean isIgnorableField(Field field) {
    if (field.isSynthetic()) {
      return true;
    }

    if (field.getType().getName().equals(Logger.class.getName())) {
      return true;
    }

    if (TestingUtils.containsAnnotation(XmlTransient.class, field.getAnnotations())) {
      return true;
    }

    return false;
  }

  private static Set<String> allowedAnnotationsForMethods = Sets.newHashSet(
      Inject.class.getName(),
      Deprecated.class.getName());

  /**
   * Ensure that current method does not have any annotations.
   * 
   * @param currMethod
   */
  @SuppressWarnings("rawtypes")
  private void validateMethodAnnotations(Class currClass) {
    for (Method currMethod : currClass.getDeclaredMethods()) {
      for (Annotation currAnnotation : currMethod.getAnnotations()) {
        assertTrue("Annotation " + currAnnotation.annotationType() + " is not allowed",
            allowedAnnotationsForMethods.contains(currAnnotation.annotationType().getName()));
      }
    }
  }

  @SuppressWarnings("rawtypes")
  private Set<Class> getSetOfDtoClasses(List<Class> classes) {
    Set<Class> set = Sets.newHashSet();

    for (Class currClass : classes) {
      // Ignore abstract/interface classes.
      int modifier = currClass.getModifiers();
      if (Modifier.isInterface(modifier)
          || TestAbstractDto.class.isAssignableFrom(currClass)) {
        continue;
      }

      if (isDtoClass(currClass)) {
        set.add(currClass);
      }
    }

    return set;
  }

  @SuppressWarnings({ "rawtypes" })
  private boolean isDtoClass(Class currClass) {
    String className = currClass.getName().toLowerCase();
    if (className.endsWith("dto") ||
        AbstractDto.class.isAssignableFrom(currClass)) {
      return true;
    }

    return false;
  }
}
