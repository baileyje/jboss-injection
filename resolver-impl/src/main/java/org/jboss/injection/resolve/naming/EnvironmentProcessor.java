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

import org.jboss.injection.resolve.spi.EnvironmentMetaDataVisitor;
import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;

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
   private Map<Class<?>, Resolver<?, C, ?>> resolvers;
   private List<EnvironmentMetaDataVisitor<?>> visitors;

   /**
    * Construct a new processor.  There will be no visitors or resolvers available.
    */
   public EnvironmentProcessor()
   {
      this(new LinkedList<EnvironmentMetaDataVisitor<?>>(), new HashMap<Class<?>, Resolver<?, C, ?>>());
   }

   /**
    * Construct a new processor with a Map of resolvers.
    *
    * @param visitors A list of visitors
    * @param resolvers A map from class to Resolver
    */
   public EnvironmentProcessor(final List<EnvironmentMetaDataVisitor<?>> visitors, final Map<Class<?>, Resolver<?, C, ?>> resolvers)
   {
      this.visitors = visitors;
      this.resolvers = resolvers;
   }

   /**
    * Processes the Environment and returns the resolver results.
    *
    * @param context The context in which to resolve (usually DeploymentUnit)
    * @param environments Environments to process references for
    * @return The resolver results
    * @throws ResolutionException if any resolution problems occur
    */
   public List<ResolverResult<?>> process(C context, Environment... environments) throws ResolutionException
   {
      return process(context, Arrays.asList(environments));
   }

   /**
    * Processes the Environment and returns the resolver results.
    *
    * @param context The context in which to resolve (usually DeploymentUnit)
    * @param environments Environments to process references for
    * @return The resolver results
    * @throws ResolutionException if any resolution problems occur
    */
   public List<ResolverResult<?>> process(C context, Iterable<Environment> environments) throws ResolutionException {
      final MappedResults mappedresults = new MappedResults();

      for(Environment environment : environments)
      {
         for(EnvironmentMetaDataVisitor<?> visitor : visitors)
         {
            process(context, environment, visitor, mappedresults);
         }
      }
      return mappedresults.results;
   }


   protected <M> void process(C context, Environment environment, EnvironmentMetaDataVisitor<M> visitor, MappedResults mappedresults) throws ResolutionException
   {
      final Iterable<M> references = visitor.getMetaData(environment);
      if(references == null)
         return;
      for(M reference : references)
      {
         process(context, reference, visitor.getMetaDataType(), mappedresults);
      }
   }

   protected <M> void process(C context, M reference, Class<M> referenceType, MappedResults mappedresults) throws ResolutionException
   {
      if(reference == null)
         return;

      final Resolver<M, C, ?> resolver = getResolver(referenceType);

      if(resolver == null)
      {
         throw new ResolutionException("Found reference [" + reference + "] but no Resolver could be found for type [" + referenceType + "]");
      }

      final ResolverResult<?> result = resolver.resolve(context, reference);
      if(result == null)
         throw new ResolutionException("Found reference [" + reference + "] but resolution failed to produce a result");
      mappedresults.add(reference, result);
   }

   @SuppressWarnings("unchecked")
   protected <M, C> Resolver<M, C, ?> getResolver(Class<M> metaDataType)
   {
      return (Resolver<M, C, ?>) resolvers.get(metaDataType);
   }

   public void addMetaDataVisitor(final EnvironmentMetaDataVisitor<?> visitor)
   {
      visitors.add(visitor);
   }

   public <M> void addResolver(final Resolver<M, C, ?> resolver)
   {
      Class<M> metaDataType = resolver.getMetaDataType();
      resolvers.put(metaDataType, resolver);
   }

   private static class MappedResults
   {
      private final Map<String, Object> referenceMap = new HashMap<String, Object>();
      private final List<ResolverResult<?>> results = new LinkedList<ResolverResult<?>>();

      public <M> void add(final M newReference, final ResolverResult result) throws ResolutionException
      {
         Object previousRef = referenceMap.put(result.getRefName(), newReference);
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
       * @throws ResolutionException if a the reference conflict with one another
       */
      private <M> void checkForConflict(M previousReference, M newReference) throws ResolutionException
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
