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

import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.metadata.javaee.spec.DescriptionGroupMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class WebSwitchBoardOperatorDeployerTest extends BasicSwitchBoardOperatorDeployerTestCase
{
   @Override
   protected void attachMetaData(Deployment deployment)
   {
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      EJBReferencesMetaData referencesMetaData = new EJBReferencesMetaData();
      EJBReferenceMetaData referenceMetaData = new EJBReferenceMetaData();
      referenceMetaData.setEjbRefName("testRef");
      referencesMetaData.add(referenceMetaData);

      Environment environment = mock(Environment.class);
      when(environment.getEjbReferences()).thenReturn(referencesMetaData);

      JBossWebMetaData jBossWebMetaData = mock(JBossWebMetaData.class);
      when(jBossWebMetaData.getJndiEnvironmentRefsGroup()).thenReturn(environment);

      DescriptionGroupMetaData descriptionGroupMetaData = mock(DescriptionGroupMetaData.class);
      when(descriptionGroupMetaData.getDisplayName()).thenReturn("Test War");

      when(jBossWebMetaData.getDescriptionGroup()).thenReturn(descriptionGroupMetaData);

      attachments.addAttachment(JBossWebMetaData.class, jBossWebMetaData);
   }
}