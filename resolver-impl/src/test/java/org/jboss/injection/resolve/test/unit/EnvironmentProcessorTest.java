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

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.resolve.naming.EnvironmentProcessor;
import org.jboss.injection.resolve.naming.ReferenceResolverResult;
import org.jboss.injection.resolve.naming.ResolutionException;
import org.jboss.injection.resolve.naming.ValueResolverResult;
import org.jboss.injection.resolve.spi.EnvironmentMetaDataVisitor;
import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.EnvironmentEntriesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test to ensure the functionality of the EnvironmentProcessor
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EnvironmentProcessorTest extends AbstractResolverTestCase
{

   @Test
   public void testResolverMatching() throws Exception
   {
      EJBReferencesMetaData referencesMetaData = new EJBReferencesMetaData();

      EJBReferenceMetaData referenceMetaData = new EJBReferenceMetaData();
      referenceMetaData.setEjbRefName("testRef");

      referencesMetaData.add(referenceMetaData);

      Environment environment = mock(Environment.class);
      when(environment.getEjbReferences()).thenReturn(referencesMetaData);

      EnvironmentProcessor<DeploymentUnit> processor = new EnvironmentProcessor<DeploymentUnit>();
      DeploymentUnit unit = mock(DeploymentUnit.class);

      processor.addMetaDataVisitor(new EnvironmentMetaDataVisitor<EJBReferenceMetaData>()
      {
         public Iterable<EJBReferenceMetaData> getMetaData(final Environment environment)
         {
            return environment.getEjbReferences();
         }

         public Class<EJBReferenceMetaData> getMetaDataType()
         {
            return EJBReferenceMetaData.class;
         }
      });

      try
      {
         processor.process(unit, environment);
         Assert.fail("Should throw exception if no Resolver can be found");
      }
      catch(ResolutionException expected)
      {
      }

      processor.addResolver(new Resolver<EJBReferenceMetaData, DeploymentUnit>()
      {
         public Class<EJBReferenceMetaData> getMetaDataType()
         {
            return EJBReferenceMetaData.class;
         }

         public ResolverResult resolve(final DeploymentUnit context, final EJBReferenceMetaData metaData)
         {
            return new ReferenceResolverResult("org.jboss.test.Bean.test", "testBean", "java:testBean");
         }
      });

      List<ResolverResult> results = processor.process(unit, environment);
      Assert.assertNotNull(results);
      Assert.assertEquals(1, results.size());
      ResolverResult result = results.get(0);
      Assert.assertEquals("org.jboss.test.Bean.test", result.getRefName());
      Assert.assertEquals("testBean", result.getBeanName());
      Assert.assertEquals("java:testBean", getPrivateField(result.getValueRetriever(), "jndiName"));
   }


   @Test
   public void testResolverWithNonConflictingEnvironmentEntries() throws Exception
   {
      EnvironmentEntriesMetaData entriesMetaData = new EnvironmentEntriesMetaData();
      EnvironmentEntryMetaData entryMetaData = new EnvironmentEntryMetaData();
      entryMetaData.setType(String.class.getName());
      entryMetaData.setValue("value");
      entryMetaData.setEnvEntryName("test");
      entriesMetaData.add(entryMetaData);

      Environment environmentOne = mock(Environment.class);
      when(environmentOne.getEnvironmentEntries()).thenReturn(entriesMetaData);
      Environment environmentTwo = mock(Environment.class);
      when(environmentTwo.getEnvironmentEntries()).thenReturn(entriesMetaData);

      EnvironmentProcessor<DeploymentUnit> processor = new EnvironmentProcessor<DeploymentUnit>();
      DeploymentUnit unit = mock(DeploymentUnit.class);

      processor.addMetaDataVisitor(new EnvironmentMetaDataVisitor<EnvironmentEntryMetaData>()
      {
         public Iterable<EnvironmentEntryMetaData> getMetaData(final Environment environment)
         {
            return environment.getEnvironmentEntries();
         }

         public Class<EnvironmentEntryMetaData> getMetaDataType()
         {
            return EnvironmentEntryMetaData.class;
         }
      });

      processor.addResolver(new Resolver<EnvironmentEntryMetaData, DeploymentUnit>()
      {
         public Class<EnvironmentEntryMetaData> getMetaDataType()
         {
            return EnvironmentEntryMetaData.class;
         }

         public ResolverResult resolve(final DeploymentUnit context, final EnvironmentEntryMetaData metaData)
         {
            return new ValueResolverResult<String>("java:comp/env/test", "testBean", metaData.getValue());
         }
      });


      List<ResolverResult> result = processor.process(unit, environmentOne, environmentTwo);
      Assert.assertEquals(1, result.size());
   }

   @Test
   public void testResolverWithConflictingEnvironmentEntries() throws Exception
   {
      EnvironmentEntriesMetaData entriesMetaData = new EnvironmentEntriesMetaData();
      EnvironmentEntryMetaData entryMetaData = new EnvironmentEntryMetaData();
      entryMetaData.setType(String.class.getName());
      entryMetaData.setValue("value");
      entryMetaData.setEnvEntryName("test");
      entriesMetaData.add(entryMetaData);

      Environment environmentOne = mock(Environment.class);
      when(environmentOne.getEnvironmentEntries()).thenReturn(entriesMetaData);

      entriesMetaData = new EnvironmentEntriesMetaData();
      entryMetaData = new EnvironmentEntryMetaData();
      entryMetaData.setType(String.class.getName());
      entryMetaData.setValue("other value");
      entryMetaData.setEnvEntryName("test");
      entriesMetaData.add(entryMetaData);

      Environment environmentTwo = mock(Environment.class);
      when(environmentTwo.getEnvironmentEntries()).thenReturn(entriesMetaData);

      EnvironmentProcessor<DeploymentUnit> processor = new EnvironmentProcessor<DeploymentUnit>();
      DeploymentUnit unit = mock(DeploymentUnit.class);

      processor.addMetaDataVisitor(new EnvironmentMetaDataVisitor<EnvironmentEntryMetaData>()
      {
         public Iterable<EnvironmentEntryMetaData> getMetaData(final Environment environment)
         {
            return environment.getEnvironmentEntries();
         }

         public Class<EnvironmentEntryMetaData> getMetaDataType()
         {
            return EnvironmentEntryMetaData.class;
         }
      });

      processor.addResolver(new Resolver<EnvironmentEntryMetaData, DeploymentUnit>()
      {
         public Class<EnvironmentEntryMetaData> getMetaDataType()
         {
            return EnvironmentEntryMetaData.class;
         }

         public ResolverResult resolve(final DeploymentUnit context, final EnvironmentEntryMetaData metaData)
         {
            return new ValueResolverResult<String>("java:comp/env/test", "testBean", metaData.getValue());
         }
      });

      try
      {
         processor.process(unit, environmentOne, environmentTwo);
         Assert.fail("Should have thrown ResolutionException based on conflicting references");
      }
      catch(ResolutionException expected)
      {
      }
   }
}
