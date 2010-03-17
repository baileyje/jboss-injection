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
package org.jboss.injection.naming.test.deployer.unit;

import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.resolve.naming.ValueResolverResult;
import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.EnvironmentEntriesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * BasicSwitchBoardOperatorDeployerTestCase -
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class BasicSwitchBoardOperatorDeployerTestCase extends AbstractSwitchBoardOperatorDeployerTestCase
{
   protected Environment defaultMockEnvironment;

   @Test
   public void testDeployWithMcNoDependency() throws Throwable
   {
      context.rebind("java:test", "Test Value");

      Deployment deployment = createDeployment("test1");
      attachMetaData(deployment);
      try
      {
         deploy(deployment);
         Assert.fail("Should have thrown an exception because of a missing dependency");
      }
      catch(DeploymentException expected)
      {
         // TODO: Make sure this is due to the missing dep
      }
      undeploy(deployment);
   }

   @Test
   public void testDeployWithMcDependencyAlreadyMet() throws Throwable
   {
      context.rebind("java:test", "Test Value");
      Deployment deployment = createDeployment("test1");
      attachMetaData(deployment);

      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      deploy(dependencyDeployment);
      assertNameNotFound("java:otherTest");
      deploy(deployment);
      assertContextValue("java:otherTest", "Test Value");
      unbind("java:otherTest", "java:test");
      undeploy(dependencyDeployment, deployment);
   }

   @Test
   public void testDeployWithMcDependencyInBatchOrdered() throws Throwable
   {
      context.rebind("java:test", "Test Value");
      Deployment envDeployment = createDeployment("test1");
      attachMetaData(envDeployment);

      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNameNotFound("java:otherTest");
      deploy(dependencyDeployment, envDeployment);
      assertContextValue("java:otherTest", "Test Value");
      unbind("java:otherTest", "java:test");
      undeploy(dependencyDeployment, envDeployment);
   }

   @Test
   public void testDeployWithMcDependencyInBatchUnOrdered() throws Throwable
   {
      context.rebind("java:test", "Test Value");
      Deployment envDeployment = createDeployment("test1");
      attachMetaData(envDeployment);
      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNameNotFound("java:otherTest");
      deploy(envDeployment, dependencyDeployment);
      assertContextValue("java:otherTest", "Test Value");
      unbind("java:otherTest", "java:test");
      undeploy(envDeployment, dependencyDeployment);
   }

   @Test
   public void testNonLinkInjection() throws Throwable
   {
      Deployment deployment = createDeployment("test1");
      attachMetaData(deployment);

      EnvironmentEntriesMetaData environmentEntriesMetaData = new EnvironmentEntriesMetaData();

      EnvironmentEntryMetaData environmentEntryMetaData = new EnvironmentEntryMetaData();
      environmentEntryMetaData.setEnvEntryName("java:test");
      environmentEntryMetaData.setType("java.lang.String");
      environmentEntryMetaData.setValue("Test Value");

      environmentEntriesMetaData.add(environmentEntryMetaData);
      assertNotNull("defaultMockEnvironment must be set", defaultMockEnvironment);
      when(defaultMockEnvironment.getEnvironmentEntries()).thenReturn(environmentEntriesMetaData);

      Resolver<EnvironmentEntryMetaData, DeploymentUnit> resolver = new Resolver<EnvironmentEntryMetaData, DeploymentUnit>()
      {
         public Class<EnvironmentEntryMetaData> getMetaDataType()
         {
            return EnvironmentEntryMetaData.class;
         }

         public ResolverResult resolve(final DeploymentUnit context, final EnvironmentEntryMetaData metaData)
         {
            return new ValueResolverResult<String>(metaData.getEnvEntryName(), "mc-bean-test", metaData.getValue());
         }
      };

      deployBean(BeanMetaDataBuilder.createBuilder("envEntryMetaDataResolver", Resolver.class.getName()).setConstructorValue(resolver).getBeanMetaData());

      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());

      assertNameNotFound("java:test");
      deploy(dependencyDeployment, deployment);
      assertContextValue("java:test", "Test Value");
      undeploy(deployment, dependencyDeployment);
   }

   protected abstract void attachMetaData(Deployment deployment);
}
