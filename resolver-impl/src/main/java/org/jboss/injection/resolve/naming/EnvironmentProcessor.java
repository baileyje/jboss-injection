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

import org.jboss.injection.resolve.spi.DuplicateReferenceValidator;
import org.jboss.injection.resolve.spi.DuplicateReferenceValidator.ReferenceResultPair;
import org.jboss.injection.resolve.spi.EnvironmentMetaDataVisitor;
import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.metadata.javaee.spec.Environment;

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
   private Map<Class<?>, DuplicateReferenceValidator<?>> duplicateReferenceValidators;

   /**
    * Construct a new processor.  There will be no visitors or resolvers available.
    */
   public EnvironmentProcessor()
   {
      this(new LinkedList<EnvironmentMetaDataVisitor<?>>(), new HashMap<Class<?>, Resolver<?, C, ?>>(),
         new HashMap<Class<?>, DuplicateReferenceValidator<?>>());
   }

   /**
    * Construct a new processor with a Map of resolvers.
    *
    * @param visitors A list of visitors
    * @param resolvers A map from class to Resolver
    * @param validators A map from clsss to DuplicateReferenceValidator 
    */
   public EnvironmentProcessor(final List<EnvironmentMetaDataVisitor<?>> visitors,
      final Map<Class<?>, Resolver<?, C, ?>> resolvers, Map<Class<?>, DuplicateReferenceValidator<?>> validators)
   {
      this.visitors = visitors;
      this.resolvers = resolvers;
      this.duplicateReferenceValidators = validators;
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
      mappedresults.add(referenceType, reference, result);
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

   public <M> void addDuplicateReferenceValidator(final DuplicateReferenceValidator<M> duplicateReferenceValidator)
   {
      Class<M> metaDataType = duplicateReferenceValidator.getMetaDataType();
      duplicateReferenceValidators.put(metaDataType, duplicateReferenceValidator);
   }

   private class MappedResults
   {
      private final Map<String, ReferenceResultPair<?>> referenceMap = new HashMap<String, ReferenceResultPair<?>>();
      private final List<ResolverResult<?>> results = new LinkedList<ResolverResult<?>>();

      public <M> void add(Class<M> referenceType, final M newReference, final ResolverResult result) throws ResolutionException
      {
         final ReferenceResultPair<M> newReferenceResult =  new ReferenceResultPair<M>(newReference, result);
         final ReferenceResultPair<?> previousReferenceResult = referenceMap.put(result.getRefName(), newReferenceResult);
         if(previousReferenceResult == null)
         {
            results.add(result);
         }
         else
         {
            checkForConflict(referenceType, previousReferenceResult, newReferenceResult);
         }
      }

      /**
       * This method is used to determine if two environment entries
       * that resolve to the same JNDI reference point are interchangeable.
       * This method will be called anytime there are duplicate ResolverResults
       * created from separate references.
       *
       * @param referenceType The type of reference being processed
       * @param previousReferencePair The previous reference
       * @param newReferencePair      The new reference
       * @throws ResolutionException if a the reference conflict with one another
       */
      private <M> void checkForConflict(Class<M> referenceType, ReferenceResultPair<?> previousReferencePair, ReferenceResultPair<M> newReferencePair) throws ResolutionException
      {
         final String envReferenceName = previousReferencePair.getResolverResult().getRefName();

         if(!previousReferencePair.getReference().getClass().isAssignableFrom(referenceType))
         {
            throw new ResolutionException("Conflicting references found during resolution.  The references ["
               + previousReferencePair.getReference() + ", " + newReferencePair.getReference()
               + "] resolve to the same JNDI name [" + envReferenceName
               + "] and are not of the same reference type.");
         }

         final ReferenceResultPair<M> typedPreviousReferencePair = (ReferenceResultPair<M>) previousReferencePair;

         DuplicateReferenceValidator<M> duplicateReferenceValidator = (DuplicateReferenceValidator<M>) duplicateReferenceValidators.get(referenceType);

         if(duplicateReferenceValidator == null)
            duplicateReferenceValidator = new ResultOnlyReferenceValidator<M>(referenceType);

         if(!duplicateReferenceValidator.isValid(typedPreviousReferencePair, newReferencePair))
         {
            throw new ResolutionException("Conflicting environment entries were found during resolution.  The references ["
               + previousReferencePair.getReference() + ", " + newReferencePair.getReference()
               + "] resolve to the same JNDI name " + envReferenceName
               + "] and were not found to result in the same environment entry value.");
         }
      }
    }
}
