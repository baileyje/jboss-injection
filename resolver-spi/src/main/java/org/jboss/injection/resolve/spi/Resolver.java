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
package org.jboss.injection.resolve.spi;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.resolve.spi.ResolverResult;

/**
 * Resolves a JNDI name and MC Bean name from the provided dependency metadata. 
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 * @param <M> The required metadata type
 */
public interface Resolver<M> {

   /**
    * This method takes the provided metadata and determines the
    * global JNDI name being referenced as well as the MC bean name
    * that is responsible for ensuring the dependency is bound into
    * JNDI.
    *
    * @param deploymentUnit The current deployment unit
    * @param metaData The metadata referencing a dependency
    * @return The ResolverResult
    */
   ResolverResult resolve(DeploymentUnit deploymentUnit, M metaData);
}
