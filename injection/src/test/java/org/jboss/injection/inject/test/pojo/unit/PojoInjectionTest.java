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
package org.jboss.injection.inject.test.pojo.unit;

import org.jboss.injection.inject.Injector;
import org.jboss.injection.inject.test.pojo.support.SimpleObject;
import org.jboss.injection.inject.test.pojo.support.SimpleValueRetriever;
import org.jboss.injection.inject.pojo.FieldInjectionPoint;
import org.jboss.injection.inject.pojo.MethodInjectionPoint;
import org.jboss.injection.inject.spi.InjectionPoint;
import org.junit.Test;
import org.junit.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Basic test to verify injection facilities using pojo targets and values.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class PojoInjectionTest
{
   private SimpleValueRetriever<Object> valueRetriever = new SimpleValueRetriever<Object>("Test Value");
   private SimpleObject simpleObject = new SimpleObject();

   @Test
   public void testFieldInjection() throws Exception
   {
      InjectionPoint<Object, Object> injectionPoint = new FieldInjectionPoint(SimpleObject.class.getDeclaredField("simpleProperty"));
      Injector<Object> injector = new Injector<Object>(injectionPoint, valueRetriever);
      injector.inject(simpleObject);

      Assert.assertNotNull(simpleObject.getSimpleProperty());
      Assert.assertEquals("Test Value", simpleObject.getSimpleProperty());
   }

   @Test
   public void testMethodInjection() throws Exception
   {
      InjectionPoint injectionPoint = new MethodInjectionPoint(SimpleObject.class.getDeclaredMethod("setSimpleProperty", String.class));
      Injector<Object> injector = new Injector<Object>(injectionPoint, valueRetriever);
      injector.inject(simpleObject);

      Assert.assertNotNull(simpleObject.getSimpleProperty());
      Assert.assertEquals("Test Value", simpleObject.getSimpleProperty());
   }

   @Test
   public void testMethodInjectionInvalidMethod() throws Exception
   {
      Method method = SimpleObject.class.getDeclaredMethod("getSimpleProperty");
      try
      {
         new MethodInjectionPoint(method);
         Assert.fail("Should not be able to create a MethodInjectionPoint for a not setter method");
      } catch(AssertionError expected)
      {
      }
   }

   @Test
   public void testInjectionPointReuse() throws Exception
   {
      // Setup common injection point and injector
      Field simpleField = SimpleObject.class.getDeclaredField("simpleProperty");
      InjectionPoint injectionPoint = new FieldInjectionPoint(simpleField);


      // Reuse same injection point for multiple targets
      Injector<Object> injector = new Injector<Object>(injectionPoint, new SimpleValueRetriever("Test Value One"));
      injector.inject(simpleObject);
      org.junit.Assert.assertEquals("Test Value One", simpleObject.getSimpleProperty());

      simpleObject = new SimpleObject();
      injector = new Injector<Object>(injectionPoint, new SimpleValueRetriever("Test Value Two"));
      injector.inject(simpleObject);
      org.junit.Assert.assertEquals("Test Value Two", simpleObject.getSimpleProperty());

      simpleObject = new SimpleObject();
      injector = new Injector<Object>(injectionPoint, new SimpleValueRetriever("Test Value Three"));
      injector.inject(simpleObject);
      org.junit.Assert.assertEquals("Test Value Three", simpleObject.getSimpleProperty());

      simpleObject = new SimpleObject();
      injector = new Injector<Object>(injectionPoint, new SimpleValueRetriever("Test Value Four"));
      injector.inject(simpleObject);
      org.junit.Assert.assertEquals("Test Value Four", simpleObject.getSimpleProperty());
   }
}
