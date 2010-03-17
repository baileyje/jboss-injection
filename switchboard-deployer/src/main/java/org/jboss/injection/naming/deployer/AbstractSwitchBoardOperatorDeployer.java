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
import org.jboss.injection.inject.InjectorFactory;
import org.jboss.injection.inject.naming.ContextInjectionPoint;
import org.jboss.injection.inject.naming.SwitchBoardOperator;
import org.jboss.injection.inject.spi.Injector;
import org.jboss.injection.inject.spi.ValueRetriever;
import org.jboss.injection.resolve.naming.EnvironmentProcessor;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.Environment;

import javax.naming.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * Deployer capable of creating SwitchBoardOperator beans from Environment MetaData.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class AbstractSwitchBoardOperatorDeployer<M> extends AbstractSimpleRealDeployer<M>
{
   private static final Logger log = Logger.getLogger("org.jboss.injection.inject.naming");
   
   private EnvironmentProcessor<DeploymentUnit> environmentProcessor;

   /**
    * Create the deployer and setup the inputs
    */
   public AbstractSwitchBoardOperatorDeployer(Class<M> metaDataType)
   {
      super(metaDataType);
      setOutput(BeanMetaData.class);
   }

   /**
    * Deploy a list of Environments as a single SwitchBoardOperator
    *
    * @param unit The deployment unit
    * @param environments  The list of environments to process
    * @throws DeploymentException if any deployment issues occur
    */
   protected void deploy(final DeploymentUnit unit, final List<Environment> environments) throws DeploymentException
   {
      final EnvironmentProcessor<DeploymentUnit> environmentProcessor = getEnvironmentProcessor();
      if(environmentProcessor == null)
         throw new IllegalStateException("SwitchBoardOperator deployers require an EnvironmentPorcessor, which has not been set.");

      final List<ResolverResult> allResults = new ArrayList<ResolverResult>();
      for(Environment environment : environments)
      {
         final List<ResolverResult> resolverResults = environmentProcessor.process(unit, environment);
         allResults.addAll(resolverResults);
      }

      if(!allResults.isEmpty())
      {
         final BeanMetaData beanMetaData = createBeanMetaData(unit, allResults);
         unit.getTopLevel().addAttachment(BeanMetaData.class.getName() + "." + beanMetaData.getName(), beanMetaData, BeanMetaData.class);
         log.debugf("Deploying SwitchBoardOperator [%s] for deployment [%s]", beanMetaData.getName(), unit);
      }
   }

   /**
    * Create the BeanMetaData for the SwitchBoardOperator
    *
    * @param unit The deploymentUnit
    * @param resolverResults The list of resolver results
    * @return The BeanMetaData
    */
   protected BeanMetaData createBeanMetaData(final DeploymentUnit unit, final List<ResolverResult> resolverResults)
   {
      final String name = getBeanName(unit);

      final BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(name, SwitchBoardOperator.class.getName());

      final ValueMetaData contextValueMetaData = createContextValueMetaData(unit);
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
      return builder.getBeanMetaData();
   }

   /**
    * Create the metdata required to access the correct Context for this deployment.
    *
    * @param unit The deploymentUnit
    * @return The injection value metadata
    */
   protected ValueMetaData createContextValueMetaData(final DeploymentUnit unit)
   {
      final String contextBeanName = "jboss.naming:" + getBeanNameQualifier(unit);
      return new AbstractInjectionValueMetaData(contextBeanName, "context");
   }

   /**
    * Create a list of injectors based on a list of resolver results.
    *
    * @param resolverResults The list of resolver results
    * @return A list of injectors
    */
   protected List<Injector<Context>> createInjectors(final List<ResolverResult> resolverResults)
   {
      final List<Injector<Context>> injectors = new ArrayList<Injector<Context>>(resolverResults.size());
      for(ResolverResult resolverResult : resolverResults)
      {
         final ContextInjectionPoint injectionPoint = new ContextInjectionPoint(resolverResult.getRefName());
         final ValueRetriever valueRetriever = resolverResult.getValueRetriever();
         final Injector<Context> injector = InjectorFactory.create(injectionPoint, valueRetriever);
         injectors.add(injector);
      }
      return injectors;
   }

   /**
    * Create the bean name
    *
    * @param deploymentUnit The deployment unit
    * @return The bean name to use
    */
   protected String getBeanName(final DeploymentUnit deploymentUnit)
   {
      return "jboss.naming:service=SwitchBoardOperator," + getBeanNameQualifier(deploymentUnit);
   }

   /**
    * Create the qualifier used in the bean name.
    *
    * @param deploymentUnit The deployment unit
    * @return The bean name to use
    */
   protected abstract String getBeanNameQualifier(final DeploymentUnit deploymentUnit);


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
}
