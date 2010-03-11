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
package org.jboss.injection.inject.test.enc.deployer.unit;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ConstructorMetaData;
import org.jboss.beans.metadata.spi.DependencyMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;
import org.jboss.injection.inject.enc.EncInjectionPoint;
import org.jboss.injection.inject.enc.EncPopulator;
import org.jboss.injection.inject.enc.LinkRefValueRetriever;
import org.jboss.injection.inject.enc.deployer.EncPopulatorDeployer;
import org.jboss.injection.inject.spi.Injector;
import org.jboss.injection.inject.test.enc.unit.AbstractEncTestCase;
import org.jboss.injection.resolve.enc.EnvironmentProcessor;
import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;
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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * EncPopulatorDeployerTest -
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EncPopulatorDeployerTest extends AbstractEncTestCase {

   @Before
   public void deployMockResolver() throws Throwable {
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder("MockResolver", Resolver.class.getName());
      Resolver<EJBReferenceMetaData> resolver = createMockResolver(EJBReferenceMetaData.class, new ResolverResult("java:test", "java:otherTest", "mc-bean-test"));
      builder.setConstructorValue(resolver);
      deployBean(builder.getBeanMetaData());
   }

   @After
   public void undeployMockResolver() throws Throwable {
      undeployBean("MockResolver");
   }

   @Test
   public void testDeployNoMc() throws Exception {
      EncPopulatorDeployer deployer = new EncPopulatorDeployer();

      deployer.setEnvironmentProcessor(getEnvironmentProcessor());

      DeploymentUnit deploymentUnit = createMockDeploymentUnit();
      Environment environment = createMockEnvironment();
      deployer.deploy(deploymentUnit, environment);

      BeanMetaData beanMetaData = deploymentUnit.getAttachment(BeanMetaData.class.getName() + "." + "jboss:service=EncPopulator,name=" + deploymentUnit.getName(), BeanMetaData.class);
      Assert.assertNotNull(beanMetaData);

      ConstructorMetaData constructorMetaData = beanMetaData.getConstructor();

      ValueMetaData constructorValueMetaData = constructorMetaData.getValue();
      Object constructorValue = constructorValueMetaData.getUnderlyingValue();

      Assert.assertTrue(constructorValue instanceof EncPopulator);

      EncPopulator encPopulator = EncPopulator.class.cast(constructorValue);

      List<Injector<?>> injectors = getPrivateField(encPopulator, "injectors", List.class);
      Assert.assertNotNull(injectors);
      Assert.assertEquals(1, injectors.size());
      Injector<?> injector = injectors.get(0);
      EncInjectionPoint<LinkRefValueRetriever> injectionPoint = (EncInjectionPoint<LinkRefValueRetriever>) getPrivateField(injector, "injectionPoint", EncInjectionPoint.class);
      String encJndiName = getPrivateField(injectionPoint, "jndiName", String.class);
      Assert.assertEquals("java:otherTest", encJndiName);
      LinkRefValueRetriever valueRetriever = getPrivateField(injector, "valueRetriever", LinkRefValueRetriever.class);
      String globalJndiName = getPrivateField(valueRetriever, "jndiName", String.class);
      Assert.assertEquals("java:test", globalJndiName);

      Set<DependencyMetaData> dependencyMetaDatas = beanMetaData.getDepends();
      Assert.assertEquals(1, dependencyMetaDatas.size());
      DependencyMetaData dependencyMetaData = dependencyMetaDatas.iterator().next();

      Assert.assertEquals("mc-bean-test", dependencyMetaData.getDependency());
   }

   @Test
   public void testDeployWithMcNoDependency() throws Throwable {
      context.rebind("java:test", "Test Value");

      Deployment deployment = deployment("test1", Environment.class, createMockEnvironment());
      try {
         deploy(deployment);
         Assert.fail("Should have thrown an exception because of a missing dependency");
      } catch(DeploymentException expected) {
         // TODO: Make sure this is due to the missing dep
      }
   }

   @Test
   public void testDeployWithMcDependencyAlreadyMet() throws Throwable {
      context.rebind("java:test", "Test Value");
      Deployment deployment = deployment("test1", Environment.class, createMockEnvironment());
      deployBean(BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNameNotFound("java:otherTest");
      deploy(deployment);
      assertContextValue("java:otherTest", "Test Value");
      undeployBean("mc-bean-test");
      context.unbind("java:otherTest");
      undeploy(deployment);
   }

   @Test
   public void testDeployWithMcDependencyInBatchOrdered() throws Throwable {
      context.rebind("java:test", "Test Value");
      Deployment envDeployment = deployment("test1", Environment.class, createMockEnvironment());
      Deployment dependencyDeployment = deployment("dependency", BeanMetaData.class, BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNameNotFound("java:otherTest");
      deploy(envDeployment, dependencyDeployment);
      assertContextValue("java:otherTest", "Test Value");
      undeployBean("mc-bean-test");
      context.unbind("java:otherTest");
      undeploy(dependencyDeployment, envDeployment);
   }

   @Test
   public void testDeployWithMcDependencyInBatchUnOrdered() throws Throwable {
      context.rebind("java:test", "Test Value");
      Deployment envDeployment = deployment("test1", Environment.class, createMockEnvironment());
      Deployment dependencyDeployment = deployment("dependency", BeanMetaData.class, BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNameNotFound("java:otherTest");
      deploy(envDeployment, dependencyDeployment);
      assertContextValue("java:otherTest", "Test Value");
      undeployBean("mc-bean-test");
      context.unbind("java:otherTest");
      undeploy(envDeployment, dependencyDeployment);
   }

   private EnvironmentProcessor getEnvironmentProcessor() {
      EnvironmentProcessor environmentProcessor = new EnvironmentProcessor();
      environmentProcessor.addResolver(createMockResolver(EJBReferenceMetaData.class, new ResolverResult("java:test", "java:otherTest", "mc-bean-test")));
      return environmentProcessor;
   }

   private <M> Resolver<M> createMockResolver(final Class<M> type, final ResolverResult defaultResult) {
      return new Resolver<M>() {
         public Class<M> getMetaDataType() {
            return type;
         }

         public ResolverResult resolve(final DeploymentUnit deploymentUnit, final M metaData) {
            return defaultResult;
         }
      };
   }

   private DeploymentUnit createMockDeploymentUnit() {
      DeploymentContext deploymentContext = new AbstractDeploymentContext("deploymentUnit", "");
      DeploymentUnit deploymentUnit = new AbstractDeploymentUnit(deploymentContext);
      return deploymentUnit;
   }

   private Environment createMockEnvironment() {
      EJBReferencesMetaData referencesMetaData = new EJBReferencesMetaData();
      EJBReferenceMetaData referenceMetaData = new EJBReferenceMetaData();
      referenceMetaData.setEjbRefName("testRef");
      referencesMetaData.add(referenceMetaData);
      return new MockEnvironment(referencesMetaData);
   }

   private <T> T getPrivateField(Object object, String fieldName, Class<T> type) throws Exception {
      Field field = object.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return (T) field.get(object);
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
