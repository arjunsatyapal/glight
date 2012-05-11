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
package com.google.light.server.guice.jersey;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightUtils.arrayToString;
import static com.google.light.testingutils.TestingUtils.containsAnnotation;
import static com.google.light.testingutils.TestingUtils.getClassesInPackage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Inject;
import com.google.light.server.constants.EnumTestInterface;
import com.google.light.server.jersey.resources.AbstractJerseyResource;

/**
 * Test for {@link JerseyResourcesEnum}.
 * 
 * TODO(arjuns): Ensure that method exist in original class.
 * 
 * @author Arjun Satyapal
 */
public class JerseyResourcesEnumTest implements EnumTestInterface {
  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_count() throws Exception {
    assertEquals(8, JerseyResourcesEnum.values().length);

    Set<Class<? extends AbstractJerseyResource>> existingSet =
        JerseyMethodEnum.getSetOfJerseyResources();
    Set<Class<? extends AbstractJerseyResource>> exhausiveSet =
        JerseyResourcesEnum.getSetOfResources();
    SetView<Class<? extends AbstractJerseyResource>> missingSet =
        Sets.difference(exhausiveSet, existingSet);

    assertEquals("You need to add Jersey Methods for following Jersey Resources : "
        + Iterables.toString(missingSet), existingSet.size(), exhausiveSet.size());

    assertEquals(JerseyMethodEnum.getSetOfJerseyResources(),
        JerseyResourcesEnum.getSetOfResources());

    // Now verify that all the methods annotated with @Path are part of JerseyMethod.
    for (Class<? extends AbstractJerseyResource> clazz : JerseyMethodEnum.getSetOfJerseyResources()) {
      Method[] methods = clazz.getMethods();

      for (Method currMethod : methods) {
        Annotation[] annotations = currMethod.getAnnotations();
        List<Annotation> listOfAnnotations = Lists.newArrayList(annotations);
        if (listOfAnnotations.size() == 0) {
          // All jersey methods have atleast two annotations.
          continue;
        }
        // TODO(arjuns): Add a better wrapper to identify if current method is a jersey method.

        JerseyMethodEnum myenum = JerseyMethodEnum.getEnum(clazz,
            currMethod.getName(), currMethod.getParameterTypes());
        checkNotNull(myenum, "Did not find mapping for : "
            + "\nclass[" + clazz.getSimpleName() + "], "
            + "\nmethod[" + currMethod.getName() + "], "
            + "\nParameterTypes[" + Iterables.toString(Lists.newArrayList(
                currMethod.getParameterTypes().getClass().getSimpleName())) + "].");

        // Now validating Method Type Annotation.
        Map<String, Annotation> mapOfAnnotation = convertAnnotations(listOfAnnotations);
        assertTrue(mapOfAnnotation.keySet().contains(myenum.getJerseyMethodAnnotation().getName()));
        assertEquals("Failed for enum : " + myenum, myenum.getPath(), JerseyUtils.getPath(myenum));

        // Now validating Method Produces.
        String producesAnnotationName = Produces.class.getName();
        if (mapOfAnnotation.keySet().contains(producesAnnotationName)) {
          Annotation annotation = mapOfAnnotation.get(producesAnnotationName);
          Produces produces = (Produces) annotation;
          assertEquals("Failed for enum : " + myenum,
              arrayToString(myenum.getProduces()),
              arrayToString(produces.value()));
        } else {
          assertEquals("If method does not have any produces Annotation, then it should have "
                + "Response as a ReturnType.", javax.ws.rs.core.Response.class, currMethod.getReturnType());
          // 
        }
      }
    }
    
    // Now verify that each declared JerseyMethodEnum exists in their corresponding resources
    for (JerseyMethodEnum jerseyMethod : JerseyMethodEnum.values()) {
      Class<? extends AbstractJerseyResource> clazz = jerseyMethod.getClazz();
      
      // This will throw NoSuchMethodException if it doesn't exist
      Method declaredMethod = clazz.getDeclaredMethod(jerseyMethod.getMethodName(), jerseyMethod.getParameterTypes());
      assertTrue("JerseyMethod "+jerseyMethod.name()+" must be public!", Modifier.isPublic(declaredMethod.getModifiers()));
      
    }
  }

  private Map<String, Annotation> convertAnnotations(List<Annotation> listOfAnnotations) {
    Map<String, Annotation> map = Maps.newHashMap();
    for (Annotation curr : listOfAnnotations) {
      map.put(curr.annotationType().getName(), curr);
    }

    return map;
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void test_jerseyResourceAnnotations() throws Exception {
    String packageName = AbstractJerseyResource.class.getPackage().getName();
    Set<String> ignoreSet = Sets.newHashSet(
        AbstractJerseyResource.class.getName());

    List<Class> classes = getClassesInPackage(packageName, null);
    List<Class> filteredClass = getJerseyResources(classes, ignoreSet);

    
    for (Class currClass : filteredClass) {
      if (ignoreSet.contains(currClass.getName())) {
        continue;
      }

      validateInjectibleConstructor(currClass);
    }
  }

  /**
   * @return
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private List<Class> getJerseyResources(List<Class> classes, Set<String> ignoreSet) {
    List<Class> filteredList = Lists.newArrayList();
    
    for (Class currClass : classes) {
      if (ignoreSet.contains(currClass.getName())) {
        continue;
      }
      
      if (containsAnnotation(Path.class, currClass.getDeclaredAnnotations())) {
        assertTrue(currClass.getSimpleName() + " should extend " + AbstractJerseyResource.class,
            AbstractJerseyResource.class.isAssignableFrom(currClass));
        filteredList.add(currClass);
        continue;
      }
      
      if (currClass.isAssignableFrom(AbstractJerseyResource.class)) {
        validateResourcePathAnnotation(currClass);
      }
    }
    
    return filteredList;
  }

  /**
   * Ensures that there is one constructor for the Guice Resource which can be used by Guice.
   */
  @SuppressWarnings("rawtypes")
  private void validateInjectibleConstructor(Class clazz) {
    Constructor[] constructors = clazz.getConstructors();

    boolean found = false;
    for (Constructor currConstructor : constructors) {
      found = containsAnnotation(Inject.class, currConstructor.getAnnotations());
      if (found) {
        return;
      }
    }

    throw new IllegalStateException("For class[" + clazz.getName()
        + ", did not find any constructor which can be injected by Guice.");
  }

  /**
   * Ensures that JerseyResource is tagged with Annotation {@link Path}.
   */
  @SuppressWarnings("rawtypes")
  private void validateResourcePathAnnotation(Class clazz) {
    String errString = Path.class.getName() + " annotation missing for class : "
        + clazz.getName();

    assertTrue(errString, containsAnnotation(Path.class, clazz.getAnnotations()));
  }
}
