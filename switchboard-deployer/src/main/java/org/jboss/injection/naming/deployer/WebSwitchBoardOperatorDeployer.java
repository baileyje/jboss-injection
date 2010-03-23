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

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.reloaded.naming.deployers.javaee.JavaEEModuleInformer;

import java.util.LinkedList;
import java.util.List;

/**
 * SwitchBoardOperatorDeployer that handles WEB/WEB+EJB deployments.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class WebSwitchBoardOperatorDeployer extends AbstractSwitchBoardOperatorDeployer<JBossWebMetaData>
{
   private JavaEEModuleInformer moduleInformer;

   public WebSwitchBoardOperatorDeployer()
   {
      super(JBossWebMetaData.class);
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
      final List<Environment> environments = new LinkedList<Environment>();
      environments.add(metaData.getJndiEnvironmentRefsGroup());

      if(unit.isAttachmentPresent(JBossMetaData.class))
      {
         final JBossMetaData jBossMetaData = unit.getAttachment(JBossMetaData.class);
         for(JBossEnterpriseBeanMetaData beanMetaData : jBossMetaData.getEnterpriseBeans())
         {
            environments.add(beanMetaData);
            environments.addAll(collectInterceptors(beanMetaData));
         }
      }
      deploy(unit, environments);
   }

   /** {@inheritDoc} */
   protected String getBeanNameQualifier(final DeploymentUnit deploymentUnit)
   {
      String applicationName = moduleInformer.getApplicationName(deploymentUnit);
      String moduleName = moduleInformer.getModulePath(deploymentUnit);
      final StringBuilder builder = new StringBuilder();
      if(applicationName != null)
      {
         builder.append("application=").append(applicationName).append(",");
      }
      builder.append("module=").append(moduleName);
      return builder.toString();
   }

   /**
    * Set the module informer
    *
    * @param moduleInformer The module informer
    */
   @Inject
   public void setModuleInformer(final JavaEEModuleInformer moduleInformer)
   {
      this.moduleInformer = moduleInformer;
   }

}
