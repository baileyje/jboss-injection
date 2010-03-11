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
package org.jboss.injection.inject.test.unit;

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
import org.jboss.test.BaseTestCase;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.junit.BeforeClass;

import java.util.List;

/**
 * AbstractInjectionTestCase -
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class AbstractInjectionTestCase {

   protected static MCServer server;
   protected static MainDeployer mainDeployer;

   @BeforeClass
   public static void setupServer() throws Exception {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      server = MCServerFactory.createServer(classLoader);
      List<BootstrapDescriptor> descriptors = server.getConfiguration().getBootstrapDescriptors();

      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(AbstractInjectionTestCase.class, "/conf/bootstrap/classloader.xml")));
      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(AbstractInjectionTestCase.class, "/conf/bootstrap/deployers.xml")));
      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(AbstractInjectionTestCase.class, "/conf/bootstrap/pojo.xml")));
      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(AbstractInjectionTestCase.class, "/conf/bootstrap/enc-deployer.xml")));
      descriptors.add(new UrlBootstrapDescriptor(BaseTestCase.findResource(AbstractInjectionTestCase.class, "/conf/bootstrap/naming.xml")));

      ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(classLoader);
      try {
         server.start();
         mainDeployer = getBean("MainDeployer", ControllerState.INSTALLED, MainDeployer.class);
      }
      finally {
         Thread.currentThread().setContextClassLoader(oldClassLoader);
      }
   }

   protected static void deploy(Deployment... deployments) throws DeploymentException {
      mainDeployer.deploy(deployments);
   }

   protected static void undeploy(Deployment... deployments) throws DeploymentException {
      mainDeployer.undeploy(deployments);
   }

   protected static <T> Deployment deployment(String name, Class<T> attachmentType, T attachment) {
      try {
         VirtualFile root = VFS.getChild(name);
         VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(root);
         MutableAttachments attachments = (MutableAttachments) deployment.getPredeterminedManagedObjects();
         attachments.addAttachment(attachmentType, attachment);
         return deployment;
      }
      catch(Exception e) {
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
   protected static Object getBean(final Object name, final ControllerState state) throws IllegalStateException {
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
   protected static <T> T getBean(final Object name, final ControllerState state, final Class<T> expected) throws ClassCastException, IllegalStateException {
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
   protected static KernelControllerContext getControllerContext(final Object name, final ControllerState state) throws IllegalStateException {
      KernelController controller = server.getKernel().getController();
      KernelControllerContext context = (KernelControllerContext) controller.getContext(name, state);
      if(context == null)
         throw new IllegalStateException("Bean not found " + name + " at state " + state + " in controller " + controller);
      return context;
   }

   protected void deployBean(BeanMetaData beanMetaData) throws Throwable {
      KernelController controller = server.getKernel().getController();
      controller.install(beanMetaData);
   }

   protected void undeployBean(String beanName) throws Throwable {
      KernelController controller = server.getKernel().getController();
      controller.uninstall(beanName);
   }

}
