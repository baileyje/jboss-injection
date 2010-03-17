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
import org.jboss.logging.Logger;
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

import java.util.Arrays;
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
   private static final Logger log = Logger.getLogger("org.jboss.injection.resolve");

   private Map<Class<?>, Resolver<?, C>> resolvers;

   private boolean allowMissingResolver;

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
    * @param context The context in which to resolve (usually DeploymentUnit)
    * @param environments Environments to process references for
    * @return The resolver results
    */
   public List<ResolverResult> process(C context, Environment... environments) throws ResolutionException
   {
      return process(context, Arrays.asList(environments));
   }

   /**
    * Processes the Environment and returns the resolver results.
    *
    * @param context The context in which to resolve (usually DeploymentUnit)
    * @param environments Environments to process references for
    * @return The resolver results
    */
   public List<ResolverResult> process(C context, Iterable<Environment> environments) throws ResolutionException {
      final MappedResults mappedresults = new MappedResults();

      for(Environment environment : environments)
      {
         // TODO: configurable via visitors
         // references
         process(context, environment.getEjbLocalReferences(), EJBLocalReferenceMetaData.class, mappedresults);
         process(context, environment.getEjbReferences(), EJBReferenceMetaData.class, mappedresults);
         process(context, environment.getEnvironmentEntries(), EnvironmentEntryMetaData.class, mappedresults);
         process(context, environment.getMessageDestinationReferences(), MessageDestinationReferenceMetaData.class, mappedresults);
         process(context, environment.getPersistenceUnitRefs(), PersistenceUnitReferenceMetaData.class, mappedresults);
         process(context, environment.getResourceReferences(), ResourceReferenceMetaData.class, mappedresults);
         process(context, environment.getResourceEnvironmentReferences(), ResourceEnvironmentReferenceMetaData.class, mappedresults);
         process(context, environment.getServiceReferences(), ServiceReferenceMetaData.class, mappedresults);

         // TODO: data sources
         // environment.getDataSources()
      }
      return mappedresults.results;
   }


   protected <M extends Iterable<T>, T extends ResourceInjectionMetaData> void process(C context, M references, Class<T> childType, MappedResults mappedresults) throws ResolutionException
   {
      if(references == null)
         return;
      for(T reference : references)
      {
         process(context, reference, childType, mappedresults);
      }
   }

   protected <M extends ResourceInjectionMetaData> void process(C context, M reference, Class<M> referenceType, MappedResults mappedresults) throws ResolutionException
   {
      if(reference == null)
         return;

      final Resolver<M, C> resolver = getResolver(referenceType);

      if(resolver == null)
      {
         if(allowMissingResolver)
         {
            log.warnf("Found reference [%s] but no Resolver could be found for type [%s]", reference, referenceType);
            return;
         }
         else
         {
            throw new IllegalStateException("Found reference [" + reference + "] but no Resolver could be found for type [" + referenceType + "]");
         }
      }

      final ResolverResult result = resolver.resolve(context, reference);
      if(result == null)
         throw new ResolutionException("Found reference [" + reference + "] but resolution failed to produce a result");
      mappedresults.add(resolver, reference, result);
   }

   @SuppressWarnings("unchecked")
   protected <M> Resolver<M, C> getResolver(Class<M> metaDataType)
   {
      return (Resolver<M, C>) resolvers.get(metaDataType);
   }

   public void addResolver(final Resolver<?, C> resolver)
   {
      Class<?> metaDataType = resolver.getMetaDataType();
      resolvers.put(metaDataType, resolver);
   }

   public void setAllowMissingResolver(final boolean allowMissingResolver)
   {
      this.allowMissingResolver = allowMissingResolver;
   }

   private static class MappedResults<M, C>
   {
      private final Map<String, M> referenceMap = new HashMap<String, M>();
      private final List<ResolverResult> results = new LinkedList<ResolverResult>();

      public void add(final Resolver<M, ?> resolver, final M newReference, final ResolverResult result) throws ResolutionException
      {
         M previousRef = referenceMap.put(result.getRefName(), newReference);
         if(previousRef == null)
         {
            results.add(result);
         }
         else
         {
            checkForConflict(previousRef, newReference);
         }
      }

      /**
       * This method is used to determine if two environment entries
       * that resolve to the same JNDI reference point are interchangeable.
       * This method will be called anytime there are duplicate ResolverResults
       * created from separate references.
       *
       * TODO: Update to check shareable and authentication @see EE.6 5.2.2
       *
       * @param previousReference The previous reference
       * @param newReference The new reference
       * @Throws ResolutionException if a the reference conflict with one another
       */
      private void checkForConflict(M previousReference, M newReference) throws ResolutionException
      {
         if(newReference instanceof EnvironmentEntryMetaData)
         {
            final EnvironmentEntryMetaData newEnvironmentEntry = EnvironmentEntryMetaData.class.cast(newReference);
            final EnvironmentEntryMetaData previousEnvironmentEntry = EnvironmentEntryMetaData.class.cast(previousReference);

            boolean conflict = !newEnvironmentEntry.getType().equals(previousEnvironmentEntry.getType());
            conflict = conflict || !newEnvironmentEntry.getValue().equals(previousEnvironmentEntry.getValue());
            if(conflict)
            {
               throw new ResolutionException("Conflicting environment entries were found during resolution.  The following references resolve to the same JNDI name: [" + previousReference + ", " + newReference + "]");
            }
         }
         else
         {
            throw new ResolutionException("Conflicting references found during resolution.  The following references resolve to a single JNDI name: [" + previousReference + ", " + newReference + "]");
         }
      }
   }
}
