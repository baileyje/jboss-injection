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
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.injection.naming.test.deployer.support.MockInterceptor;
import org.jboss.injection.naming.test.deployer.support.OtherMockBean;
import org.jboss.injection.naming.test.deployer.support.MockBean;
import org.jboss.metadata.annotation.creator.ejb.jboss.JBoss50Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.annotation.finder.DefaultAnnotationFinder;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.spec.InterceptorBindingsMetaData;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;
import org.jboss.metadata.ejb.spec.InterceptorsMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntriesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.reloaded.naming.spi.JavaEEComponent;
import org.jboss.reloaded.naming.spi.JavaEEModule;
import org.junit.Test;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EJBSwitchBoardOperatorDeployerTest extends BasicSwitchBoardOperatorDeployerTestCase
{
   @Override
   protected void attachMetaData(Deployment deployment)
   {
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();

      AnnotationFinder<AnnotatedElement> finder = new DefaultAnnotationFinder<AnnotatedElement>();
      JBoss50Creator creator = new JBoss50Creator(finder);
      JBossMetaData jBossMetaData = creator.create(Arrays.<Class<?>>asList(MockBean.class, OtherMockBean.class));

      JBossEnterpriseBeanMetaData jBossEnterpriseBeanMetaData = jBossMetaData.getEnterpriseBean("MockBean");

      attachments.addAttachment(JBossMetaData.class, jBossMetaData);

      attachJavaEEComponents(attachments);
   }

   private void attachJavaEEComponents(final MutableAttachments attachments)
   {
      JavaEEComponent component = mock(JavaEEComponent.class);
      when(component.getContext()).thenReturn(compContext);

      BeanMetaData beanMetaData = BeanMetaDataBuilder.createBuilder("jboss.naming:module=Module,component=MockBean", JavaEEComponent.class.getName())
         .setConstructorValue(component)
         .getBeanMetaData();
      attachments.addAttachment(BeanMetaData.class.getName() + ".JavaEEComponent", beanMetaData);


      JavaEEComponent otherComponent = mock(JavaEEComponent.class);
      when(otherComponent.getContext()).thenReturn(compContext);
      beanMetaData = BeanMetaDataBuilder.createBuilder("jboss.naming:module=Module,component=OtherBean", JavaEEComponent.class.getName())
         .setConstructorValue(otherComponent)
         .getBeanMetaData();
      attachments.addAttachment(BeanMetaData.class.getName() + ".OtherJavaEEComponent", beanMetaData);

      JavaEEModule module = mock(JavaEEModule.class);
      when(module.getContext()).thenReturn(moduleContext);

      beanMetaData = BeanMetaDataBuilder.createBuilder("jboss.naming:module=Module", JavaEEModule.class.getName())
         .setConstructorValue(module)
         .getBeanMetaData();
      attachments.addAttachment(BeanMetaData.class.getName() + ".JavaEEModule", beanMetaData);
   }

   @Test
   public void testNonLinkInjection() throws Throwable
   {
      Deployment deployment = createDeployment("test1");

      EnvironmentEntriesMetaData environmentEntriesMetaData = new EnvironmentEntriesMetaData();

      EnvironmentEntryMetaData environmentEntryMetaData = new EnvironmentEntryMetaData();
      environmentEntryMetaData.setEnvEntryName("test");
      environmentEntryMetaData.setType("java.lang.String");
      environmentEntryMetaData.setValue("Test Value");

      environmentEntriesMetaData.add(environmentEntryMetaData);

      JBossEnterpriseBeanMetaData beanMetaData = mock(JBossEnterpriseBeanMetaData.class);
      when(beanMetaData.getEnvironmentEntries()).thenReturn(environmentEntriesMetaData);
      when(beanMetaData.getKey()).thenReturn("MockBean");
      when(beanMetaData.getName()).thenReturn("MockBean");

      JBossEnterpriseBeansMetaData beansMetaData = new JBossEnterpriseBeansMetaData();
      beansMetaData.add(beanMetaData);

      JBossMetaData jBossMetaData = mock(JBossMetaData.class);
      when(jBossMetaData.getEnterpriseBeans()).thenReturn(beansMetaData);

      when(beanMetaData.getJBossMetaData()).thenReturn(jBossMetaData);

      JBossAssemblyDescriptorMetaData assemblyDescriptors = new JBossAssemblyDescriptorMetaData();
      assemblyDescriptors.setInterceptorBindings(new InterceptorBindingsMetaData());
      when(jBossMetaData.getAssemblyDescriptor()).thenReturn(assemblyDescriptors);

      MutableAttachments attachments = ((MutableAttachments) deployment.getPredeterminedManagedObjects());
      attachments.addAttachment(JBossMetaData.class, jBossMetaData);
      attachJavaEEComponents(attachments);

      Deployment dependencyDeployment = createDeployment("dependency", BeanMetaDataBuilder.createBuilder("bean-testBean", String.class.getName()).setConstructorValue("test").getBeanMetaData());

      assertNameNotFound("java:comp/env/test");
      deploy(dependencyDeployment, deployment);
      assertContextValue("java:comp/env/test", "Test Value");
      undeploy(deployment, dependencyDeployment);
      unbind(context, "java:comp/env/test");
   }
//
//   @Test
//   public void testDeployWithInterceptors() throws Exception
//   {
//      bindOtherBean();
//      Deployment deployment = createDeployment("test1");
//      attachMetaData(deployment);
//
//      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
//
//      InterceptorsMetaData interceptorsMetaData = new InterceptorsMetaData();
//      InterceptorMetaData interceptorMetaData = mock(InterceptorMetaData.class);
//
//      when(interceptorMetaData.getKey()).thenReturn(MockInterceptor.class.getName());
//      when(interceptorMetaData.getInterceptorClass()).thenReturn(MockInterceptor.class.getName());
//      interceptorsMetaData.add(interceptorMetaData);
//
//      EnvironmentEntriesMetaData environmentEntriesMetaData = new EnvironmentEntriesMetaData();
//
//      EnvironmentEntryMetaData environmentEntryMetaData = new EnvironmentEntryMetaData();
//      environmentEntryMetaData.setEnvEntryName("test");
//      environmentEntryMetaData.setType("java.lang.String");
//      environmentEntryMetaData.setValue("Test Value");
//
//      environmentEntriesMetaData.add(environmentEntryMetaData);
//
//      when(interceptorMetaData.getEnvironmentEntries()).thenReturn(environmentEntriesMetaData);
//
//      JBossMetaData jBossMetaData = attachments.getAttachment(JBossMetaData.class);
//
//      Field interceptors = JBossMetaData.class.getDeclaredField("interceptors");
//      interceptors.setAccessible(true);
//      interceptors.set(jBossMetaData, interceptorsMetaData);
//
//      Deployment dependencyDeployment = createBinderDeployment();
//      deploy(dependencyDeployment);
//
//      assertNameNotFound(TEST_BEAN_PROP_FQ_JNDI_NAME);
//      assertNameNotFound("java:comp/env/test");
//      deploy(deployment);
//      assertContextValue(TEST_BEAN_PROP_FQ_JNDI_NAME, OTHER_BEAN);
//      assertContextValue("java:comp/env/test", "Test Value");
//      undeploy(dependencyDeployment, deployment);
//      unbindJndiEntries();
//      unbind(context, "java:comp/env/test");
//   }
}
