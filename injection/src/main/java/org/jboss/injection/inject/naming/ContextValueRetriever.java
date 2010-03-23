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

import org.jboss.injection.inject.spi.ValueRetriever;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Value retriever used to get values from JNDI
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class ContextValueRetriever<M> implements ValueRetriever<M>
{
   private Context context;
   private final String jndiName;

   /**
    * Creates a new instance with no Context.  Will default to an InitialContext.
    *
    * @param jndiName To retrieve the value at
    */
   public ContextValueRetriever(String jndiName)
   {
      this(null, jndiName);
   }

   /**
    * Creates a new instance with a context.
    *
    * @param context  The context to retrieve the value from
    * @param jndiName The location to retrieve the value from
    */
   public ContextValueRetriever(final Context context, String jndiName)
   {
      this.context = context;
      this.jndiName = jndiName;
   }

   /**
    * {@inheritDoc}
    */
   public M getValue()
   {
      return lookup(jndiName);
   }

   /**
    * Look up the value at a specific jndi location
    *
    * @param jndiName The jndi name to lookup
    * @return The value at the jndi location
    */
   protected M lookup(final String jndiName)
   {
      M dependency;
      try
      {
         final Context context = getContext();
         dependency = (M)context.lookup(jndiName);
      }
      catch(NamingException e)
      {
         Throwable cause = e;
         while(cause.getCause() != null)
            cause = cause.getCause();
         throw new RuntimeException("Unable to lookup jndi value: " + jndiName + cause.getMessage(), e);
      }
      return dependency;
   }

   protected Context getContext() throws NamingException
   {
      if(context == null)
         context = new InitialContext();
      return context;
   }

   @Override
   public String toString()
   {
      return "ContextValueRetriever{" + "context=" + context + ", jndiName='" + jndiName + '\'' + '}';
   }
}
