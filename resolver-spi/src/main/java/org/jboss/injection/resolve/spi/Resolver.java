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


/**
 * Resolves a JNDI name and MC Bean name from the provided dependency metadata.
 * <p/>
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 * @param <M> The required metadata type
 * @param <C> The context in which to resolve (usually DeploymentUnit)
 * @param <V> The resolved value (usually an global JNDI name)
 */
public interface Resolver<M, C, V>
{
   /**
    * Return the type of metaData that can be resolved.
    *
    * @return The class object for this resolver type.
    */
   Class<M> getMetaDataType();

   /**
    * This method takes the provided metadata and determines the
    * global JNDI name being referenced as well as the MC bean name
    * that is responsible for ensuring the dependency is bound into
    * JNDI.
    *
    * @param context  The resolving context (usually DeploymentUnit)
    * @param metaData The metadata referencing a dependency
    * @return The ResolverResult
    */
   ResolverResult<V> resolve(C context, M metaData);
}
