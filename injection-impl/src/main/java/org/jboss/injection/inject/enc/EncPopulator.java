package org.jboss.injection.inject.enc;

import org.jboss.injection.inject.spi.Injection;

import javax.naming.Context;
import java.util.List;

/**
 * TODO: Rename me.....
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 */
public class EncPopulator {

   private final List<Injection<Context>> injections;
   private final Context enc;

   public EncPopulator(final Context enc, final List<Injection<Context>> injections) {
      if(enc == null) throw new IllegalArgumentException("ENC context is required");
      this.enc = enc;
      this.injections = injections;
   }

   public void start() {
      if(injections == null)
         return;
      for(Injection<Context> injection : injections) {
         injection.perform(enc);
      }
   }
}
