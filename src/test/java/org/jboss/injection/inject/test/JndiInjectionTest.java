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
package org.jboss.injection.inject.test;

import org.jboss.injection.inject.InjectionFactory;
import org.jboss.injection.inject.spi.Injection;
import org.jboss.injection.inject.support.SimpleObject;
import org.jboss.injection.inject.pojo.FieldInjectionPoint;
import org.jboss.injection.inject.pojo.PojoInjector;
import org.jboss.injection.inject.jndi.JndiValueRetriever;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.lang.reflect.Field;

/**
 * Basic test to verify injection facilities using pojo targets and JNDI retrieved values.
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 */
public class JndiInjectionTest extends AbstractInjectionTestCase {

   private Context context;
   private PojoInjector injector;
   private SimpleObject simpleObject = new SimpleObject();

   @Before
   public void setUp() throws Exception {
      context = new InitialContext();
      injector = new PojoInjector();
   }

   @Test
   public void testJndiInjection() throws Exception {
      context.rebind("java:test", "Test Value");
      FieldInjectionPoint injectionPoint = new FieldInjectionPoint(SimpleObject.class.getDeclaredField("simpleProperty"));

      Injection<Object> injection = InjectionFactory.create(injector, injectionPoint, new JndiValueRetriever(context, "java:test"));
      injection.perform(simpleObject);

      Assert.assertNotNull(simpleObject.getSimpleProperty());
      Assert.assertEquals("Test Value", simpleObject.getSimpleProperty());
   }

}
