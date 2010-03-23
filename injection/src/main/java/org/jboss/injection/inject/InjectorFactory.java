/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.injection.inject;

import org.jboss.injection.inject.spi.InjectionPoint;
import org.jboss.injection.inject.spi.ValueRetriever;

/**
 * Create an Injector based on an InjectionPoint and a ValueRetriever
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 *
 * @deprecated Default Injector constructor should be used instead 
 */
public class InjectorFactory
{

   /**
    * Create an Injection with the required components.
    *
    * @param injectionPoint The injection point to inject the value
    * @param valueRetriever Value retriever responsible for getting the value to inject
    * @param <T>            The injection target type
    * @param <V>            The injection value type
    * @return An injector that can inject into the target type
    *
    */
   public static <T, V> Injector<T> create(final InjectionPoint<T, V> injectionPoint, final ValueRetriever<V> valueRetriever)
   {
      return new Injector<T>(injectionPoint, valueRetriever);
   }
}
