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
package org.jboss.injection.inject.pojo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provides a InjectionPoint representing a method.
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 */
public class MethodInjectionPoint extends AbstractAccessibleObjectBeanProperty<Method> {

   /**
    * Constructs a new instance with the provided method.
    *
    * @param method The method used for injection
    */
   public MethodInjectionPoint(final Method method) {
      super(method);
      assert method.getReturnType() == Void.TYPE;
      assert method.getParameterTypes().length == 1;
      assert method.getName().startsWith("set");
   }

   /**{@inheritDoc} */
   public void set(final Object target, final Object value) {
      Method method = getAccessibleObject();
      Object args[] = {value};
      try {
         method.invoke(target, args);
      }
      catch(IllegalAccessException e) {
         throw new RuntimeException(e);
      }
      catch(IllegalArgumentException e) {
         String msg = "failed to set value " + value + " with setter " + method;
         throw new IllegalArgumentException(msg);
      }
      catch(InvocationTargetException e) {
         Throwable cause = e.getCause();
         if(cause instanceof Error)
            throw (Error) cause;
         if(cause instanceof RuntimeException)
            throw (RuntimeException) cause;
         throw new RuntimeException(cause);
      }
   }
}