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
package org.jboss.injection.naming.test.deployer.unit;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ConstructorMetaData;
import org.jboss.beans.metadata.spi.DependencyMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.bootstrap.api.descriptor.BootstrapDescriptor;
import org.jboss.bootstrap.api.descriptor.UrlBootstrapDescriptor;
import org.jboss.bootstrap.api.mc.server.MCServer;
import org.jboss.bootstrap.api.mc.server.MCServerFactory;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.structure.spi.DeploymentContext;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentContext;
import org.jboss.deployers.structure.spi.helpers.AbstractDeploymentUnit;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.injection.inject.naming.ContextInjectionPoint;
import org.jboss.injection.inject.naming.LinkRefValueRetriever;
import org.jboss.injection.inject.naming.SwitchBoardOperator;
import org.jboss.injection.inject.spi.Injector;
import org.jboss.injection.naming.deployer.SwitchBoardOperatorDeployer;
import org.jboss.injection.resolve.enc.EnvironmentProcessor;
import org.jboss.injection.resolve.spi.Resolver;
import org.jboss.injection.resolve.spi.ResolverResult;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.test.BaseTestCase;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class AbstractSwitchBoardOperatorDeployerTestCase<M extends Environment>
{

   protected static MCServer server;
   private static MainDeployer mainDeployer;

   @BeforeClass
   public static void setupServer() throws Exception
   {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      server = MCServerFactory.createServer(classLoader);
      List<BootstrapDescriptor> descriptors = server.getConfiguration().getBootstrapDescriptors();

      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(EJBSwitchBoardOperatorDeployerTest.class, "/conf/bootstrap/naming.xml")));
      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(EJBSwitchBoardOperatorDeployerTest.class, "/conf/bootstrap/classloader.xml")));
      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(EJBSwitchBoardOperatorDeployerTest.class, "/conf/bootstrap/deployers.xml")));
      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(EJBSwitchBoardOperatorDeployerTest.class, "/conf/bootstrap/pojo.xml")));
      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(EJBSwitchBoardOperatorDeployerTest.class, "/conf/bootstrap/switchboard-operator-deployer.xml")));

      ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      try
      {
         server.start();
         mainDeployer = getBean("MainDeployer", ControllerState.INSTALLED, MainDeployer.class);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldClassLoader);
      }
   }

   private Context context;

   @Before
   public void initializeContext() throws Exception
   {
      context = new InitialContext();
   }

   @Before
   public void deployMockResolver() throws Throwable
   {
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder("MockResolver", Resolver.class.getName());
      Resolver<EJBReferenceMetaData, DeploymentUnit> resolver = createMockResolver(EJBReferenceMetaData.class, new ResolverResult("java:test", "java:otherTest", "mc-bean-test"));
      builder.setConstructorValue(resolver);
      deployBean(builder.getBeanMetaData());
   }

   @After
   public void undeployMockResolver() throws Throwable
   {
      undeployBean("MockResolver");
   }

   protected abstract Class<M> getMetaDataType();

   protected abstract SwitchBoardOperatorDeployer<M> getDeployer();

   @Test
   public void testDeployNoMc() throws Exception
   {
      SwitchBoardOperatorDeployer<M> deployer = getDeployer();

      deployer.setEnvironmentProcessor(getEnvironmentProcessor());

      DeploymentUnit deploymentUnit = createMockDeploymentUnit();

      deployer.deploy(deploymentUnit, createMockEnvironment());

      BeanMetaData beanMetaData = deploymentUnit.getAttachment(BeanMetaData.class.getName() + "." + "jboss:service=SwitchBoardOperator,name=" + deploymentUnit.getName(), BeanMetaData.class);
      Assert.assertNotNull(beanMetaData);

      ConstructorMetaData constructorMetaData = beanMetaData.getConstructor();

      ValueMetaData constructorValueMetaData = constructorMetaData.getValue();
      Object constructorValue = constructorValueMetaData.getUnderlyingValue();

      Assert.assertTrue(constructorValue instanceof SwitchBoardOperator);

      SwitchBoardOperator switchBoardOperator = SwitchBoardOperator.class.cast(constructorValue);

      List<Injector<?>> injectors = getPrivateField(switchBoardOperator, "injectors", List.class);
      Assert.assertNotNull(injectors);
      Assert.assertEquals(1, injectors.size());
      Injector<?> injector = injectors.get(0);
      ContextInjectionPoint<LinkRefValueRetriever> injectionPoint = (ContextInjectionPoint<LinkRefValueRetriever>) getPrivateField(injector, "injectionPoint", ContextInjectionPoint.class);
      String encJndiName = getPrivateField(injectionPoint, "jndiName", String.class);
      Assert.assertEquals("java:otherTest", encJndiName);
      LinkRefValueRetriever valueRetriever = getPrivateField(injector, "valueRetriever", LinkRefValueRetriever.class);
      String globalJndiName = getPrivateField(valueRetriever, "jndiName", String.class);
      Assert.assertEquals("java:test", globalJndiName);

      Set<DependencyMetaData> dependencyMetaDatas = beanMetaData.getDepends();
      Assert.assertEquals(1, dependencyMetaDatas.size());
      DependencyMetaData dependencyMetaData = dependencyMetaDatas.iterator().next();

      Assert.assertEquals("mc-bean-test", dependencyMetaData.getDependency());
   }

   @Test
   public void testDeployWithMcNoDependency() throws Throwable
   {
      context.rebind("java:test", "Test Value");

      Deployment deployment = deployment("test1", getMetaDataType(), createMockEnvironment());
      try
      {
         deploy(deployment);
         Assert.fail("Should have thrown an exception because of a missing dependency");
      } catch(DeploymentException expected)
      {
         // TODO: Make sure this is due to the missing dep
      }
   }

   @Test
   public void testDeployWithMcDependencyAlreadyMet() throws Throwable
   {
      context.rebind("java:test", "Test Value");
      Deployment deployment = deployment("test1", getMetaDataType(), createMockEnvironment());
      deployBean(BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNameNotFound("java:otherTest");
      deploy(deployment);
      assertContextValue("java:otherTest", "Test Value");
      undeployBean("mc-bean-test");
      context.unbind("java:otherTest");
      undeploy(deployment);
   }

   @Test
   public void testDeployWithMcDependencyInBatchOrdered() throws Throwable
   {
      context.rebind("java:test", "Test Value");
      Deployment envDeployment = deployment("test1", getMetaDataType(), createMockEnvironment());
      Deployment dependencyDeployment = deployment("dependency", BeanMetaData.class, BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNameNotFound("java:otherTest");
      deploy(envDeployment, dependencyDeployment);
      assertContextValue("java:otherTest", "Test Value");
      context.unbind("java:otherTest");
      undeploy(dependencyDeployment, envDeployment);
   }

   @Test
   public void testDeployWithMcDependencyInBatchUnOrdered() throws Throwable
   {
      context.rebind("java:test", "Test Value");
      Deployment envDeployment = deployment("test1", getMetaDataType(), createMockEnvironment());
      Deployment dependencyDeployment = deployment("dependency", BeanMetaData.class, BeanMetaDataBuilder.createBuilder("mc-bean-test", String.class.getName()).setConstructorValue("test").getBeanMetaData());
      assertNameNotFound("java:otherTest");
      deploy(envDeployment, dependencyDeployment);
      assertContextValue("java:otherTest", "Test Value");
      context.unbind("java:otherTest");
      undeploy(envDeployment, dependencyDeployment);
   }

   private EnvironmentProcessor<DeploymentUnit> getEnvironmentProcessor()
   {
      EnvironmentProcessor<DeploymentUnit> environmentProcessor = new EnvironmentProcessor<DeploymentUnit>();
      environmentProcessor.addResolver(createMockResolver(EJBReferenceMetaData.class, new ResolverResult("java:test", "java:otherTest", "mc-bean-test")));
      return environmentProcessor;
   }

   private <M> Resolver<M, DeploymentUnit> createMockResolver(final Class<M> type, final ResolverResult defaultResult)
   {
      return new Resolver<M, DeploymentUnit>()
      {
         public Class<M> getMetaDataType()
         {
            return type;
         }

         public ResolverResult resolve(final DeploymentUnit deploymentUnit, final M metaData)
         {
            return defaultResult;
         }
      };
   }

   private DeploymentUnit createMockDeploymentUnit()
   {
      DeploymentContext deploymentContext = new AbstractDeploymentContext("deploymentUnit", "");
      DeploymentUnit deploymentUnit = new AbstractDeploymentUnit(deploymentContext);
      return deploymentUnit;
   }

   private M createMockEnvironment()
   {
      M environment = mock(getMetaDataType());

      EJBReferencesMetaData referencesMetaData = new EJBReferencesMetaData();
      EJBReferenceMetaData referenceMetaData = new EJBReferenceMetaData();
      referenceMetaData.setEjbRefName("testRef");
      referencesMetaData.add(referenceMetaData);
      when(environment.getEjbReferences()).thenReturn(referencesMetaData);
      return environment;
   }

   private <T> T getPrivateField(Object object, String fieldName, Class<T> type) throws Exception
   {
      Field field = object.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return (T) field.get(object);
   }

   protected static void deploy(Deployment... deployments) throws DeploymentException
   {
      mainDeployer.deploy(deployments);
   }

   protected static void undeploy(Deployment... deployments) throws DeploymentException
   {
      mainDeployer.undeploy(deployments);
   }

   protected static <T> Deployment deployment(String name, Class<T> attachmentType, T attachment)
   {
      try
      {
         VirtualFile root = VFS.getChild(name);
         VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
         MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
         attachments.addAttachment(attachmentType, attachment);
         return deployment;
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Get a bean
    *
    * @param name  the name of the bean
    * @param state the state of the bean
    * @return the bean
    * @throws IllegalStateException when the bean does not exist at that state
    */
   protected static Object getBean(final Object name, final ControllerState state) throws IllegalStateException
   {
      KernelControllerContext context = getControllerContext(name, state);
      return context.getTarget();
   }

   /**
    * Get a bean
    *
    * @param <T>      the expected type
    * @param name     the name of the bean
    * @param state    the state of the bean
    * @param expected the expected type
    * @return the bean
    * @throws ClassCastException    when the bean can not be cast to the expected type
    * @throws IllegalStateException when the bean does not exist at that state
    */
   protected static <T> T getBean(final Object name, final ControllerState state, final Class<T> expected) throws ClassCastException, IllegalStateException
   {
      if(expected == null)
         throw new IllegalArgumentException("Null expected");
      Object bean = getBean(name, state);
      return expected.cast(bean);
   }

   /**
    * Get a context
    *
    * @param name  the name of the bean
    * @param state the state of the bean
    * @return the context
    * @throws IllegalStateException when the context does not exist at that state
    */
   protected static KernelControllerContext getControllerContext(final Object name, final ControllerState state) throws IllegalStateException
   {
      KernelController controller = server.getKernel().getController();
      KernelControllerContext context = (KernelControllerContext) controller.getContext(name, state);
      if(context == null)
         throw new IllegalStateException("Bean not found " + name + " at state " + state + " in controller " + controller);
      return context;
   }

   protected void deployBean(BeanMetaData beanMetaData) throws Throwable
   {
      KernelController controller = server.getKernel().getController();
      controller.install(beanMetaData);
   }

   protected void undeployBean(String beanName) throws Throwable
   {
      KernelController controller = server.getKernel().getController();
      controller.uninstall(beanName);
   }

   protected void assertContextValue(String jndiName, Object value) throws Exception
   {
      String actual = (String) context.lookup(jndiName);
      Assert.assertEquals(value, actual);
   }

   protected void assertNameNotFound(String name)
   {
      try
      {
         context.lookup(name);
         Assert.fail("The name should not be found in the context: " + name);
      } catch(NamingException expected)
      {
      }
   }
}
