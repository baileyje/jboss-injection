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
package org.jboss.injection.inject.naming;

import org.jboss.injection.inject.spi.InjectionPoint;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * InjectionPoint instance capable of injecting a value into a Context
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 * @param <V> The type of the object being injected
 */
public class ContextInjectionPoint<V> implements InjectionPoint<Context, V>
{

   private final String jndiName;

   /**
    * Construct a new ContextInjectionPoint with a target jndi name.
    *
    * @param jndiName The jndi name to use when injecting into the context
    */
   public ContextInjectionPoint(final String jndiName)
   {
      if(jndiName == null) throw new IllegalArgumentException("JNDI name can not be null");
      this.jndiName = jndiName;
   }

   /**
    * {@inheritDoc}
    */
   public void set(final Context context, final V value)
   {
      try
      {
         context.bind(jndiName, value);
      } catch(NamingException e)
      {
         throw new RuntimeException("Failed to bind value [" + value + "] into context [" + context + "] with jndi name [" + jndiName + "]");
      }
   }
}
