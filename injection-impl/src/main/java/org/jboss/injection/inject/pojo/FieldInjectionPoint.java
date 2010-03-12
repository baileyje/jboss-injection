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

import java.lang.reflect.Field;

/**
 * Provides a InjectionPoint representing a field.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class FieldInjectionPoint extends AbstractAccessibleObjectBeanProperty<Field>
{

   /**
    * Constructs a new instance with the provided method.
    *
    * @param field The field used for injection
    */
   public FieldInjectionPoint(final Field field)
   {
      super(field);
   }

   /**
    * {@inheritDoc}
    */
   public void set(final Object target, final Object value)
   {
      Field field = getAccessibleObject();
      try
      {
         field.set(target, value);
      }
      catch(IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
      catch(IllegalArgumentException e)
      {
         String msg = "failed to set value " + value + " on field " + field;

         // Help out with the error message; let the developer know if the
         // value and target field CLs are not equal
         ClassLoader fieldLoader = field.getType().getClassLoader();
         ClassLoader valueLoader = value.getClass().getClassLoader();
         // Equal if both are null (some JDKs use this to represent Bootstrap CL), or they're equal - EJBTHREE-1694
         boolean equalLoaders = (fieldLoader == null && valueLoader == null) ? true : fieldLoader.equals(valueLoader);
         if(!equalLoaders)
         {
            msg = msg + "; Reason: ClassLoaders of value and target are not equal";

         }
         throw new IllegalArgumentException(msg);
      }
   }
}
