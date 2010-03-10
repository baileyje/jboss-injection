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

import org.jboss.bootstrap.api.descriptor.BootstrapDescriptor;
import org.jboss.bootstrap.api.descriptor.UrlBootstrapDescriptor;
import org.jboss.bootstrap.api.mc.server.MCServer;
import org.jboss.bootstrap.api.mc.server.MCServerFactory;
import org.jboss.test.BaseTestCase;
import org.junit.BeforeClass;

import java.util.List;

/**
 * AbstractInjectionTestCase -
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class AbstractInjectionTestCase {

    protected static MCServer server;

    @BeforeClass
    public static void setupServer() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      server = MCServerFactory.createServer(classLoader);
      List<BootstrapDescriptor> descriptors = server.getConfiguration().getBootstrapDescriptors();
      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(AbstractInjectionTestCase.class, "/conf/bootstrap/naming.xml")));

      ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      try
      {
         server.start();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldClassLoader);
      }
    }

}
