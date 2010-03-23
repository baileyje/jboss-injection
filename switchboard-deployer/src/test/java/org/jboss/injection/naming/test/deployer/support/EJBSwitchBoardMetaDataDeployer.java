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
package org.jboss.injection.naming.test.deployer.support;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.naming.switchboard.SwitchBoardComponentMetaData;
import org.jboss.injection.naming.switchboard.SwitchBoardMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

/**
 * Deployer that handles EJB only deployments.
 * Will skip any deployment units that also have JBossWebMetaData.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EJBSwitchBoardMetaDataDeployer extends AbstractSimpleRealDeployer<JBossMetaData>
{

   public EJBSwitchBoardMetaDataDeployer()
   {
      super(JBossMetaData.class);
      setOutput(SwitchBoardMetaData.class);
   }

   @Override
   public void deploy(final DeploymentUnit unit, final JBossMetaData deployment) throws DeploymentException
   {
      if(unit.isAttachmentPresent(JBossWebMetaData.class))
         return;

      final SwitchBoardMetaData switchBoardMetaData = new SwitchBoardMetaData();

      for(JBossEnterpriseBeanMetaData enterpriseBeanMetaData : deployment.getEnterpriseBeans())
      {
         final SwitchBoardComponentMetaData switchBoardComponentMetaData = new SwitchBoardComponentMetaData(enterpriseBeanMetaData);
         switchBoardComponentMetaData.setComponentName(enterpriseBeanMetaData.getName());
         switchBoardMetaData.addComponent(switchBoardComponentMetaData);
      }
      unit.addAttachment(SwitchBoardMetaData.class, switchBoardMetaData);
   }

}
