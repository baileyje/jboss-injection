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
import org.jboss.metadata.web.jboss.JBossWebMetaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SwitchBoardOperatorDeployer implementation based on JBossWebMetaData.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class WebSwitchBoardOperatorDeployer extends SwitchBoardOperatorDeployer<JBossWebMetaData>
{

   public WebSwitchBoardOperatorDeployer()
   {
      super(JBossWebMetaData.class);
   }

   @Override
   protected List<NamedEnvironment> getEnvironments(final JBossWebMetaData deployment)
   {
      // TODO: What is the correct name to use?
      final String name = deployment.getDescriptionGroup().getDisplayName();
      final Environment environment = deployment.getJndiEnvironmentRefsGroup();
      final NamedEnvironment namedEnvironment = new NamedEnvironment(name, environment);
      return Collections.singletonList(namedEnvironment);
   }
}