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
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.EnvironmentEntriesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.util.naming.Util;
import org.junit.Assert;
import org.junit.Test;

import javax.naming.LinkRef;

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
      finally
      {
         undeploy(deployment);
      }
   }

   @Test
   public void testDeployWithMcDependencyAlreadyMet() throws Throwable
   {
      Util.rebind(context, "java:testBean", "Test Value");
      Deployment deployment = createDeployment("test1");
      attachMetaData(deployment);

      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("bean-testBean", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      deploy(dependencyDeployment);
      assertNameNotFound("java:comp/env/testBean");
      deploy(deployment);
      assertContextValue("java:comp/env/testBean", "Test Value");
      unbind(compContext, "env/testBean");
      unbind(context, "java:testBean");
      undeploy(dependencyDeployment, deployment);
   }

   @Test
   public void testDeployWithMcDependencyInBatchOrdered() throws Throwable
   {
      Util.rebind(context, "java:testBean", "Test Value");
      Deployment envDeployment = createDeployment("test1");
      attachMetaData(envDeployment);

      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("bean-testBean", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNameNotFound("java:comp/env/testBean");
      deploy(dependencyDeployment, envDeployment);
      assertContextValue("java:comp/env/testBean", "Test Value");
      unbind(compContext, "env/testBean");
      unbind(context, "java:testBean");
      undeploy(dependencyDeployment, envDeployment);
   }

   @Test
   public void testDeployWithMcDependencyInBatchUnOrdered() throws Throwable
   {
      Util.rebind(context, "java:testBean", "Test Value");
      Deployment envDeployment = createDeployment("test1");
      attachMetaData(envDeployment);
      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("bean-testBean", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNameNotFound("java:comp/env/testBean");
      deploy(envDeployment, dependencyDeployment);
      assertContextValue("java:comp/env/testBean", "Test Value");
      unbind(compContext, "env/testBean");
      unbind(context, "java:testBean");
      undeploy(envDeployment, dependencyDeployment);
   }

   @Test
   public void testNonLinkInjection() throws Throwable
   {
      Deployment deployment = createDeployment("test1");
      attachMetaData(deployment);

      EnvironmentEntriesMetaData environmentEntriesMetaData = new EnvironmentEntriesMetaData();

      EnvironmentEntryMetaData environmentEntryMetaData = new EnvironmentEntryMetaData();
      environmentEntryMetaData.setEnvEntryName("test");
      environmentEntryMetaData.setType("java.lang.String");
      environmentEntryMetaData.setValue("Test Value");

      environmentEntriesMetaData.add(environmentEntryMetaData);
      assertNotNull("defaultMockEnvironment must be set", defaultMockEnvironment);
      when(defaultMockEnvironment.getEnvironmentEntries()).thenReturn(environmentEntriesMetaData);

      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("bean-testBean", String.class.getName()).setConstructorValue("test").getBeanMetaData());

      assertNameNotFound("java:comp/env/test");
      deploy(dependencyDeployment, deployment);
      assertContextValue("java:comp/env/test", "Test Value");
      undeploy(deployment, dependencyDeployment);
   }

   protected abstract void attachMetaData(Deployment deployment);
}
