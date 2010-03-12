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
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.injection.inject.InjectorFactory;
import org.jboss.injection.inject.naming.ContextInjectionPoint;
import org.jboss.injection.inject.naming.LinkRefValueRetriever;
import org.jboss.injection.inject.spi.Injector;
import org.jboss.injection.inject.naming.SwitchBoardOperator;
import org.jboss.injection.resolve.enc.EnvironmentProcessor;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.metadata.javaee.spec.Environment;

import javax.naming.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * Deployer capable of creating SwitchBoardOperator beans from Environment MetaData.
 *
 * @author <a href=mailto:"jbailey@redhat.com">John Bailey</a>
 */
public abstract class SwitchBoardOperatorDeployer<M> extends AbstractSimpleRealDeployer<M>
{

   private EnvironmentProcessor<DeploymentUnit> environmentProcessor;

   public SwitchBoardOperatorDeployer(Class<M> metadataClass)
   {
      super(metadataClass);
      setOutput(BeanMetaData.class);
   }

   @Override
   public void deploy(final DeploymentUnit unit, final M deployment) throws DeploymentException
   {
      final Environment environment = getEnvironment(deployment);
      deploy(unit, deployment, environment);
   }

   protected abstract Environment getEnvironment(M deployment);

   protected void deploy(final DeploymentUnit unit, final M metaData, final Environment environment) throws DeploymentException
   {
      final String name = "jboss:service=SwitchBoardOperator,name=" + unit.getName();

      final EnvironmentProcessor<DeploymentUnit> environmentProcessor = getEnvironmentProcessor();
      if(environmentProcessor == null)
         throw new IllegalStateException("SwitchBoardOperator deployers require an EnvironmentPorcessor, which has not been set.");

      final List<ResolverResult> resolverResults = environmentProcessor.process(unit, environment);

      if(resolverResults != null && !resolverResults.isEmpty())
      {
         final BeanMetaData beanMetaData = createBeanMetaData(name, resolverResults);
         unit.getTopLevel().addAttachment(BeanMetaData.class.getName() + "." + name, beanMetaData, BeanMetaData.class);
      }
   }

   protected BeanMetaData createBeanMetaData(final String name, final List<ResolverResult> resolverResults)
   {
      final BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(name, SwitchBoardOperator.class.getName());
      builder.setConstructorValue(createSwitchBoardOperator(resolverResults));

      for(ResolverResult resolverResult : resolverResults)
      {
         builder.addDependency(resolverResult.getBeanName());
      }
      return builder.getBeanMetaData();
   }

   protected SwitchBoardOperator createSwitchBoardOperator(final List<ResolverResult> resolverResults)
   {
      final List<Injector<Context>> injectors = new ArrayList<Injector<Context>>(resolverResults.size());
      for(ResolverResult resolverResult : resolverResults)
      {
         final Injector<Context> injector = InjectorFactory.create(new ContextInjectionPoint(resolverResult.getRefName()), new LinkRefValueRetriever(resolverResult.getJndiName()));
         injectors.add(injector);
      }
      return new SwitchBoardOperator(injectors);
   }

   protected EnvironmentProcessor<DeploymentUnit> getEnvironmentProcessor()
   {
      return environmentProcessor;
   }

   @Inject
   public void setEnvironmentProcessor(final EnvironmentProcessor<DeploymentUnit> environmentProcessor)
   {
      this.environmentProcessor = environmentProcessor;
   }
}
