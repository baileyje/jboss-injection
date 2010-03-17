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

import org.jboss.deployers.spi.deployer.helpers.AbstractComponentDeployer;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeploymentVisitor;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock deployer that creates JBossEnterpriseBeanMetaData components from JBossMetaData
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class MockEjbComponentDeployer extends AbstractComponentDeployer<JBossMetaData, JBossEnterpriseBeanMetaData>
{
   public MockEjbComponentDeployer()
   {
      setDeploymentVisitor(new DeploymentVisitor());
      setOutput(JBossEnterpriseBeanMetaData.class);
   }

   private class DeploymentVisitor extends AbstractDeploymentVisitor<JBossEnterpriseBeanMetaData, JBossMetaData>
   {
      @Override
      protected List<JBossEnterpriseBeanMetaData> getComponents(JBossMetaData deployment)
      {
         final List<JBossEnterpriseBeanMetaData> components = new ArrayList<JBossEnterpriseBeanMetaData>(deployment.getEnterpriseBeans());
         return components;
      }

      @Override
      protected Class<JBossEnterpriseBeanMetaData> getComponentType()
      {
         return JBossEnterpriseBeanMetaData.class;
      }

      public Class<JBossMetaData> getVisitorType()
      {
         return JBossMetaData.class;
      }

      @Override
      protected String getComponentName(JBossEnterpriseBeanMetaData attachment)
      {
         return JBossEnterpriseBeanMetaData.class.getName() + "." + attachment.getName();
      }
   }
}
