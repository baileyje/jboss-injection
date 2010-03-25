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
package org.jboss.injection.resolve.spi;


/**
 * Used to validate whether duplicate environment references are valid.
 * Implementations should perform any reference specific checks.
 * (eg.  Sharable, Auth, etc)
 * <br />
 * See EE6 specification section 5.2.2 for more details.
 *
 * @param <M> The type of metadata
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class DuplicateReferenceValidator<M>
{
   private Class<M> metaDataType;

   public DuplicateReferenceValidator(final Class<M> metaDataType)
   {
      this.metaDataType = metaDataType;
   }

   /**
    * Get the metadata type this supports
    *
    * @return The metadata type
    */
   public Class<M> getMetaDataType()
   {
      return metaDataType;
   }

   /**
    * Validate whether the previous and new result are a valid duplication and will result
    * in the same namespace entry.
    *
    * @param previousResult The previous result that resolved to the same reference location.
    * @param newResult The previous result that resolved to the same reference location.
    * @return True if the references resolve to the same env entry value, false if they do not
    */
   public boolean isValid(final ReferenceResultPair<M> previousResult, final ReferenceResultPair<M> newResult)
   {
      // First check to make sure the results are equal.
      return previousResult.getResolverResult().equals(newResult.getResolverResult())
         && attributesEqual(previousResult.getReference(), newResult.getReference());
   }

   /**
    * Checks to see if the attributes for the reference metadata that caused
    * duplicate environment references have the same attributes (Shareable, Auth, etc)
    *
    * @param previousReference The previous reference metadate
    * @param newReference The new reference metadate
    * @return true if the reference's attributes are equal and false if not.
    */
   protected abstract boolean attributesEqual(M previousReference, M newReference);

   /**
    * Wrapper to hold onto a piece of reference metadata and the resolver result created.
    *
    * @param <M> The type of metadata
    */
   public static class ReferenceResultPair<M>
   {
      private final M reference;
      private final ResolverResult<?> resolverResult;

      public ReferenceResultPair(final M reference, final ResolverResult<?> resolverResult)
      {
         this.reference = reference;
         this.resolverResult = resolverResult;
      }

      /**
       * Get the reference metadata
       *
       * @return The reference metadata
       */
      public M getReference()
      {
         return reference;
      }

      /**
       * Get the resolver result
       *
       * @return The result
       */
      public ResolverResult<?> getResolverResult()
      {
         return resolverResult;
      }
   }
}
