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
package org.jboss.injection.resolve.naming;

import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.metadata.javaee.spec.EJBLocalReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.PersistenceUnitReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Process an {@link Environment} instance and creates all the {@link ResolverResult}
 * instances for all references that can be resolved.
 * <p/>
 * C is the resolving context (usually DeploymentUnit)
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EnvironmentProcessor<C>
{

   public Map<Class<?>, Resolver<?, C>> resolvers;

   /**
    * Construct a new processor.  There will be no resolvers available.
    */
   public EnvironmentProcessor()
   {
      this(new HashMap<Class<?>, Resolver<?, C>>());
   }

   /**
    * Construct a new processor with a Map of resolvers.
    *
    * @param resolvers A map from class to Resolver
    */
   public EnvironmentProcessor(final Map<Class<?>, Resolver<?, C>> resolvers)
   {
      this.resolvers = resolvers;
   }

   /**
    * Processes the Environment and returns the resolver results.
    *
    * @param environment An Environment to process references for
    * @return The resolver results
    * @deprecated specifying a context will become mandatory
    */
   @Deprecated
   public List<ResolverResult> process(Environment environment)
   {
      return process(null, environment);
   }

   /**
    * Processes the Environment and returns the resolver results.
    *
    * @param context     the context in which to resolve (usually DeploymentUnit)
    * @param environment An Environment to process references for
    * @return The resolver results
    */
   public List<ResolverResult> process(C context, Environment environment)
   {
      final List<ResolverResult> results = new LinkedList<ResolverResult>();

      // TODO: configurable via visitors
      // references
      process(context, environment.getEjbLocalReferences(), EJBLocalReferenceMetaData.class, results);
      process(context, environment.getEjbReferences(), EJBReferenceMetaData.class, results);
      process(context, environment.getEnvironmentEntries(), EnvironmentEntryMetaData.class, results);
      process(context, environment.getMessageDestinationReferences(), MessageDestinationReferenceMetaData.class, results);
      process(context, environment.getPersistenceUnitRefs(), PersistenceUnitReferenceMetaData.class, results);
      process(context, environment.getResourceReferences(), ResourceReferenceMetaData.class, results);
      process(context, environment.getResourceEnvironmentReferences(), ResourceEnvironmentReferenceMetaData.class, results);
      process(context, environment.getServiceReferences(), ServiceReferenceMetaData.class, results);

      // TODO: data sources
      // environment.getDataSources()
      return results;
   }

   protected <M extends Iterable<T>, T extends ResourceInjectionMetaData> void process(C context, M references, Class<T> childType, List<ResolverResult> results)
   {
      if(references == null)
         return;
      for(T reference : references)
      {
         process(context, reference, childType, results);
      }
   }

   protected <M extends ResourceInjectionMetaData> void process(C context, M reference, Class<M> referenceType, List<ResolverResult> results)
   {
      if(reference == null)
         return;

      final Resolver<M, C> resolver = getResolver(referenceType);

      if(resolver == null)
         throw new IllegalStateException("Found reference [" + reference + "] but no Resolver could be found for type [" + referenceType + "]");

      final ResolverResult result = resolver.resolve(context, reference);
      if(result == null)
         throw new RuntimeException("Found reference [" + reference + "] but resolution failed to produce a result");
      results.add(result);
   }

   @SuppressWarnings("unchecked")
   protected <M> Resolver<M, C> getResolver(Class<M> metaDataType)
   {
      return (Resolver<M, C>) resolvers.get(metaDataType);
   }

   public void addResolver(Resolver<?, C> resolver)
   {
      Class<?> metaDataType = resolver.getMetaDataType();
      resolvers.put(metaDataType, resolver);
   }
}
