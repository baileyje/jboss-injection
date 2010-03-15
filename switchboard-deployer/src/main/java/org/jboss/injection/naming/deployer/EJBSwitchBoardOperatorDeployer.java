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

import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.javaee.spec.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * SwitchBoardOperatorDeployer implementation based on JBossMetaData.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EJBSwitchBoardOperatorDeployer extends SwitchBoardOperatorDeployer<JBossMetaData>
{

   public EJBSwitchBoardOperatorDeployer()
   {
      super(JBossMetaData.class);
   }

   @Override
   protected List<NamedEnvironment> getEnvironments(final JBossMetaData deployment)
   {
      final JBossEnterpriseBeansMetaData jBossEnterpriseBeansMetaData = deployment.getEnterpriseBeans();
      final List<NamedEnvironment> namedEnvironments = new ArrayList<NamedEnvironment>(jBossEnterpriseBeansMetaData.size());
      for(JBossEnterpriseBeanMetaData jBossEnterpriseBeanMetaData : jBossEnterpriseBeansMetaData)
      {
         final String name = jBossEnterpriseBeanMetaData.getName();
         final NamedEnvironment namedEnvironment = new NamedEnvironment(name, jBossEnterpriseBeanMetaData);
         namedEnvironments.add(namedEnvironment);
      }
      return namedEnvironments;
   }
}