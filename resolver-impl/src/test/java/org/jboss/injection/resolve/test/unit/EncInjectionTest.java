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
package org.jboss.injection.resolve.test.unit;

import org.jboss.injection.inject.InjectorFactory;
import org.jboss.injection.resolve.enc.EncInjectionPoint;
import org.jboss.injection.resolve.enc.EncPopulator;
import org.jboss.injection.resolve.enc.LinkRefValueRetriever;
import org.jboss.injection.inject.spi.Injector;
import org.jboss.injection.resolve.test.support.SimpleValueRetriever;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import java.util.Arrays;

/**
 * Basic test to verify EncInjection function as expected.
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EncInjectionTest extends AbstractResolverTestCase {

   private Context context;

   @Before
   public void setUp() throws Exception {
      context = new InitialContext();
   }

   @Test
   public void testEncInjection() throws Exception {
      Injector<Context> injector = InjectorFactory.create(new EncInjectionPoint<String>("java:test"), new SimpleValueRetriever("Test Value"));

      injector.inject(context);

      assertContextValue("java:test", "Test Value");
   }

   @Test
   public void testEncLinkInjection() throws Exception {
      context.rebind("java:test", "Test Value");

      Injector<Context> injector = InjectorFactory.create(new EncInjectionPoint<LinkRef>("java:comp/test"), new LinkRefValueRetriever("java:test"));

      assertNameNotFound("java:comp/test");
      injector.inject(context);

      assertContextValue("java:comp/test", "Test Value");
   }

   @Test
   public void testEncPopulator() throws Exception {
      Injector<Context> injectorOne = InjectorFactory.create(
            new EncInjectionPoint<String>("java:testOne"),
            new SimpleValueRetriever("Test Value One"));
      Injector<Context> injectorTwo = InjectorFactory.create(
            new EncInjectionPoint<String>("java:testTwo"),
            new SimpleValueRetriever("Test Value Two"));
      Injector<Context> injectorThree = InjectorFactory.create(
            new EncInjectionPoint<String>("java:testThree"),
            new SimpleValueRetriever("Test Value Three"));

      EncPopulator encPopulator = new EncPopulator(context, Arrays.asList(injectorOne, injectorTwo,injectorThree));

      assertNameNotFound("java:testOne");
      assertNameNotFound("java:testTwo");
      assertNameNotFound("java:testThree");

      encPopulator.start();

      assertContextValue("java:testOne", "Test Value One");
      assertContextValue("java:testTwo", "Test Value Two");
      assertContextValue("java:testThree", "Test Value Three");
   }

   private void assertContextValue(String jndiName, Object value) throws Exception {
      String actual = (String)context.lookup(jndiName);
      Assert.assertEquals(value, actual);
   }

   private void assertNameNotFound(String name) {
      try {
         context.lookup(name);
         Assert.fail("The name should not be found in the context: " + name);
      } catch(NamingException expected) {
      }
   }
}