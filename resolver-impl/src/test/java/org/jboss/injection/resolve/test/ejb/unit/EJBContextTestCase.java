/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
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
package org.jboss.injection.resolve.test.ejb.unit;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.resolve.naming.EnvironmentProcessor;
import org.jboss.injection.resolve.naming.ReferenceResolverResult;
import org.jboss.injection.resolve.spi.EnvironmentMetaDataVisitor;
import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.injection.resolve.test.ejb.YASessionBean;
import org.jboss.injection.resolve.test.unit.AbstractResolverTestCase;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBoss50MetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.junit.Test;

import javax.ejb.EJBContext;
import javax.naming.LinkRef;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class EJBContextTestCase
{
   private static class ResourceEnvRefResolver implements Resolver<ResourceEnvironmentReferenceMetaData, DeploymentUnit, ReferenceResolverResult>, EnvironmentMetaDataVisitor<ResourceEnvironmentReferenceMetaData>
   {
      public Class<ResourceEnvironmentReferenceMetaData> getMetaDataType()
      {
         return ResourceEnvironmentReferenceMetaData.class;
      }

      public ReferenceResolverResult resolve(DeploymentUnit unit, ResourceEnvironmentReferenceMetaData metaData)
      {
         try
         {
            // EJBTHREE-1671 : The resType class should be loaded through the container's classloader
            Class<?> type = unit.getClassLoader().loadClass(metaData.getType());
            if(EJBContext.class.isAssignableFrom(type))
            {
               return new ReferenceResolverResult(metaData.getResourceEnvRefName(), null, "java:comp/EJBContext");
            }
            throw new RuntimeException("NYI");
         }
         catch(ClassNotFoundException e)
         {
            throw new RuntimeException("Unknown resource environment reference type " + metaData.getType() + " found in " + metaData + " of " + unit, e);
         }
      }

      public Iterable<ResourceEnvironmentReferenceMetaData> getMetaData(final Environment environment)
      {
         return environment.getResourceEnvironmentReferences();
      }
   }

   @Test
   public void test1() throws Exception
   {
      // I'm too lazy to setup some metadata myself
      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      JBoss50Creator creator = new JBoss50Creator(finder);
      JBoss50MetaData metaData = creator.create(Arrays.<Class<?>>asList(YASessionBean.class));
      JBossSessionBeanMetaData beanMetaData = (JBossSessionBeanMetaData) metaData.getEnterpriseBean("YASessionBean");
      Environment environment = beanMetaData.getJndiEnvironmentRefsGroup();

      // mock a context
      DeploymentUnit unit = mock(DeploymentUnit.class);
      when(unit.getClassLoader()).thenReturn(currentThread().getContextClassLoader());

      // go
      EnvironmentProcessor<DeploymentUnit> processor = new EnvironmentProcessor<DeploymentUnit>();
      ResourceEnvRefResolver resolver = new ResourceEnvRefResolver();
      processor.addResolver(resolver);
      processor.addMetaDataVisitor(resolver);
      List<ResolverResult<?>> result = processor.process(unit, environment);
      assertEquals("org.jboss.injection.resolve.test.ejb.YASessionBean/ctx", result.get(0).getRefName());
      assertEquals("java:comp/EJBContext", ((LinkRef)result.get(0).getValue()).getLinkName());
   }
}