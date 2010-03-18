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

import org.jboss.metadata.javaee.spec.Environment;

/**
 * Extracts specific metadata from an Environment instance.
 * This will likely be paired with (or implemented by) a Resolver of
 * the same metadata type.
 *
 * @param <M> The metadata type to getMetaData
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public interface EnvironmentMetaDataVisitor<M>
{
   /**
    * Return the type of metaData being extracted
    *
    * @return The class object for the metadata type
    */
   Class<M> getMetaDataType();

   /**
    * Extracts the metadata references from an Environment
    *
    * @param environment The environemnt to getMetaData references from
    * @return The reference metadata
    */
   Iterable<M> getMetaData(Environment environment);
}
