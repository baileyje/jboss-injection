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
import org.jboss.injection.resolve.naming.EnvironmentProcessor;
import org.jboss.injection.resolve.naming.ResolutionException;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.logging.Logger;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.spec.InterceptorBindingMetaData;
import org.jboss.metadata.ejb.spec.InterceptorBindingsMetaData;
import org.jboss.metadata.ejb.spec.InterceptorClassesMetaData;
import org.jboss.metadata.ejb.spec.InterceptorMetaData;
import org.jboss.metadata.ejb.spec.InterceptorsMetaData;
import org.jboss.metadata.javaee.spec.Environment;

import javax.naming.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deployer capable of creating SwitchBoardOperator beans from Environment MetaData.
 *
 * @param <M> The metadata attachment type to deploy a SwitchBoardOperator for.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class AbstractSwitchBoardOperatorDeployer<M> extends AbstractSimpleRealDeployer<M>
{
   private static final Logger log = Logger.getLogger(AbstractSwitchBoardOperatorDeployer.class);
   
   private EnvironmentProcessor<DeploymentUnit> environmentProcessor;

   /**
    * Create the deployer and setup the inputs
    *
    * @param metaDataType The metadata attachement type
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

      List<ResolverResult<?>> results;
      try
      {
         results = environmentProcessor.process(unit, environments);
      }
      catch(ResolutionException e)
      {
         throw DeploymentException.rethrowAsDeploymentException("Failed to resolve Environment references", e);
      }

      if(results != null && !results.isEmpty())
      {
         final BeanMetaData beanMetaData = createBeanMetaData(unit, results);
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
   protected BeanMetaData createBeanMetaData(final DeploymentUnit unit, final List<ResolverResult<?>> resolverResults)
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
    * @param <V> The value type for the resolver result
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
    * Collect the interceptors for an enterprise bean.
    *
    * @param enterpriseBean The enterprise bean
    * @return The interceptors used by this enterprise bean
    */
   protected Collection<InterceptorMetaData> collectInterceptors(final JBossEnterpriseBeanMetaData enterpriseBean)
   {
      final JBossMetaData jBossMetaData = enterpriseBean.getJBossMetaData();

      // Lets get out of here early if there is no interceptor metadata
      if(jBossMetaData.getInterceptors() == null)
         return Collections.emptySet();

      final Set<InterceptorMetaData> interceptors = new HashSet<InterceptorMetaData>();

      final InterceptorBindingsMetaData interceptorBindings = enterpriseBean.getJBossMetaData().getAssemblyDescriptor().getInterceptorBindings();
      for(InterceptorBindingMetaData interceptorBinding : interceptorBindings)
      {
         if(interceptorBinding.getEjbName().equals(enterpriseBean.getName()))
         {
            final InterceptorClassesMetaData interceptorClasses = interceptorBinding.getInterceptorClasses();
            collectInterceptors(jBossMetaData, interceptorClasses, interceptors);
         }
      }
      return interceptors;
   }

   /**
    * Collect the interceptors based on InterceptorClassesMetaData 
    *
    * @param jbossMetaData The JbossMetaData
    * @param interceptorClasses The interceptor classes to find
    * @param interceptors The collected interceptors
    */
   protected void collectInterceptors(final JBossMetaData jbossMetaData, final InterceptorClassesMetaData interceptorClasses, final Collection<InterceptorMetaData> interceptors)
   {
      final InterceptorsMetaData allInterceptors = jbossMetaData.getInterceptors();
      for(InterceptorMetaData interceptor : allInterceptors)
      {
         if(interceptorClasses.contains(interceptor.getInterceptorClass()))
         {
            if(interceptor != null)
               interceptors.add(interceptor);
         }
      }
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
