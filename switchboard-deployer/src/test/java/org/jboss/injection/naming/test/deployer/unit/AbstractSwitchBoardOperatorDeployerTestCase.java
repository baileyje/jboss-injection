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
import org.jboss.bootstrap.api.descriptor.BootstrapDescriptor;
import org.jboss.bootstrap.api.descriptor.UrlBootstrapDescriptor;
import org.jboss.bootstrap.api.mc.server.MCServer;
import org.jboss.bootstrap.api.mc.server.MCServerFactory;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployers.client.spi.Deployment;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.attachments.MutableAttachments;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.naming.Util;
import org.jboss.test.BaseTestCase;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URL;
import java.util.List;

import static org.mockito.Mockito.mock;


/**
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class AbstractSwitchBoardOperatorDeployerTestCase
{

   protected static MCServer server;
   protected static MainDeployer mainDeployer;
   protected static Context context;
   protected static Context compContext;
   protected static Context moduleContext;

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
      context = new InitialContext();
      Context javaContext = (Context) context.lookup("java:");
      compContext = javaContext.createSubcontext("comp");
      moduleContext = javaContext.createSubcontext("module");
   }

   @AfterClass
   public static void tearDownServer() throws Exception
   {
      server.shutdown();
   }

   protected static void deploy(Deployment... deployments) throws DeploymentException
   {
      mainDeployer.deploy(deployments);
   }

   protected static void undeploy(Deployment... deployments) throws DeploymentException
   {
      mainDeployer.undeploy(deployments);
   }

   protected static Deployment createDeployment(URL url)
   {
      try
      {
         VirtualFile root = VFS.getChild(url);
         return VFSDeploymentFactory.getInstance().createVFSDeployment(root);
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }


   protected static Deployment createDeployment(String name)
   {
      try
      {
         VirtualFile root = VFS.getChild(name);
         return VFSDeploymentFactory.getInstance().createVFSDeployment(root);
      }
      catch(Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   protected Deployment createDeployment(final String name, final BeanMetaData beanMetaData)
   {
      final Deployment deployment = createDeployment(name);
      MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
      attachments.addAttachment(BeanMetaData.class, beanMetaData);
      return deployment;
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
      Object actual = context.lookup(jndiName);
      Assert.assertEquals(value, actual);
   }

   protected void assertNameNotFound(String name)
   {
      try
      {
         context.lookup(name);
         Assert.fail("The name should not be found in the context: " + name);
      }
      catch(NamingException expected)
      {
      }
   }

   protected void unbind(Context context, String... names) throws NamingException
   {
      for(String name : names)
         context.unbind(name);
   }
}
