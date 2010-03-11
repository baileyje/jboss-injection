package org.jboss.injection.inject.enc;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.injection.inject.spi.Injector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;

/**
 * TODO: Rename me.....
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EncPopulator {

   private final List<Injector<Context>> injectors;

   public EncPopulator(final List<Injector<Context>> injectors) {
      this.injectors = injectors;
   }

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

   private Context getEnc() throws NamingException {
      return new InitialContext();
   }
}
