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
package org.jboss.injection.inject.test.naming.unit;

import org.jboss.injection.inject.naming.ContextInjectionPoint;
import org.jboss.injection.inject.naming.LinkRefValueRetriever;
import org.jboss.injection.inject.Injector;
import org.jboss.injection.inject.pojo.GenericValueRetriever;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.LinkRef;

/**
 * Basic test to verify ContextInjection functions as expected.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class ContextInjectionTest extends AbstractNamingTestCase
{

   @Test
   public void testEncInjection() throws Exception
   {
      Injector<Context> injector = new Injector<Context>(new ContextInjectionPoint<String>("java:test"), new GenericValueRetriever<String>("Test Value"));

      injector.inject(context);

      assertContextValue("java:test", "Test Value");
   }

   @Test
   public void testEncLinkInjection() throws Exception
   {
      context.rebind("java:test", "Test Value");

      Injector<Context> injector = new Injector<Context>(new ContextInjectionPoint<LinkRef>("java:comp/test"), new LinkRefValueRetriever("java:test"));

      assertNameNotFound("java:comp/test");
      injector.inject(context);

      assertContextValue("java:comp/test", "Test Value");
   }
}