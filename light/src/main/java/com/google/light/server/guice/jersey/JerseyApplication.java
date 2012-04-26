package com.google.light.server.guice.jersey;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

public class JerseyApplication extends Application {
  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> set = new HashSet<Class<?>>();

    for (JerseyResourcesEnum curr : JerseyResourcesEnum.values()) {
      set.add(curr.getClazz());
    }

    // Adding JAXB Context Resolvers for JSON/XML.
    set.add(JAXBJsonContextResolver.class);
    set.add(JAXBXmlContextResolver.class);

    return set;
  }

}
