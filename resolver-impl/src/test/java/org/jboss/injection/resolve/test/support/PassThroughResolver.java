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
package org.jboss.injection.resolve.test.support;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;

/**
 * PassThroughResolver -
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 * @version $Revision$
 */
public class PassThroughResolver<M> implements Resolver<M> {

   private final String beanName;
   private final String jndiName;

   public PassThroughResolver(final String beanName, final String jndiName) {
      this.beanName = beanName;
      this.jndiName = jndiName;
   }

   public ResolverResult resolve(DeploymentUnit unit, final M metaData) {
      return new ResolverResult() {
         public String getBeanName() {
            return beanName;
         }

         public String getJndiName() {
            return jndiName;
         }
      };
   }
}
