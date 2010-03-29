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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javassist.Modifier;

import javax.naming.Context;

import org.jboss.injection.inject.pojo.FieldInjectionPoint;
import org.jboss.injection.inject.pojo.MethodInjectionPoint;
import org.jboss.injection.inject.spi.InjectionPoint;
import org.jboss.injection.inject.Injector;
import org.jboss.injection.inject.spi.ValueRetriever;
import org.jboss.injection.resolve.spi.EnvironmentMetaDataVisitor;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.ResourceInjectionMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;

/**
 * Process an {@link Environment} for injection targets ( {@link ResourceInjectionTargetMetaData})
 * and creates {@link Injector}s for each injection target.
 * <p>
 *  This processor creates a list of {@link Injector}s which fetch the value, to be injected,
 *  from the ENC {@link Context} and inject the value into the injection target's field/method.
 * </p>
 * <p>
 *  This processor expects the ENC {@link Context} to be passed to it. It uses the passed ENC context
 *  to create a {@link ContextValueRetriever} corresponding to the context. The {@link ContextValueRetriever}
 *  is then used to create a {@link Injector}
 * </p>
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class InjectionProcessor
{
   
   private List<EnvironmentMetaDataVisitor<ResourceInjectionMetaData>> visitors;
   
   public InjectionProcessor()
   {
      this(new ArrayList<EnvironmentMetaDataVisitor<ResourceInjectionMetaData>>());
   }
   
   public InjectionProcessor(List<EnvironmentMetaDataVisitor<ResourceInjectionMetaData>> visitors)
   {
      this.visitors = visitors;
   }
   
   /**
    * Process a <code>environment</code> for injection targets and creates 
    * a list of {@link Injector}s
    * 
    * @param enc The ENC of the component to which the <code>environment</code> belongs
    * @param cl The classloader of the component being processed
    * @param environment Environment being processed for injection targets 
    * @return The injectors for the environment
    * @throws Exception
    */
   public List<Injector<Object>> process(Context enc, ClassLoader cl, Environment environment)
         throws Exception
   {
      List<Injector<Object>> injectors = new ArrayList<Injector<Object>>();
      for (EnvironmentMetaDataVisitor<ResourceInjectionMetaData> visitor : this.visitors)
      {
         injectors.addAll(this.process(enc, cl, visitor.getMetaData(environment)));   
      }
      
      return injectors;
   }


   /**
    * Creates a list of {@link Injector}s for the passed injections.
    * 
    * <p>
    *   This method iterates each of the passed <code>injections</code> and:
    *   <ul>
    *       <li> 
    *           For each {@link ResourceInjectionMetaData} it creates a {@link ContextValueRetriever} for each of the injections. 
    *           The {@link ResourceInjectionMetaData#getName()} represents the ENC jndi name
    *           and will be used to create the {@link ContextValueRetriever}.
    *       </li>
    *       <li>
    *           For each {@link ResourceInjectionTargetMetaData} within the {@link ResourceInjectionMetaData}
    *           this method creates a corresponding {@link InjectionPoint}. 
    *           The {@link InjectionPoint} and the {@link ContextValueRetriever} is then used to create an {@link Injector}
    *       </li>   
    * </p>
    * 
    * @param <M> Iterable {@link ResourceInjectionMetaData}
    * @param <T> {@link ResourceInjectionMetaData}
    * @param enc The ENC {@link Context} of the component being processed
    * @param cl The classloader of the component being processed
    * @param injections The injections being processed
    * @return
    * @throws Exception
    */
   private <M extends Iterable<T>, T extends ResourceInjectionMetaData> List<Injector<Object>> process(Context enc,
         ClassLoader cl, M injections) throws Exception
   {
      List<Injector<Object>> injectors = new ArrayList<Injector<Object>>();
      if (injections == null)
      {
         return injectors;
      }
      for (T injection : injections)
      {
         // get the enc name
         String encName = injection.getName();
         // create a ENC retriever for the enc name and the ENC context
         ValueRetriever<Object> encValRetriever = new ContextValueRetriever<Object>(enc, encName);
         // get the injection target
         Set<ResourceInjectionTargetMetaData> injectionTargets = injection.getInjectionTargets();
         if (injectionTargets == null || injectionTargets.isEmpty())
         {
            continue;
         }
         for (ResourceInjectionTargetMetaData injectionTarget : injectionTargets)
         {
            Class<?> targetClass = cl.loadClass(injectionTarget.getInjectionTargetClass());
            // field/method name
            String targetName = injectionTarget.getInjectionTargetName();
            // find the correct injection point (field or method)
            InjectionPoint<Object, Object> injectionPoint = getInjectionPoint(targetClass, targetName);
            // create an injector
            Injector<Object> injector = new Injector<Object>(injectionPoint, encValRetriever);
            
            // add this injector to the injectors to be returned
            injectors.add(injector);
         }
      }
      return injectors;
   }

   /**
    * Find the correct {@link InjectionPoint} for the passed <code>injectionTargetClass</code>
    * and the <code>injectionTargetName</code>
    * 
    * @param injectionTargetClass
    * @param injectionTargetName
    * @return
    */
   private InjectionPoint<Object, Object> getInjectionPoint(Class<?> injectionTargetClass, String injectionTargetName)
   {
      // first look for JavaBean style method
      // TODO: See if there's a utility already available to do this
      // and add more validations (what would happen if 2 overloaded setters
      // were found?)
      String methodName = "set" + injectionTargetName;
      Method[] methods = injectionTargetClass.getDeclaredMethods();
      for (Method m : methods)
      {
         if (Modifier.isPrivate(m.getModifiers()))
         {
            continue;
         }
         if (m.getParameterTypes().length != 1)
         {
            continue;
         }
         if (m.getName().equals(methodName))
         {
            return new MethodInjectionPoint<Object, Object>(m);
         }
      }
      // the target is not a method, so let's try a field
      // now
      try
      {
         Field field = injectionTargetClass.getDeclaredField(injectionTargetName);
         return new FieldInjectionPoint<Object, Object>(field);
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);
      }
      catch (NoSuchFieldException e)
      {
         // we couldn't find a target for injection
         throw new RuntimeException("Injection target not found on class " + injectionTargetClass + " and target name "
               + injectionTargetName);
      }
   }
   
   
}
