package org.jboss.injection.naming.test.deployer.support;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.reloaded.naming.deployers.javaee.JavaEEComponentInformer;

/**
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class MockJavaEEComponentInformer implements JavaEEComponentInformer
{
   public String getComponentName(final DeploymentUnit unit)
   {
      return "Component";
   }

   public boolean isJavaEEComponent(final DeploymentUnit unit)
   {
      return true;
   }

   public String getApplicationName(final DeploymentUnit deploymentUnit)
   {
      return null;
   }

   public String getModulePath(final DeploymentUnit deploymentUnit)
   {
      return "Module";
   }

   public ModuleType getModuleType(final DeploymentUnit deploymentUnit)
   {
      return null;
   }

   public String[] getRequiredAttachments()
   {
      return new String[0];
   }
}
