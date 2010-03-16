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
import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
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

import javax.naming.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Deployer capable of creating SwitchBoardOperator beans from Environment MetaData.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class SwitchBoardOperatorDeployer extends AbstractRealDeployer
{
   private EnvironmentProcessor<DeploymentUnit> environmentProcessor;

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

   /** {@inheritDoc} */
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
         deploy(unit, jBossEnterpriseBeanMetaData.getName(), Collections.singletonList((Environment)jBossEnterpriseBeanMetaData));
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
      deploy(unit, metaData.getDescriptionGroup().getDisplayName(), environments);
   }

   /**
    * Deploy a list of Environments as a single SwitchBoardOperator
    *
    * @param unit The deployment unit
    * @param componentName The name of the component the SwitchBoardOperator supports
    * @param environments The list of environments to process
    * @throws DeploymentException if any deployment issues occur
    */
   protected void deploy(final DeploymentUnit unit, String componentName, final List<Environment> environments) throws DeploymentException
   {
      final String name = "jboss:service=SwitchBoardOperator,component=" + componentName + ",deployment=" + unit.getName();

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
         final BeanMetaData beanMetaData = createBeanMetaData(name, allResults);
         unit.getTopLevel().addAttachment(BeanMetaData.class.getName() + "." + name, beanMetaData, BeanMetaData.class);
      }
   }

   /**
    * Create the BeanMetaData for the SwitchBoardOperator
    *
    * @param name The bean name
    * @param resolverResults The list of resolver results
    * @return The BeanMetaData
    */
   protected BeanMetaData createBeanMetaData(final String name, final List<ResolverResult> resolverResults)
   {
      final BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(name, SwitchBoardOperator.class.getName());
      builder.setConstructorValue(createSwitchBoardOperator(resolverResults));

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
    * Create an instance of the SwitchBoardOperator based on a list of resolver results.
    *
    * @param resolverResults The list of resolver results
    * @return A new SwitchBoardOperator
    */
   protected SwitchBoardOperator createSwitchBoardOperator(final List<ResolverResult> resolverResults)
   {
      final List<Injector<Context>> injectors = new ArrayList<Injector<Context>>(resolverResults.size());
      for(ResolverResult resolverResult : resolverResults)
      {
         final ContextInjectionPoint injectionPoint = new ContextInjectionPoint(resolverResult.getRefName());
         final ValueRetriever valueRetriever = resolverResult.getValueRetriever();
         final Injector<Context> injector = InjectorFactory.create(injectionPoint, valueRetriever);
         injectors.add(injector);
      }
      return new SwitchBoardOperator(injectors);
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
}
