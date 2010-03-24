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
package org.jboss.injection.naming.test.deployer.support;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.resolve.naming.ReferenceResolverResult;
import org.jboss.injection.resolve.spi.EnvironmentMetaDataVisitor;
import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.metadata.javaee.spec.AnnotatedEJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.Environment;

/**
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class MockEjbReferenceResolver implements Resolver<EJBReferenceMetaData, DeploymentUnit, ReferenceResolverResult>, EnvironmentMetaDataVisitor<EJBReferenceMetaData>
{
   public Iterable<EJBReferenceMetaData> getMetaData(final Environment environment)
   {
      return environment.getEjbReferences();
   }

   public Class<EJBReferenceMetaData> getMetaDataType()
   {
      return EJBReferenceMetaData.class;
   }

   public ReferenceResolverResult resolve(final DeploymentUnit context, final EJBReferenceMetaData metaData)
   {
      return new ReferenceResolverResult("env/" + metaData.getEjbRefName(), "binder-" + metaData.getEjbRefName(), "java:" + metaData.getLink());
   }
}