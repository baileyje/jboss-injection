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
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.reloaded.naming.spi.JavaEEComponent;

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
      JBossMetaData jBossMetaData = mock(JBossMetaData.class);
      JBossEnterpriseBeanMetaData jBossEnterpriseBeanMetaData = mock(JBossEnterpriseBeanMetaData.class);
      when(jBossEnterpriseBeanMetaData.getKey()).thenReturn("testBean");
      when(jBossEnterpriseBeanMetaData.getName()).thenReturn("testBean");

      EJBReferencesMetaData referencesMetaData = new EJBReferencesMetaData();
      EJBReferenceMetaData referenceMetaData = new EJBReferenceMetaData();
      referenceMetaData.setEjbRefName("testRef");
      referencesMetaData.add(referenceMetaData);

      when(jBossEnterpriseBeanMetaData.getEjbReferences()).thenReturn(referencesMetaData);

      JBossEnterpriseBeansMetaData jBossEnterpriseBeansMetaData = new JBossEnterpriseBeansMetaData();
      jBossEnterpriseBeansMetaData.add(jBossEnterpriseBeanMetaData);

      when(jBossMetaData.getEnterpriseBeans()).thenReturn(jBossEnterpriseBeansMetaData);

      attachments.addAttachment(JBossMetaData.class, jBossMetaData);

      defaultMockEnvironment = jBossEnterpriseBeanMetaData;

      JavaEEComponent component = mock(JavaEEComponent.class);
      when(component.getContext()).thenReturn(context);

      BeanMetaData beanMetaData = BeanMetaDataBuilder.createBuilder("JavaEEComponent", JavaEEComponent.class.getName())
         .setConstructorValue(component)
         .addAlias("java:comp")
         .getBeanMetaData();
      attachments.addAttachment(BeanMetaData.class.getName() + ".JavaEEComponent", beanMetaData);
   }
}
