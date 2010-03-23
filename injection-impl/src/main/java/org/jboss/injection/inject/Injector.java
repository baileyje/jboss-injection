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
package org.jboss.injection.inject;

import org.jboss.injection.inject.spi.InjectionPoint;
import org.jboss.injection.inject.spi.ValueRetriever;

/**
 * Injectors injectInto an inject into a target.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 * @param <T> The target object type
 */
public class Injector<T>
{
   private final TypedDelegate<T, ?> delegate;

   /**
    * Create a new Injector with an injection point and value retriever
    *
    * @param injectionPoint The injection point to inject into
    * @param valueRetriever The value retriever used to obtain the injected value
    */
   public <V> Injector(final InjectionPoint<T, V> injectionPoint, final ValueRetriever<V> valueRetriever)
   {
      if(injectionPoint == null) throw new IllegalArgumentException("TypedDelegate point can not be null");
      if(valueRetriever == null) throw new IllegalArgumentException("Value retriever can not be null");
      delegate = new TypedDelegate<T, V>(injectionPoint, valueRetriever);
   }

   /**
    * Performs the injection
    *
    * @param target The target object receiving the injection
    */
   public void inject(T target)
   {
      delegate.injectInto(target);
   }

   /**
    *  The purpose of this delegate is to hide the <V> value parameter from the public API of the
    *  Injector class as it is only used creation time, and is no longer needed for the call to inject.
    *  Basically internally formalizing the contract between the injection point's required value type
    *  and the type returned by the value retriever.
   */
   private static class TypedDelegate<T, V>
   {
      private final ValueRetriever<V> valueRetriever;
      private final InjectionPoint<T, V> injectionPoint;

      private TypedDelegate(final InjectionPoint<T, V> injectionPoint, final ValueRetriever<V> valueRetriever)
      {
         this.injectionPoint = injectionPoint;
         this.valueRetriever = valueRetriever;
      }

      private void injectInto(T target)
      {
         final V value = getValue();
         injectionPoint.set(target, value);
      }


      protected V getValue()
      {
         return valueRetriever.getValue();
      }
   }

   @Override
   public String toString()
   {
      return "Injector{" + "injectionPoint=" + delegate.injectionPoint + ", valueRetriever=" + delegate.valueRetriever +  '}';
   }
}
