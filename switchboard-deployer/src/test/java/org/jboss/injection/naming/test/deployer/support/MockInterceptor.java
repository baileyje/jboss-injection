package org.jboss.injection.naming.test.deployer.support;

import javax.annotation.PostConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class MockInterceptor
{
   @AroundInvoke
   public Object mdbInterceptor(InvocationContext ctx) throws Exception
   {
      System.out.println("*** Intercepting call");
      return ctx.proceed();
   }

}
