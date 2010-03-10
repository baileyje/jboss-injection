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

import org.jboss.injection.resolve.enc.EnvironmentProcessor;
import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.injection.resolve.test.support.PassThroughResolver;
import org.jboss.metadata.javaee.spec.AnnotatedEJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.DataSourceMetaData;
import org.jboss.metadata.javaee.spec.DataSourcesMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferencesMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.EnvironmentEntriesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbacksMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferencesMetaData;
import org.jboss.metadata.javaee.spec.PersistenceContextReferenceMetaData;
import org.jboss.metadata.javaee.spec.PersistenceContextReferencesMetaData;
import org.jboss.metadata.javaee.spec.PersistenceUnitReferenceMetaData;
import org.jboss.metadata.javaee.spec.PersistenceUnitReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferencesMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferencesMetaData;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test to ensure the functionality of the EnvironmentProcessor
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EnvironmentProcessorTest {

   @Test
   public void testResolverMatching() throws Exception {
      EJBReferencesMetaData referencesMetaData = new EJBReferencesMetaData();

      EJBReferenceMetaData referenceMetaData = new EJBReferenceMetaData();
      referenceMetaData.setEjbRefName("testRef");

      referencesMetaData.add(referenceMetaData);

      Environment environment = new MockEnvironment(referencesMetaData);

      Map<Class<?>, Resolver<?>> resolvers = new HashMap<Class<?>, Resolver<?>>();

      EnvironmentProcessor processor = new EnvironmentProcessor(resolvers);

      try {
         processor.process(environment);
         Assert.fail("Should throw exception if no Resolver can be found");
      } catch(IllegalStateException expected){}  

      resolvers.put(EJBReferenceMetaData.class, new PassThroughResolver<EJBReferenceMetaData>("testBean", "java:testBean"));

      List<ResolverResult> results = processor.process(environment);
      Assert.assertNotNull(results);
      Assert.assertEquals(1, results.size());
      Assert.assertEquals("testBean", results.get(0).getBeanName());
      Assert.assertEquals("java:testBean", results.get(0).getJndiName());
   }

   private static class MockEnvironment implements Environment {

      private final EJBReferencesMetaData ejbReferencesMetaData;

      private MockEnvironment(final EJBReferencesMetaData ejbReferencesMetaData) {
         this.ejbReferencesMetaData = ejbReferencesMetaData;
      }

      public DataSourceMetaData getDataSourceByName(final String name) {
         return null;
      }

      public EJBLocalReferencesMetaData getEjbLocalReferences() {
         return null;
      }

      public EJBLocalReferenceMetaData getEjbLocalReferenceByName(final String name) {
         return null;
      }

      public PersistenceContextReferencesMetaData getPersistenceContextRefs() {
         return null;
      }

      public PersistenceContextReferenceMetaData getPersistenceContextReferenceByName(final String name) {
         return null;
      }

      public DataSourcesMetaData getDataSources() {
         return null;
      }

      public EnvironmentEntriesMetaData getEnvironmentEntries() {
         return null;
      }

      public EnvironmentEntryMetaData getEnvironmentEntryByName(final String name) {
         return null;
      }

      public EJBReferencesMetaData getEjbReferences() {
         return ejbReferencesMetaData;
      }

      public AnnotatedEJBReferencesMetaData getAnnotatedEjbReferences() {
         return null;
      }

      public EJBReferenceMetaData getEjbReferenceByName(final String name) {
         return null;
      }

      public ServiceReferencesMetaData getServiceReferences() {
         return null;
      }

      public ServiceReferenceMetaData getServiceReferenceByName(final String name) {
         return null;
      }

      public ResourceReferencesMetaData getResourceReferences() {
         return null;
      }

      public ResourceReferenceMetaData getResourceReferenceByName(final String name) {
         return null;
      }

      public ResourceEnvironmentReferencesMetaData getResourceEnvironmentReferences() {
         return null;
      }

      public ResourceEnvironmentReferenceMetaData getResourceEnvironmentReferenceByName(final String name) {
         return null;
      }

      public MessageDestinationReferencesMetaData getMessageDestinationReferences() {
         return null;
      }

      public MessageDestinationReferenceMetaData getMessageDestinationReferenceByName(final String name) {
         return null;
      }

      public LifecycleCallbacksMetaData getPostConstructs() {
         return null;
      }

      public LifecycleCallbacksMetaData getPreDestroys() {
         return null;
      }

      public PersistenceUnitReferencesMetaData getPersistenceUnitRefs() {
         return null;
      }

      public PersistenceUnitReferenceMetaData getPersistenceUnitReferenceByName(final String name) {
         return null;
      }
   }

}
