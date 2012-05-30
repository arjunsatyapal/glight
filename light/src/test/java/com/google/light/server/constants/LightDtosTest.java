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
import static com.google.light.testingutils.TestingUtils.getAnnotationIfExists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.light.server.annotations.OverrideFieldAnnotationName;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.NeedsDtoValidation;
import com.google.light.server.dto.TestAbstractDto;
import com.google.light.server.dto.importresource.ImportBatchWrapper;
import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.dto.notifications.AbstractNotification;
import com.google.light.server.dto.notifications.ChildJobCompletionNotification;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode;
import com.google.light.server.dto.pojo.tree.GoogleDocTree;
import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.FTSDocumentId;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocImportBatchJobContext;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocResourceId;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.dto.thirdparty.google.youtube.YouTubePlaylistInfo;
import com.google.light.server.dto.thirdparty.google.youtube.YouTubeVideoInfo;
import com.google.light.server.jobs.handlers.collectionjobs.gdoccollection.ImportCollectionGoogleDocContext;
import com.google.light.server.jobs.handlers.collectionjobs.youtubeplaylist.ImportCollectionYouTubePlaylistContext;
import com.google.light.server.jobs.handlers.modulejobs.gdocument.ImportModuleGoogleDocJobContext;
import com.google.light.server.jobs.handlers.modulejobs.synthetic.ImportModuleSyntheticModuleJobContext;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
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
      AbstractNotification.class.getName(),
      AbstractTreeNode.class.getName(),
      AbstractTypeWrapper.class.getName(),
      ChildJobCompletionNotification.class.getName(),
      CollectionId.class.getName(),
      ContentLicense.class.getName(),
      ExternalId.class.getName(),
      FTSDocumentId.class.getName(),
      GoogleDocResourceId.class.getName(),
      GoogleDocTree.class.getName(),
      GoogleDocImportBatchJobContext.class.getName(),
      GoogleOAuth2TokenInfo.class.getName(),
      GoogleLoginTokenInfo.class.getName(),
      GoogleUserInfo.class.getName(),
      GSBlobInfo.class.getName(),
      ImportModuleGoogleDocJobContext.class.getName(),
      ImportBatchWrapper.class.getName(),
      ImportCollectionGoogleDocContext.class.getName(),
      ImportCollectionYouTubePlaylistContext.class.getName(),
      ImportModuleSyntheticModuleJobContext.class.getName(),
      JobId.class.getName(),
      ModuleId.class.getName(),
      PersonId.class.getName(),
      Version.class.getName(),
      YouTubeVideoInfo.class.getName(),
      YouTubePlaylistInfo.class.getName());

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
      // TODO(arjuns): Fix this.
      if (currClass.isEnum()) {
        continue;
      }

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
          NeedsDtoValidation.class.isAssignableFrom(currClass));

      if (!currClass.isEnum()) {
        validateClassAnnotations(currClass, lightDtoEnum);
      }

      validateMethodAnnotations(currClass);

      if (currClass.isEnum()) {
        validateEnumAnnotations(currClass);
      } else {
        validateFieldAnnotations(currClass);
      }

    }
  }

  /**
   * @param currClass
   */
  private void validateEnumAnnotations(Class currClass) {
    for (Field currEnum : currClass.getDeclaredFields()) {
      System.out.println(currEnum);
    }
  }

  private static Set<String> allowedAnnotationsForClasses = Sets.newHashSet(
      Deprecated.class.getName(),
      SuppressWarnings.class.getName(),
      JsonTypeName.class.getName(),
      JsonSerialize.class.getName(),
      JsonDeserialize.class.getName(),
      XmlAccessorType.class.getName(),
      XmlJavaTypeAdapter.class.getName(),
      XmlRootElement.class.getName(),
      XmlType.class.getName());

  @SuppressWarnings("rawtypes")
  private void validateClassAnnotations(Class currClass, LightDtos lightDtoEnum) {
    String currClassName = currClass.getSimpleName();
    if (!Modifier.isAbstract(currClass.getModifiers())) {
      // Abstract Classes do not need XmlRootElement tag.

      assertTrue(currClassName + " should be annotated with "
          + XmlRootElement.class.getSimpleName(),
          containsAnnotation(XmlRootElement.class, currClass.getAnnotations()));

      assertTrue(currClassName + " should be annotated with "
          + XmlAccessorType.class.getSimpleName(),
          containsAnnotation(XmlAccessorType.class, currClass.getAnnotations()));

      assertTrue(currClassName + " should be annotated with "
          + JsonTypeName.class.getSimpleName(),
          containsAnnotation(JsonTypeName.class, currClass.getAnnotations()));

      String rootElementName = getXmlRootElementName(currClass.getAnnotations());
      String jsonTypeName = getJsonTypeValue(currClass.getAnnotations());
      String expectedRootElementName = lightDtoEnum.getXmlRootElementName();

      checkNotBlank(rootElementName, currClassName + " cannot have blank XmlRootElement name");
      checkNotBlank(jsonTypeName, currClassName + " cannot have blank JsonTypeName value");
      assertEquals(currClassName + " root name does not match.",
          expectedRootElementName, rootElementName);

    }

    // Ensure that does not have any Json Annotation at class level.
    validateAnnotations(currClass, allowedAnnotationsForClasses);
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
      JsonProperty.class.getName(),
      JsonIgnore.class.getName(),
      OverrideFieldAnnotationName.class.getName(),
      XmlAnyElement.class.getName(),
      XmlElement.class.getName(),
      XmlElementWrapper.class.getName(),
      XmlEnumValue.class.getName(),
      XmlTransient.class.getName());

  /**
   * @param field
   */
  @SuppressWarnings("rawtypes")
  private void validateFieldAnnotations(Class currClass) {
    String className = currClass.getSimpleName();

    Map<String, Field> mapOfJsonPropertyNameToField = Maps.newConcurrentMap();

    for (Field currField : currClass.getDeclaredFields()) {
      String fieldNameForErr = className + "#" + currField.getName();

      // Ensure that all the annotations are permitted.
      for (Annotation currAnnotation : currField.getAnnotations()) {
        assertTrue(fieldNameForErr + " : Annotation "
            + currAnnotation.annotationType().getSimpleName()
            + " is not allowed",
            allowedAnnotationsForFields.contains(currAnnotation.annotationType().getName()));
      }

      if (isIgnorableField(currField)) {
        continue;
      }

      String xmlElementName = null;
      String xmlElementWrapperName = null;
      /*
       * If a field is a collection, then it should have two tags for XML : One for wrapper and
       * one for elements. Third tag should be for JsonProperty. And JsonProperty should share name
       * with XmlElementWrapper. If its not a collection, then XmlElement and XmlWrapper name
       * should be same.
       */
      if (Collection.class.isAssignableFrom(currField.getType())) {
        assertTrue(fieldNameForErr + " should be annotated with "
            + XmlElementWrapper.class.getSimpleName() + " and " + XmlElement.class.getName(),
            containsAnnotation(XmlElementWrapper.class, currField.getAnnotations()));
        xmlElementWrapperName = getXmlElementWrappertName(currField.getAnnotations());
      }

      
      // Check for XmlElement/XmlElementWrapper Annotation.
      if (StringUtils.isNotBlank(xmlElementWrapperName)) {
        assertTrue(
            fieldNameForErr + " should be annotated with "
                + XmlElementWrapper.class.getSimpleName(),
            containsAnnotation(XmlElementWrapper.class, currField.getAnnotations()));
        if (!containsAnnotation(XmlElement.class, currField.getAnnotations())) {
          assertTrue(
              fieldNameForErr + " should be annotated with " + XmlAnyElement.class.getSimpleName(),
              containsAnnotation(XmlAnyElement.class, currField.getAnnotations()));
        }
      } else {

       

          assertTrue(
              fieldNameForErr + " should be annotated with " + XmlElement.class.getSimpleName(),
              containsAnnotation(XmlElement.class, currField.getAnnotations()));
          xmlElementName = getXmlElementName(currField.getAnnotations());

          LightPreconditions.checkNotBlank(xmlElementName,
              fieldNameForErr + " has empty XmlElement.");
        }

      String errorMsg =
          fieldNameForErr
              + " needs to have overriden names for both "
              + "Json and Xml and these overridden names should be same and should match fieldName."
              + "For Xml, if its a list, then use both" + XmlElementWrapper.class.getSimpleName()
              + " & "
              + XmlElement.class.getSimpleName() + " and for Json use "
              + JsonProperty.class.getSimpleName();

      assertTrue(fieldNameForErr + " should be annotated with @JsonProperty.",
          containsAnnotation(JsonProperty.class, currField.getAnnotations()));

      String jsonPropertyValue = getJsonPropertyValue(currField.getAnnotations());
      checkNotBlank(jsonPropertyValue, errorMsg);

      if (xmlElementWrapperName != null) {
        assertEquals(errorMsg, xmlElementWrapperName, jsonPropertyValue);
      } else {
        assertEquals(errorMsg, xmlElementName, jsonPropertyValue);
      }

      if (!containsAnnotation(OverrideFieldAnnotationName.class, currField.getAnnotations())) {
        assertEquals(errorMsg, jsonPropertyValue, currField.getName());
      }

      // Now ensure that it is unqiue in this class so that deserialization can work.
      Field tempField = mapOfJsonPropertyNameToField.get(jsonPropertyValue);
      if (tempField != null) {
        String dupErrMsg = "For class " + currClass.getSimpleName()
            + ", Found more then one field annotated with " + jsonPropertyValue + " and they are "
            + "#" + tempField.getName() + " and #" + currField.getName();

        throw new IllegalArgumentException(dupErrMsg);
      } else {
        mapOfJsonPropertyNameToField.put(jsonPropertyValue, currField);
      }
    }
  }

  private String getXmlRootElementName(Annotation[] annotations) {
    Annotation tempAnnotation =
        TestingUtils.getAnnotationIfExists(XmlRootElement.class, annotations);

    if (tempAnnotation == null) {
      return null;
    }

    XmlRootElement element = (XmlRootElement) tempAnnotation;
    return element.name();
  }

  private String getXmlElementWrappertName(Annotation[] annotations) {
    Annotation tempAnnotation = getAnnotationIfExists(XmlElementWrapper.class, annotations);

    if (tempAnnotation == null) {
      return null;
    }

    XmlElementWrapper element = (XmlElementWrapper) tempAnnotation;
    return element.name();
  }

  private String getXmlElementName(Annotation[] annotations) {
    Annotation tempAnnotation = getAnnotationIfExists(XmlElement.class, annotations);

    if (tempAnnotation == null) {
      return null;
    }

    XmlElement element = (XmlElement) tempAnnotation;
    return element.name();
  }

  private String getJsonPropertyValue(Annotation[] annotations) {
    Annotation tempAnnotation = getAnnotationIfExists(JsonProperty.class, annotations);

    if (tempAnnotation == null) {
      return null;
    }

    JsonProperty property = (JsonProperty) tempAnnotation;
    return property.value();
  }

  private String getJsonTypeValue(Annotation[] annotations) {
    Annotation tempAnnotation = TestingUtils.getAnnotationIfExists(JsonTypeName.class, annotations);

    if (tempAnnotation == null) {
      return null;
    }

    JsonTypeName typeName = (JsonTypeName) tempAnnotation;
    return typeName.value();
  }

  private boolean isIgnorableField(Field field) {
    if (field.isSynthetic() || Modifier.isTransient(field.getModifiers())) {
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
        NeedsDtoValidation.class.isAssignableFrom(currClass)) {
      return true;
    }

    return false;
  }

  /**
   * This test ensures that all the Dtos are tagged with Unique {@link XmlRootElement#name()}. This
   * is required otherwise JAXB gets confused while deserializing.
   */
  @Test
  public void test_uniqueXmlRootElement() {
    Map<String, LightDtos> map = Maps.newHashMap();

    for (LightDtos currDto : LightDtos.values()) {
      // Ignoring abstract classes.
      if (currDto.getClazz().getSimpleName().startsWith("Abstract")) {
        continue;
      }

      String rootElementName = currDto.getXmlRootElementName();
      if (map.keySet().contains(rootElementName)) {
        fail("More then one is tagged with " + rootElementName + " and are : " + currDto + ", "
            + map.get(rootElementName));
      } else {
        map.put(rootElementName, currDto);
      }
    }
  }
}
