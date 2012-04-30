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

import static com.google.light.server.utils.LightUtils.arrayToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.light.server.constants.EnumTestInterface;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Produces;
import org.junit.Test;

/**
 * Test for {@link JerseyResourcesEnum}.
 *
 * @author Arjun Satyapal
 */
public class JerseyResourcesEnumTest implements EnumTestInterface {
  /** 
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_count() throws Exception {
//    assertEquals(1, JerseyResourcesEnum.values().length);
    
    assertEquals(JerseyMethodEnum.getSetOfResources(), JerseyResourcesEnum.getSetOfResources());
    
    // Now verify that all the methods annotated with @Path are part of JerseyMethod.
    for (Class<? extends AbstractJerseyResource> clazz : JerseyMethodEnum.getSetOfResources()) {
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
        
        Preconditions.checkNotNull(myenum, "Did not find mapping for : \n"
            + "class[" + clazz + "], \nmethod[" + currMethod + "], \nParameterTypes[" + 
            Iterables.toString(Lists.newArrayList(currMethod.getParameterTypes())) + "].");

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
        }
      }
    }
  }

  /**
   * @param listOfAnnotations
   * @return
   */
  private Map<String, Annotation> convertAnnotations(List<Annotation> listOfAnnotations) {
    Map<String, Annotation> map = Maps.newHashMap();
    for (Annotation curr : listOfAnnotations) {
      map.put(curr.annotationType().getName(), curr);
    }
    
    return map;
  }
}
