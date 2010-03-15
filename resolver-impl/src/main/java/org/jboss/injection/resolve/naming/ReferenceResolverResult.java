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
package org.jboss.injection.resolve.naming;

import org.jboss.injection.inject.naming.LinkRefValueRetriever;
import org.jboss.injection.resolve.spi.ResolverResult;

/**
 * ResolverResult implementation that supports JNDI based references.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class ReferenceResolverResult extends ResolverResult
{
   /**
    * Creates a new instance
    *
    * @param refName The reference JNDI NAME
    * @param beanName The MC bean name to this depends on
    * @param targetJndiName The JNDI name to target
    */
   public ReferenceResolverResult(final String refName, final String beanName, String targetJndiName)
   {
      super(refName, beanName, new LinkRefValueRetriever(targetJndiName));
   }
}
