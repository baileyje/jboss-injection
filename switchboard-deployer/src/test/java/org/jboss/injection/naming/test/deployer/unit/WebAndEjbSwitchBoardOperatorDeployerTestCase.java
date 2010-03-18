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
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.injection.resolve.naming.ReferenceResolverResult;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.javaee.spec.DescriptionGroupMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.reloaded.naming.spi.JavaEEModule;
import org.jboss.util.naming.Util;
import org.junit.Assert;
import org.junit.Test;

import javax.naming.NamingException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class WebAndEjbSwitchBoardOperatorDeployerTestCase extends AbstractSwitchBoardOperatorDeployerTestCase
{
   @Test
   public void testDeployWithMcNoDependency() throws Throwable
   {
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
      undeploy(deployment);
   }

   @Test
   public void testDeployWithMcDependencyAlreadyMet() throws Throwable
   {
      bindContextValues();

      Deployment deployment = createDeployment("test1");
      attachMetaData(deployment);

      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("bean-testBean", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      Deployment dependencyDeploymentTwo = createDeployment("dependencyTwo", BeanMetaDataBuilder.createBuilder("bean-testBeanFromEjb", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      deploy(dependencyDeployment, dependencyDeploymentTwo);

      assertNoContextValues();
      deploy(deployment);
      assertContextValues();

      cleanContextValues();
      undeploy(dependencyDeployment, dependencyDeploymentTwo, deployment);
   }

   @Test
   public void testDeployWithMcDependencyInBatchOrdered() throws Throwable
   {
      bindContextValues();

      Deployment envDeployment = createDeployment("test1");
      attachMetaData(envDeployment);

Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("bean-testBean", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      Deployment dependencyDeploymentTwo = createDeployment("dependencyTwo", BeanMetaDataBuilder.createBuilder("bean-testBeanFromEjb", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNoContextValues();
      deploy(dependencyDeployment, dependencyDeploymentTwo, envDeployment);
      assertContextValues();

      cleanContextValues();
      undeploy(dependencyDeployment,dependencyDeploymentTwo, envDeployment);
   }

   @Test
   public void testDeployWithMcDependencyInBatchUnOrdered() throws Throwable
   {
      bindContextValues();

      Deployment envDeployment = createDeployment("test1");
      attachMetaData(envDeployment);

      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("bean-testBean", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      Deployment dependencyDeploymentTwo = createDeployment("dependencyTwo", BeanMetaDataBuilder.createBuilder("bean-testBeanFromEjb", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNoContextValues();
      deploy(envDeployment, dependencyDeployment, dependencyDeploymentTwo);
      assertContextValues();

      cleanContextValues();
      undeploy(dependencyDeployment, dependencyDeploymentTwo, envDeployment);
   }

   private void assertContextValues() throws Exception
   {
      assertContextValue("java:comp/env/testBean", "Test Value");
      assertContextValue("java:comp/env/testBeanFromEjb", "Test Value Two");
   }

   private void assertNoContextValues()
   {
      assertNameNotFound("java:comp/env/testBean");
      assertNameNotFound("java:comp/env/testBeanFromEjb");
   }

   protected void attachMetaData(Deployment deployment)
   {
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      EJBReferencesMetaData referencesMetaData = new EJBReferencesMetaData();
      EJBReferenceMetaData referenceMetaData = new EJBReferenceMetaData();
      referenceMetaData.setEjbRefName("testBean");
      referencesMetaData.add(referenceMetaData);

      Environment environment = mock(Environment.class);
      when(environment.getEjbReferences()).thenReturn(referencesMetaData);

      JBossWebMetaData jBossWebMetaData = mock(JBossWebMetaData.class);
      when(jBossWebMetaData.getJndiEnvironmentRefsGroup()).thenReturn(environment);

      DescriptionGroupMetaData descriptionGroupMetaData = mock(DescriptionGroupMetaData.class);
      when(descriptionGroupMetaData.getDisplayName()).thenReturn("Test War");

      when(jBossWebMetaData.getDescriptionGroup()).thenReturn(descriptionGroupMetaData);

      attachments.addAttachment(JBossWebMetaData.class, jBossWebMetaData);

      JBossMetaData jBossMetaData = mock(JBossMetaData.class);
      JBossEnterpriseBeanMetaData jBossEnterpriseBeanMetaData = mock(JBossEnterpriseBeanMetaData.class);
      when(jBossEnterpriseBeanMetaData.getKey()).thenReturn("testBean");
      when(jBossEnterpriseBeanMetaData.getName()).thenReturn("testBean");

      referencesMetaData = new EJBReferencesMetaData();
      referenceMetaData = new EJBReferenceMetaData();
      referenceMetaData.setEjbRefName("testBeanFromEjb");
      referencesMetaData.add(referenceMetaData);

      when(jBossEnterpriseBeanMetaData.getEjbReferences()).thenReturn(referencesMetaData);

      JBossEnterpriseBeansMetaData jBossEnterpriseBeansMetaData = new JBossEnterpriseBeansMetaData();
      jBossEnterpriseBeansMetaData.add(jBossEnterpriseBeanMetaData);

      when(jBossMetaData.getEnterpriseBeans()).thenReturn(jBossEnterpriseBeansMetaData);

      attachments.addAttachment(JBossMetaData.class, jBossMetaData);

      JavaEEModule module = mock(JavaEEModule.class);
      when(module.getContext()).thenReturn(compContext);

      BeanMetaData beanMetaData = BeanMetaDataBuilder.createBuilder("jboss.naming:module=Module", JavaEEModule.class.getName())
         .setConstructorValue(module)
         .getBeanMetaData();
      attachments.addAttachment(BeanMetaData.class.getName() + ".JavaEEModule", beanMetaData);
   }

   private void bindContextValues() throws NamingException
   {
      Util.rebind(context, "java:testBean", "Test Value");
      Util.rebind(context, "java:testBeanFromEjb", "Test Value Two");
   }

   private void cleanContextValues() throws NamingException
   {

      unbind(compContext, "env/testBean", "env/testBeanFromEjb");
      unbind(context, "java:testBean", "java:testBeanFromEjb");
   }
}
