package org.jboss.injection.resolve.enc;

import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.metadata.javaee.spec.AbstractEJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferencesMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EnvironmentProcessor -
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EnvironmentProcessor {

   public Map<Class<?>, Resolver<?>> resolvers = new HashMap<Class<?>, Resolver<?>>();

   public void process(Environment environment) {
      process(environment.getEjbReferences());

   }

   private void process(EJBReferencesMetaData ejbLocalRefs) {
      for(EJBReferenceMetaData metaData : ejbLocalRefs) {
         process(metaData);
      }

   }

   private void process(final EJBReferenceMetaData reference) {
      Resolver<EJBReferenceMetaData> ejbReferenceResolver = getResolver(EJBReferenceMetaData.class);

      if(ejbReferenceResolver == null)
         throw new IllegalStateException("Found EJB reference " + reference + ", but no EJB reference resolver has been set");

      ResolverResult result = ejbReferenceResolver.resolve(null, reference);
   }

   private <M> Resolver<M> getResolver(Class<M> metaDataType) {
      return (Resolver<M>) resolvers.get(metaDataType);
   }

}
