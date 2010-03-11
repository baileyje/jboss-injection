package org.jboss.injection.inject.enc;

import org.jboss.injection.inject.spi.Injector;

import javax.naming.Context;
import java.util.List;

/**
 * TODO: Rename me.....
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EncPopulator {

   private final List<Injector<Context>> injectors;
   private final Context enc;

   public EncPopulator(final Context enc, final List<Injector<Context>> injectors) {
      if(enc == null) throw new IllegalArgumentException("ENC context is required");
      this.enc = enc;
      this.injectors = injectors;
   }

   public void start() {
      if(injectors == null)
         return;
      for(Injector<Context> injection : injectors) {
         injection.inject(enc);
      }
   }
}
