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
import org.jboss.beans.metadata.plugins.AbstractInjectionValueMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.reloaded.naming.deployers.javaee.JavaEEComponentInformer;

import java.util.Collections;

/**
 * SwitchBoardOperatorDeployer that handles EJB only deployments.
 * Will skip any deployment units that also have JBossWebMetaData.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EJBSwitchBoardOperatorDeployer extends SwitchBoardOperatorDeployer<JBossMetaData>
{
   private JavaEEComponentInformer componentInformer;

   public EJBSwitchBoardOperatorDeployer()
   {
      super(JBossMetaData.class);
   }

   /**
    * Deploy with EJB metadata only.  Will create a separate switchboard for each component.
    *
    * @param unit The deployment unit
    * @param metaData The metadata to process
    * @throws org.jboss.deployers.spi.DeploymentException if any deployment issues occur
    */
   public void deploy(final DeploymentUnit unit, final JBossMetaData metaData) throws DeploymentException
   {
      // Make sure this is not a war deployment with ejbs
      if(unit.isAttachmentPresent(JBossWebMetaData.class))
         return;

      final JBossEnterpriseBeansMetaData jBossEnterpriseBeansMetaData = metaData.getEnterpriseBeans();
      for(JBossEnterpriseBeanMetaData jBossEnterpriseBeanMetaData : jBossEnterpriseBeansMetaData)
      {
         deploy(unit, Collections.singletonList((Environment) jBossEnterpriseBeanMetaData));
      }
   }

   /** {@inheritDoc} */
   protected String createBeanName(final DeploymentUnit deploymentUnit)
   {
      final String applicationName = componentInformer.getApplicationName(deploymentUnit);
      final String moduleName = componentInformer.getModulePath(deploymentUnit);
      final String componentName = componentInformer.getComponentName(deploymentUnit);
      final StringBuilder builder = new StringBuilder("jboss.naming:service=SwitchBoardOperator");
      if(applicationName != null)
      {
         builder.append(",application=").append(applicationName);
      }
      builder.append(",module=").append(moduleName);
      builder.append(",component=").append(componentName);
      return builder.toString();
   }

   /** {@inheritDoc} */
   protected AbstractInjectionValueMetaData createContextInjectionValueMetaData()
   {
      return new AbstractInjectionValueMetaData("java:comp", "context");
   }

   /**
    * Get the component informer
    *
    * @return the component informer
    */
   protected JavaEEComponentInformer getComponentInformer()
   {
      return componentInformer;
   }

   /**
    * Set the component informer
    *
    * @param componentInformer The component informer
    */
   @Inject
   public void setComponentInformer(final JavaEEComponentInformer componentInformer)
   {
      this.componentInformer = componentInformer;
   }
}
