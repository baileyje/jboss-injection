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
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.plugins.graph.Search;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.inject.InjectorFactory;
import org.jboss.injection.inject.naming.ContextInjectionPoint;
import org.jboss.injection.inject.naming.SwitchBoardOperator;
import org.jboss.injection.inject.spi.Injector;
import org.jboss.injection.inject.spi.ValueRetriever;
import org.jboss.injection.resolve.naming.EnvironmentProcessor;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.reloaded.naming.deployers.javaee.JavaEEComponentInformer;

import javax.naming.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deployer capable of creating SwitchBoardOperator beans from Environment MetaData.
 * TODO:  This thing is becoming a mess.  This should be split between web+ejb and ejb only
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class SwitchBoardOperatorDeployer extends AbstractRealDeployer
{
   private EnvironmentProcessor<DeploymentUnit> environmentProcessor;

   private JavaEEComponentInformer componentInformer;

   /**
    * Create the deployer and setup the inputs
    */
   public SwitchBoardOperatorDeployer()
   {
      // We need to look for both EJB and WEB Metadata
      addInput(JBossMetaData.class);
      addInput(JBossWebMetaData.class);
      setOutput(BeanMetaData.class);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void internalDeploy(final DeploymentUnit unit) throws DeploymentException
   {
      if(unit.isAttachmentPresent(JBossWebMetaData.class))
      {
         final JBossWebMetaData jBossWebMetaData = unit.getAttachment(JBossWebMetaData.class);
         deploy(unit, jBossWebMetaData);
      }
      else if(unit.isAttachmentPresent(JBossMetaData.class))
      {
         final JBossMetaData jBossMetaData = unit.getAttachment(JBossMetaData.class);
         deploy(unit, jBossMetaData);
      }
   }

   /**
    * Deploy with EJB metadata only.  Will create a separate switchboard for each component.
    *
    * @param unit The deployment unit
    * @param metaData The metadata to process
    * @throws DeploymentException if any deployment issues occur
    */
   protected void deploy(final DeploymentUnit unit, final JBossMetaData metaData) throws DeploymentException
   {
      final JBossEnterpriseBeansMetaData jBossEnterpriseBeansMetaData = metaData.getEnterpriseBeans();
      for(JBossEnterpriseBeanMetaData jBossEnterpriseBeanMetaData : jBossEnterpriseBeansMetaData)
      {
         deploy(unit, false, Collections.singletonList((Environment) jBossEnterpriseBeanMetaData));
      }
   }

   /**
    * Deploy with WEB (and maybe EJB) metadata.  Will create a single switchboard for all components.
    *
    * @param unit The deployment unit
    * @param metaData The metadata to process
    * @throws DeploymentException if any deployment issues occur
    */
   protected void deploy(final DeploymentUnit unit, final JBossWebMetaData metaData) throws DeploymentException
   {
      final List<Environment> environments = new ArrayList<Environment>();
      environments.add(metaData.getJndiEnvironmentRefsGroup());

      if(unit.isAttachmentPresent(JBossMetaData.class))
      {
         final JBossMetaData jBossMetaData = unit.getAttachment(JBossMetaData.class);
         environments.addAll(jBossMetaData.getEnterpriseBeans());
      }
      deploy(unit, true, environments);
   }

   /**
    * Deploy a list of Environments as a single SwitchBoardOperator
    *
    * @param unit The deployment unit
    * @param webDeployment Whether or not this is a web deployment
    * @param environments  The list of environments to process
    * @throws DeploymentException if any deployment issues occur
    */
   protected void deploy(final DeploymentUnit unit, final boolean webDeployment, final List<Environment> environments) throws DeploymentException
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
         final BeanMetaData beanMetaData = createBeanMetaData(unit, webDeployment, allResults);
         unit.getTopLevel().addAttachment(BeanMetaData.class.getName() + "." + beanMetaData.getName(), beanMetaData, BeanMetaData.class);
      }
   }

   /**
    * Create the BeanMetaData for the SwitchBoardOperator
    *
    * @param unit The deploymentUnit
    * @param resolverResults The list of resolver results
    * @return The BeanMetaData
    */
   protected BeanMetaData createBeanMetaData(final DeploymentUnit unit, final boolean webDeployment, final List<ResolverResult> resolverResults)
   {
      final String name = createBeanName(unit, !webDeployment);

      final BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(name, SwitchBoardOperator.class.getName());

      /* TODO: Find out why adding Scope annotations causes problems undeploying beans */

      AbstractInjectionValueMetaData contextInjectionValueMetaData = null;
      if(webDeployment)
      {
         contextInjectionValueMetaData = new AbstractInjectionValueMetaData("java:module", "context");
      }
      else
      {
         contextInjectionValueMetaData = new AbstractInjectionValueMetaData("java:comp", "context");
      }
      contextInjectionValueMetaData.setSearch(Search.LOCAL);

      builder.addConstructorParameter(Context.class.getName(), contextInjectionValueMetaData);
      builder.addConstructorParameter(List.class.getName(), createInjectors(resolverResults));

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
    * Create the bean name
    * 
    * @param includeComponent Whether to include component name
    * @param deploymentUnit The deployment unit
    * @return The bean name to use
    */
   protected String createBeanName(final DeploymentUnit deploymentUnit, final boolean includeComponent)
   {
      String applicationName = componentInformer.getApplicationName(deploymentUnit);
      String moduleName = componentInformer.getModulePath(deploymentUnit);

      final StringBuilder builder = new StringBuilder("jboss.naming:service=SwitchBoardOperator");

      if(applicationName != null)
      {
         builder.append(",application=").append(applicationName);
      }
      builder.append(",module=").append(moduleName);
      if(includeComponent)
      {
         String componentName = componentInformer.getComponentName(deploymentUnit);
         builder.append(",component=").append(componentName);
      }
      return builder.toString();
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
    * Get the component informer
    *
    * @return the component informer
    */
   protected JavaEEComponentInformer getComponentInformer()
   {
      return componentInformer;
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
