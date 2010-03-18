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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.Context;

import org.jboss.injection.inject.InjectorFactory;
import org.jboss.injection.inject.pojo.FieldInjectionPoint;
import org.jboss.injection.inject.pojo.MethodInjectionPoint;
import org.jboss.injection.inject.spi.ContextInjectionProcessor;
import org.jboss.injection.inject.spi.InjectionPoint;
import org.jboss.injection.inject.spi.Injector;
import org.jboss.injection.inject.spi.ValueRetriever;

/**
 * An implementation of {@link ContextInjectionProcessor} which processes
 * {@link Method} and {@link Field} annotation and creates a {@link Injector}
 * based on either a {@link MethodInjectionPoint} or a {@link FieldInjectionPoint}
 * and a {@link ContextValueRetriever}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public abstract class AbstractContextInjectionProcessor<T extends Annotation> implements ContextInjectionProcessor<T>
{
   // let the individual implementations provide the annotation type
   public abstract Class<T> getAnnotationType();

   /**
    * Returns the ENC name from the passed <code>annotation</code>.
    * <p>
    *   Typically, annotations like {@link Resource}, {@link EJB} etc...
    *   have a <code>name</code> attribute which corresponds to the ENC
    *   name from where the value to be injected, has to be looked up. 
    * </p>
    * <p>
    *   For example, for the annotation:
    *   <code>@Resource (name="datasource")</code>
    *   this method would return back "datasource" as the ENC name
    * </p>
    * <p>
    *   This method returns null if the annotation does not have a ENC name
    *   set
    * </p>  
    *  
    * @param annotation
    * @return
    */
   protected abstract String getENCName(T annotation);

   /**
    * Processes the annotation on the passed <code>method</code> and 
    * creates a {@link Injector}, based on a {@link MethodInjectionPoint}
    * and a {@link ContextValueRetriever}
    * 
    * {@inheritDoc}
    */
   @Override
   public Injector<Object> process(Context ctx, Method method)
   {
      // first check for the presence of any annotation 
      T annotation = method.getAnnotation(this.getAnnotationType());
      // no annotation == no work!
      if (annotation == null)
      {
         return null;
      }

      // get any ENC name that's been specified as a attribute value
      // of the annotation
      String encName = this.getENCName(annotation);
      // If any ENC name was specified, then prefix the name with
      // "env/" (as per the spec)
      if (encName != null && encName.trim().isEmpty() == false)
      {
         encName = "env/" + encName;
      }
      else
      {
         // no ENC name was found in the annotation, so
         // generate a Spec specified ENC name from the method (this
         // will include the "env/" prefix
         encName = ENCNameGenerator.getEncName(method);
      }
      // Create a context value retriever for the passed (ENC) context
      // and the ENC name
      ValueRetriever<Object> valRetriever = new ContextValueRetriever<Object>(ctx, encName);
      // we are injecting into a method, so a MethodInjectionPoint
      InjectionPoint<Object, Object> methodInjectionPoint = new MethodInjectionPoint(method);

      // finally create the injector for the method injection point and the
      // context value retriever
      return InjectorFactory.create(methodInjectionPoint, valRetriever);

   }

   /**
    * Processes the annotation on the passed <code>field</code> and 
    * creates a {@link Injector}, based on a {@link FieldInjectionPoint}
    * and a {@link ContextValueRetriever}
    * 
    * {@inheritDoc}
    */
   @Override
   public Injector<Object> process(Context ctx, Field field)
   {
      // first check for the presence of any annotation 
      T annotation = field.getAnnotation(this.getAnnotationType());
      // no annotation == no work!
      if (annotation == null)
      {
         return null;
      }
      // get any ENC name that's been specified as a attribute value
      // of the annotation
      String encName = this.getENCName(annotation);
      // If any ENC name was specified, then prefix the name with
      // "env/" (as per the spec)
      if (encName != null && encName.trim().isEmpty() == false)
      {

         encName = "env/" + encName;
      }
      else
      {
         // no ENC name was found in the annotation, so
         // generate a Spec specified ENC name from the method (this
         // will include the "env/" prefix
         encName = ENCNameGenerator.getEncName(field);
      }

      // Create a context value retriever for the passed (ENC) context
      // and the ENC name
      ValueRetriever<Object> valRetriever = new ContextValueRetriever<Object>(ctx, encName);
      // we inject into a field, so create a FieldInjectionPoint
      InjectionPoint<Object, Object> fieldInjectionPoint = new FieldInjectionPoint(field);

      // finally create the injector for the field injection point and the
      // context value retriever
      return InjectorFactory.create(fieldInjectionPoint, valRetriever);

   }

}
