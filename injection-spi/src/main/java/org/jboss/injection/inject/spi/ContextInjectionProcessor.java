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
package org.jboss.injection.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.naming.Context;


/**
 * A {@link ContextInjectionProcessor} processes annotation on a method 
 * or a field of a class and creates a {@link Injector} for the class' object.
 * 
 * <p>
 *  The {@link ContextInjectionProcessor} uses the JNDI context to get hold of the value
 *  to be injected
 * </p>
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public interface ContextInjectionProcessor<T extends Annotation>
{

   /**
    * Returns the annotation type which this processor looks for, on
    * {@link Method}s and {@link Field}s.
    *  
    * @return
    */
   public Class<T> getAnnotationType();

   /**
    * Creates and returns an {@link Injector} by processing the <code>method</code>
    * for a annotation of type {@link #getAnnotationType()}.
    * <p>
    * This method returns null if no {@link Injector} was created for the passed
    * <code>method</code>
    * </p>
    * @param ctx The naming context which will be used by the {@link Injector} to
    *               get the value to be injected. 
    *               <p>The passed <code>ctx</code> need *not* contain the value to be 
    *               injected, when this method is invoked. The context will only be looked up,
    *               when the created {@link Injector#inject(Object)} method is invoked
    * @param method The method to be processed for the presence of any annotation of type
    *               {@link #getAnnotationType()}
    * @return
    */
   public Injector<Object> process(Context ctx, Method method);

   /**
    * Creates and returns an {@link Injector} by processing the <code>field</code>
    * for a annotation of type {@link #getAnnotationType()}.
    * <p>
    * This method returns null if no {@link Injector} was created for the passed
    * <code>field</code>
    * </p>
    * @param ctx The naming context which will be used by the {@link Injector} to
    *               get the value to be injected. 
    *               <p>The passed <code>ctx</code> need *not* contain the value to be 
    *               injected, when this method is invoked. The context will only be looked up,
    *               when the created {@link Injector#inject(Object)} method is invoked
    * @param field The field to be processed for the presence of any annotation of type
    *               {@link #getAnnotationType()}
    * @return
    */
   public Injector<Object> process(Context ctx, Field field);

}
