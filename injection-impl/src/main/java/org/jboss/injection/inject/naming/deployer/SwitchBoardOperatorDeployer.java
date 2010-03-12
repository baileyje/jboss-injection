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
package org.jboss.injection.inject.naming.deployer;

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
 * Deployer capable of create an EncPopulator bean from EnvironmentMetaData.
 * TODO: Determine if we should be looking for Environment.  (Maybe SessionMetaData. etc) 
 *
 * @author <a href=mailto:"jbailey@redhat.com">John Bailey</a>
 */
public class SwitchBoardOperatorDeployer extends AbstractSimpleRealDeployer<Environment> {

   private EnvironmentProcessor environmentProcessor;

   public SwitchBoardOperatorDeployer() {
      super(Environment.class);
      //setComponentsOnly(true);
      setOutput(BeanMetaData.class);
   }

   @Override
   public void deploy(final DeploymentUnit unit, final Environment environment) throws DeploymentException {
      final String name = "jboss:service=EncPopulator,name=" + unit.getName();

      final List<ResolverResult> resolverResults = processEnvironment(environment);

      if(resolverResults != null && !resolverResults.isEmpty()) {
         final BeanMetaData beanMetaData = createBeanMetaData(name, resolverResults);
         unit.getTopLevel().addAttachment(BeanMetaData.class.getName() + "." + name, beanMetaData, BeanMetaData.class);
      }
   }

   private BeanMetaData createBeanMetaData(final String name, final List<ResolverResult> resolverResults) {
      final BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(name, SwitchBoardOperator.class.getName());
      builder.setConstructorValue(createSwitchBoardOperator(resolverResults));

      for(ResolverResult resolverResult : resolverResults) {
         builder.addDependency(resolverResult.getBeanName());
      }
      return builder.getBeanMetaData();
   }

   private List<ResolverResult> processEnvironment(Environment environment) {
      final EnvironmentProcessor environmentProcessor = getEnvironmentProcessor();
      return environmentProcessor.process(environment);
   }

   private SwitchBoardOperator createSwitchBoardOperator(final List<ResolverResult> resolverResults) {
      final List<Injector<Context>> injectors = new ArrayList<Injector<Context>>(resolverResults.size());
      for(ResolverResult resolverResult : resolverResults) {
         final Injector<Context> injector = InjectorFactory.create(new ContextInjectionPoint(resolverResult.getRefName()), new LinkRefValueRetriever(resolverResult.getJndiName()));
         injectors.add(injector);
      }
      return new SwitchBoardOperator(injectors);
   }

   public EnvironmentProcessor getEnvironmentProcessor() {
      return environmentProcessor;
   }

   @Inject
   public void setEnvironmentProcessor(final EnvironmentProcessor environmentProcessor) {
      this.environmentProcessor = environmentProcessor;
   }
}
