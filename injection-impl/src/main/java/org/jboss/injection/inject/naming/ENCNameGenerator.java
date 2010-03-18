/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A utility for generating a ENC name from a {@link Method}, {@link Field}
 * or a {@link Class}, as specified in the EJB3 spec
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class ENCNameGenerator
{

   /**
    * Generates a ENC name from a {@link Class}
    * @param type The {@link Class} type
    * @return Returns null if the passed <code>type</code> is null. Else
    *   returns the ENC name
    */
   public static String getEncName(Class<?> type)
   {
      if (type == null)
      {
         return null;
      }
      return "env/" + type.getName();
   }

   /**
    * Generates the ENC name from the passed <code>method</code>
    * @param method The method 
    * @return Returns null if the passed <code>method</code> is null. Else
    *   returns the ENC name
    */
   public static String getEncName(Method method)
   {
      if (method == null)
      {
         return null;
      }
      // generate the ENC name
      String encName = method.getName().substring(3);
      if (encName.length() > 1)
      {
         encName = encName.substring(0, 1).toLowerCase() + encName.substring(1);
      }
      else
      {
         encName = encName.toLowerCase();
      }
      encName = getEncName(method.getDeclaringClass()) + "/" + encName;

      return encName;
   }

   /**
    * Generates the ENC name from the passed <code>field</code>
    * @param field The field
    * @return Returns null if the passed <code>field</code> is null. Else
    *   returns the ENC name
    */
   public static String getEncName(Field field)
   {
      if (field == null)
      {
         return null;
      }
      return getEncName(field.getDeclaringClass()) + "/" + field.getName();
   }
}
