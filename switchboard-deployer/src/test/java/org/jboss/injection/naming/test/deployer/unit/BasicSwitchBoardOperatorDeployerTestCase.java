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
import org.jboss.injection.naming.test.deployer.support.MockBean;
import org.jboss.injection.naming.test.deployer.support.OtherMockBean;
import org.jboss.util.naming.Util;
import org.junit.Assert;
import org.junit.Test;

import javax.naming.NamingException;

/**
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class BasicSwitchBoardOperatorDeployerTestCase extends AbstractSwitchBoardOperatorDeployerTestCase
{
   protected static final String OTHER_BEAN_CLASS = OtherMockBean.class.getName();
   protected static final String TEST_BEAN_CLASS = MockBean.class.getName();
   protected static final OtherMockBean OTHER_BEAN = new OtherMockBean("test");
   protected static final String OTHER_BEAN_JNDI_NAME = "java:" + OTHER_BEAN_CLASS;
   protected static final String TEST_BEAN_PROP_JNDI_NAME = TEST_BEAN_CLASS + "/otherBean";
   protected static final String TEST_BEAN_PROP_FQ_JNDI_NAME = "java:comp/env/" + TEST_BEAN_PROP_JNDI_NAME;
   protected static final String BINDER_BEAN_NAME = "binder-" + OTHER_BEAN_CLASS;

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
      bindOtherBean();

      Deployment deployment = createDeployment("test1");
      attachMetaData(deployment);

      Deployment dependencyDeployment = createBinderDeployment();
      deploy(dependencyDeployment);

      assertNameNotFound(TEST_BEAN_PROP_FQ_JNDI_NAME);
      deploy(deployment);
      assertContextValue(TEST_BEAN_PROP_FQ_JNDI_NAME, OTHER_BEAN);

      undeploy(dependencyDeployment, deployment);

      unbindJndiEntries();
   }

   @Test
   public void testDeployWithMcDependencyInBatchOrdered() throws Throwable
   {
      bindOtherBean();

      Deployment envDeployment = createDeployment("test1");
      attachMetaData(envDeployment);

      Deployment dependencyDeployment = createBinderDeployment();

      assertNameNotFound(TEST_BEAN_PROP_FQ_JNDI_NAME);
      deploy(dependencyDeployment, envDeployment);
      assertContextValue(TEST_BEAN_PROP_FQ_JNDI_NAME, OTHER_BEAN);

      undeploy(dependencyDeployment, envDeployment);

      unbindJndiEntries();
   }

   @Test
   public void testDeployWithMcDependencyInBatchUnOrdered() throws Throwable
   {
      bindOtherBean();

      Deployment envDeployment = createDeployment("test1");
      attachMetaData(envDeployment);

      Deployment dependencyDeployment = createBinderDeployment();

      assertNameNotFound(TEST_BEAN_PROP_FQ_JNDI_NAME);
      deploy(envDeployment, dependencyDeployment);
      assertContextValue(TEST_BEAN_PROP_FQ_JNDI_NAME, OTHER_BEAN);

      undeploy(envDeployment, dependencyDeployment);

      unbindJndiEntries();
   }

   protected abstract void attachMetaData(Deployment deployment);

   protected void bindOtherBean() throws Exception
   {
      Util.rebind(context, OTHER_BEAN_JNDI_NAME, OTHER_BEAN);
      assertContextValue("java:org.jboss.injection.naming.test.deployer.support.OtherMockBean", OTHER_BEAN);
   }

   protected Deployment createBinderDeployment()
   {
      return createDeployment("binder", BeanMetaDataBuilder.createBuilder(BINDER_BEAN_NAME, OTHER_BEAN_CLASS).getBeanMetaData());
   }

   protected void unbindJndiEntries() throws NamingException
   {
      unbind(context, TEST_BEAN_PROP_FQ_JNDI_NAME);
      unbind(context, OTHER_BEAN_JNDI_NAME);
   }
}
