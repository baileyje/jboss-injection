package org.jboss.injection.inject.naming;

import org.jboss.injection.inject.spi.Injector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;

/**
 * Populates an ENC context based on a set of Injectors
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class SwitchBoardOperator {

   private final List<Injector<Context>> injectors;

   /**
    * Create with the set of injectors
    * 
    * @param injectors Injectors used to populate the context
    */
   public SwitchBoardOperator(final List<Injector<Context>> injectors) {
      this.injectors = injectors;
   }

   /**
    * Called when this bean's dependencies are met.  
    *
    * @throws RuntimeException If any problems occur population the context.
    */
   public void start() throws RuntimeException {
      if(injectors == null)
         return;
      try {
         final Context enc = getEnc();
         for(Injector<Context> injection : injectors) {
            injection.inject(enc);
         }
      } catch(NamingException exception) {
         throw new RuntimeException("Failed to populate ENC", exception);
      }
   }

   /**
    * Get the correct ENC.
    *
    * @return The ENC
    * @throws NamingException if any problems occur obtaining the ENC
    */
   private Context getEnc() throws NamingException {
      return new InitialContext(); // TODO: Use ENCFactory or something
   }
}
