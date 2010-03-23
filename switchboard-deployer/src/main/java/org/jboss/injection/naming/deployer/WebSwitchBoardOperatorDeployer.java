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
package org.jboss.injection.naming.deployer;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.naming.switchboard.SwitchBoardMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;

/**
 * Deployer that handles WEB/WEB+EJB deployments.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class WebSwitchBoardOperatorDeployer extends AbstractSimpleRealDeployer<JBossWebMetaData>
{

   public WebSwitchBoardOperatorDeployer()
   {
      super(JBossWebMetaData.class);
      setOutput(SwitchBoardMetaData.class);
   }

   /**
    * Deploy with WEB (and maybe EJB) metadata.  Will create a single switchboard for all components.
    *
    * @param unit The deployment unit
    * @param metaData The metadata to process
    * @throws DeploymentException if any deployment issues occur
    */
   public void deploy(final DeploymentUnit unit, final JBossWebMetaData metaData) throws DeploymentException
   {
      SwitchBoardMetaData switchBoardMetaData = new SwitchBoardMetaData(metaData.getJndiEnvironmentRefsGroup());


      if(unit.isAttachmentPresent(JBossMetaData.class))
      {
         // We are not going to do components since they all share a name space
         final JBossMetaData jBossMetaData = unit.getAttachment(JBossMetaData.class);
         for(JBossEnterpriseBeanMetaData beanMetaData : jBossMetaData.getEnterpriseBeans())
         {
            if(beanMetaData.getAnnotatedEjbReferences() != null)
               switchBoardMetaData.getAnnotatedEjbReferences().addAll(beanMetaData.getAnnotatedEjbReferences());
            if(beanMetaData.getDataSources() != null)
               switchBoardMetaData.getDataSources().addAll(beanMetaData.getDataSources());
            if(beanMetaData.getEjbLocalReferences() != null)
               switchBoardMetaData.getEjbLocalReferences().addAll(beanMetaData.getEjbLocalReferences());
            if(beanMetaData.getEjbReferences() != null)
               switchBoardMetaData.getEjbReferences().addAll(beanMetaData.getEjbReferences());
            if(beanMetaData.getEnvironmentEntries() != null)
               switchBoardMetaData.getEnvironmentEntries().addAll(beanMetaData.getEnvironmentEntries());
            if(beanMetaData.getMessageDestinationReferences() != null)
               switchBoardMetaData.getMessageDestinationReferences().addAll(beanMetaData.getMessageDestinationReferences());
            if(beanMetaData.getPersistenceContextRefs() != null)
               switchBoardMetaData.getPersistenceContextRefs().addAll(beanMetaData.getPersistenceContextRefs());
            if(beanMetaData.getPersistenceUnitRefs() != null)
               switchBoardMetaData.getPersistenceUnitRefs().addAll(beanMetaData.getPersistenceUnitRefs());
            if(beanMetaData.getResourceEnvironmentReferences() != null)
               switchBoardMetaData.getResourceEnvironmentReferences().addAll(beanMetaData.getResourceEnvironmentReferences());
            if(beanMetaData.getResourceReferences() != null)
               switchBoardMetaData.getResourceReferences().addAll(beanMetaData.getResourceReferences());
            if(beanMetaData.getServiceReferences() != null)
               switchBoardMetaData.getServiceReferences().addAll(beanMetaData.getServiceReferences());
         }
      }

      unit.addAttachment(SwitchBoardMetaData.class, switchBoardMetaData);
   }

}
