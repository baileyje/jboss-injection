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

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.DeploymentException;
import org.junit.Assert;
import org.junit.Test;

/**
 * BasicSwitchBoardOperatorDeployerTestCase -
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class BasicSwitchBoardOperatorDeployerTestCase extends AbstractSwitchBoardOperatorDeployerTestCase
{
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
      } catch(DeploymentException expected)
      {
         // TODO: Make sure this is due to the missing dep
      }
   }

   @Test
   public void testDeployWithMcDependencyAlreadyMet() throws Throwable
   {
      context.rebind("java:test", "Test Value");
      Deployment deployment = createDeployment("test1");
      attachMetaData(deployment);

      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaData.class, BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
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

      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaData.class, BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
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
      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaData.class, BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNameNotFound("java:otherTest");
      deploy(envDeployment, dependencyDeployment);
      assertContextValue("java:otherTest", "Test Value");
      unbind("java:otherTest", "java:test");
      undeploy(envDeployment, dependencyDeployment);
   }

   protected abstract void attachMetaData(Deployment deployment);
}
