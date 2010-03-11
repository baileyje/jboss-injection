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
 * The results of executing a Resolver.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class ResolverResult {

   private final String globalJndiName;
   private final String encJndiName;
   private final String beanName;

   public ResolverResult(final String globalJndiName, final String encJndiName, final String beanName) {
      this.globalJndiName = globalJndiName;
      this.encJndiName = encJndiName;
      this.beanName = beanName;
   }

   /**
    * Get the resolved global JNDI entry for a component.
    *
    * @return The global JNDI name
    */
   public String getGlobalJndiName() {
      return globalJndiName;
   }

   /**
    * Get the ENC JNDI entry for mapping.
    *
    * TODO:  Should this be determined by Resolvers..
    *
    * @return The ENC JNDI name
    */
   public String getEncJndiName() {
      return encJndiName;
   }

   /**
    * Get the resolved MC bean name that will be binding the component into JNDI.
    *
    * @return The MC bean name
    */
   public String getBeanName() {
      return beanName;
   }
}
