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
import org.jboss.injection.naming.test.deployer.support.OtherMockBean;
import org.jboss.naming.Util;
import org.jboss.reloaded.naming.spi.JavaEEComponent;
import org.jboss.reloaded.naming.spi.JavaEEModule;
import org.junit.Test;

import java.net.URL;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test to verify the SwitchBoardParsingDeployer correctly handles parsing -switchBoard.xml file.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class SwitchBoardParsingDeployerTest extends AbstractSwitchBoardOperatorDeployerTestCase
{
   @Test
   public void testParsingDeployment() throws Exception
   {
      OtherMockBean otherBean = new OtherMockBean("test");
      Util.bind(context, "java:OtherBean", otherBean);
      URL url = SwitchBoardParsingDeployerTest.class.getResource("/test/test.jar/");
      Deployment deployment = createDeployment(url);
      attachJavaEEComponents(deployment);

      Deployment binder = createDeployment("binder", BeanMetaDataBuilder.createBuilder("binder-a", Object.class.getName()).getBeanMetaData());

      assertNameNotFound("java:comp/env/a");
      assertNameNotFound("java:module/env/a");
      deploy(deployment, binder);
      assertContextValue("java:comp/env/a", otherBean);
      assertContextValue("java:module/env/a", otherBean);
      undeploy(deployment, binder);
      unbind(context, "java:OtherBean");
      unbind(compContext, "env/a");
      unbind(moduleContext, "env/a");
   }


   private void attachJavaEEComponents(final Deployment deployment)
   {
      MutableAttachments attachments = (MutableAttachments)deployment.getPredeterminedManagedObjects();
      JavaEEComponent component = mock(JavaEEComponent.class);
      when(component.getContext()).thenReturn(compContext);

      BeanMetaData beanMetaData = BeanMetaDataBuilder.createBuilder("jboss.naming:module=Module,component=MyBean", JavaEEComponent.class.getName())
         .setConstructorValue(component)
         .getBeanMetaData();
      attachments.addAttachment(BeanMetaData.class.getName() + ".JavaEEComponent", beanMetaData);


      JavaEEModule module = mock(JavaEEModule.class);
      when(module.getContext()).thenReturn(moduleContext);

      beanMetaData = BeanMetaDataBuilder.createBuilder("jboss.naming:module=Module", JavaEEModule.class.getName())
         .setConstructorValue(module)
         .getBeanMetaData();
      attachments.addAttachment(BeanMetaData.class.getName() + ".JavaEEModule", beanMetaData);
   }
}
