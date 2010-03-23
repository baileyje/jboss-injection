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
package org.jboss.injection.naming.deployer;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.plugins.AbstractInjectionValueMetaData;
import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.inject.naming.ContextInjectionPoint;
import org.jboss.injection.inject.naming.SwitchBoardOperator;
import org.jboss.injection.inject.Injector;
import org.jboss.injection.inject.pojo.GenericValueRetriever;
import org.jboss.injection.inject.spi.ValueRetriever;
import org.jboss.injection.naming.switchboard.SwitchBoardComponentMetaData;
import org.jboss.injection.naming.switchboard.SwitchBoardMetaData;
import org.jboss.injection.resolve.naming.EnvironmentProcessor;
import org.jboss.injection.resolve.naming.ResolutionException;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.logging.Logger;
import org.jboss.reloaded.naming.deployers.javaee.JavaEEComponentInformer;

import javax.naming.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deployer capable of creating SwitchBoardOperator beans from SwitchBoardMetaData.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class SwitchBoardOperatorDeployer extends AbstractSimpleRealDeployer<SwitchBoardMetaData>
{
   private static final Logger log = Logger.getLogger(SwitchBoardOperatorDeployer.class);

   private JavaEEComponentInformer componentInformer;

   private EnvironmentProcessor<DeploymentUnit> environmentProcessor;

   /**
    * Create the deployer and setup the inputs
    */
   public SwitchBoardOperatorDeployer()
   {
      super(SwitchBoardMetaData.class);
      setOutput(BeanMetaData.class);
   }

   /**
    * Deploy a list of Environments as a single SwitchBoardOperator
    *
    * @param unit                The deployment unit
    * @param switchBoardMetaData The switchboard metadata
    * @throws DeploymentException if any deployment issues occur
    */
   public void deploy(final DeploymentUnit unit, final SwitchBoardMetaData switchBoardMetaData) throws DeploymentException
   {
      final EnvironmentProcessor<DeploymentUnit> environmentProcessor = getEnvironmentProcessor();
      if(environmentProcessor == null)
         throw new IllegalStateException("SwitchBoardOperator deployers require an EnvironmentPorcessor, which has not been set.");

      //First the module level entries
      try
      {
         List<ResolverResult<?>> results = environmentProcessor.process(unit, switchBoardMetaData);
         if(results != null && !results.isEmpty())
         {
            deployBeanMetaData(unit, null, results);
         }
      }
      catch(ResolutionException e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Failed to resolve module level references for " + unit, e);
      }

      //Now the component level entries
      if(switchBoardMetaData.getComponents() == null)
         return;
      for(SwitchBoardComponentMetaData componentMetaData : switchBoardMetaData.getComponents())
      {
         try
         {

            List<ResolverResult<?>> results = environmentProcessor.process(unit, componentMetaData);
            if(results != null && !results.isEmpty())
            {
               deployBeanMetaData(unit, componentMetaData.getComponentName(), results);
            }
         }
         catch(ResolutionException e)
         {
            throw DeploymentException.rethrowAsDeploymentException("Failed to resolve references for component " + componentMetaData.getComponentName() + " in " + unit, e);
         }
      }


   }

   /**
    * Deploy the BeanMetaData for the SwitchBoardOperator
    *
    * @param unit            The deploymentUnit
    * @param componentName   The component name
    * @param resolverResults The list of resolver results
    */
   protected void deployBeanMetaData(final DeploymentUnit unit, final String componentName, final List<ResolverResult<?>> resolverResults)
   {
      final String name = getBeanName(unit, componentName);

      final BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(name, SwitchBoardOperator.class.getName());

      final ValueMetaData contextValueMetaData = createContextValueMetaData(unit, componentName);
      builder.addConstructorParameter(Context.class.getName(), contextValueMetaData);

      final List<Injector<Context>> injectors = createInjectors(resolverResults);
      builder.addConstructorParameter(List.class.getName(), injectors);

      for(ResolverResult resolverResult : resolverResults)
      {
         final String beanName = resolverResult.getBeanName();
         if(beanName != null)
         {
            builder.addDependency(beanName);
         }

      }

      final BeanMetaData beanMetaData = builder.getBeanMetaData();
      unit.getTopLevel().addAttachment(BeanMetaData.class.getName() + "." + name, beanMetaData, BeanMetaData.class);
      log.debugf("Deploying SwitchBoardOperator [%s] for deployment [%s]", name, unit);
   }

   /**
    * Create the metdata required to access the correct Context for this deployment.
    *
    * @param unit          The deploymentUnit
    * @param componentName The component name
    * @return The injection value metadata
    */
   protected ValueMetaData createContextValueMetaData(final DeploymentUnit unit, String componentName)
   {
      final String contextBeanName = "jboss.naming:" + getBeanNameQualifier(unit, componentName);
      return new AbstractInjectionValueMetaData(contextBeanName, "context");
   }

   /**
    * Create a list of injectors based on a list of resolver results.
    *
    * @param resolverResults The list of resolver results
    * @return A list of injectors
    */
   protected List<Injector<Context>> createInjectors(final List<ResolverResult<?>> resolverResults)
   {
      final List<Injector<Context>> injectors = new ArrayList<Injector<Context>>(resolverResults.size());
      for(ResolverResult<?> resolverResult : resolverResults)
      {
         final Injector<Context> injector = createInjector(resolverResult);
         injectors.add(injector);
      }
      return injectors;
   }

   /**
    * Create an injector for a specified resolver result
    *
    * @param resolverResult The resolver result to create an injection for
    * @param <V>            The value type for the resolver result
    * @return An injector
    */
   protected <V> Injector<Context> createInjector(ResolverResult<V> resolverResult)
   {
      final ContextInjectionPoint<V> injectionPoint = new ContextInjectionPoint<V>(resolverResult.getRefName());
      final V value = resolverResult.getValue();
      final ValueRetriever<V> valueRetriever = new GenericValueRetriever<V>(value);
      return new Injector<Context>(injectionPoint, valueRetriever);
   }

   /**
    * Create the bean name
    *
    * @param deploymentUnit The deployment unit
    * @param componentName  The component name
    * @return The bean name to use
    */
   protected String getBeanName(final DeploymentUnit deploymentUnit, final String componentName)
   {
      return "jboss.naming:service=SwitchBoardOperator," + getBeanNameQualifier(deploymentUnit, componentName);
   }

   /**
    * Create the qualifier used in the bean name.
    *
    * @param deploymentUnit The deployment unit
    * @param componentName  The component name
    * @return The bean name to use
    */
   protected String getBeanNameQualifier(final DeploymentUnit deploymentUnit, final String componentName)
   {
      final String applicationName = componentInformer.getApplicationName(deploymentUnit);
      final String moduleName = componentInformer.getModulePath(deploymentUnit);

      final StringBuilder builder = new StringBuilder();
      if(applicationName != null)
      {
         builder.append("application=").append(applicationName).append(",");
      }
      builder.append("module=").append(moduleName);
      if(componentName != null)
      {
         builder.append(",component=").append(componentName);
      }
      return builder.toString();
   }

   /**
    * Get the environment processor
    *
    * @return The environment processor
    */
   protected EnvironmentProcessor<DeploymentUnit> getEnvironmentProcessor()
   {
      return environmentProcessor;
   }

   /**
    * Set the environment processor
    *
    * @param environmentProcessor The environment processor
    */
   @Inject
   public void setEnvironmentProcessor(final EnvironmentProcessor<DeploymentUnit> environmentProcessor)
   {
      this.environmentProcessor = environmentProcessor;
   }

   /**
    * Set the component informer
    *
    * @param componentInformer The component informer
    */
   @Inject
   public void setComponentInformer(final JavaEEComponentInformer componentInformer)
   {
      this.componentInformer = componentInformer;
   }
}
