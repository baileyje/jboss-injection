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
package org.jboss.injection.inject.test.enc.unit;

import org.jboss.injection.inject.InjectorFactory;
import org.jboss.injection.inject.test.unit.AbstractInjectionTestCase;
import org.jboss.injection.inject.enc.EncInjectionPoint;
import org.jboss.injection.inject.enc.EncPopulator;
import org.jboss.injection.inject.enc.LinkRefValueRetriever;
import org.jboss.injection.inject.spi.Injector;
import org.jboss.injection.inject.test.support.SimpleValueRetriever;
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
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EncInjectionTest extends AbstractEncTestCase {

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
}