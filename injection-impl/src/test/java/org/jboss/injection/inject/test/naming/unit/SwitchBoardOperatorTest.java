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

import org.jboss.injection.inject.InjectorFactory;
import org.jboss.injection.inject.naming.ContextInjectionPoint;
import org.jboss.injection.inject.naming.SwitchBoardOperator;
import org.jboss.injection.inject.spi.Injector;
import org.jboss.injection.inject.test.pojo.support.SimpleValueRetriever;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.Context;
import java.util.Arrays;

/**
 * SwitchBoardOperatorTest
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class SwitchBoardOperatorTest extends AbstractNamingTestCase {

   @Test
   public void testEncPopulator() throws Exception {
      Injector<Context> injectorOne = InjectorFactory.create(
         new ContextInjectionPoint<String>("java:testOne"),
         new SimpleValueRetriever("Test Value One"));
      Injector<Context> injectorTwo = InjectorFactory.create(
         new ContextInjectionPoint<String>("java:testTwo"),
         new SimpleValueRetriever("Test Value Two"));
      Injector<Context> injectorThree = InjectorFactory.create(
         new ContextInjectionPoint<String>("java:testThree"),
         new SimpleValueRetriever("Test Value Three"));

      SwitchBoardOperator encPopulator = new SwitchBoardOperator(Arrays.asList(injectorOne, injectorTwo, injectorThree));

      assertNameNotFound("java:testOne");
      assertNameNotFound("java:testTwo");
      assertNameNotFound("java:testThree");

      encPopulator.start();

      assertContextValue("java:testOne", "Test Value One");
      assertContextValue("java:testTwo", "Test Value Two");
      assertContextValue("java:testThree", "Test Value Three");
   }
}
