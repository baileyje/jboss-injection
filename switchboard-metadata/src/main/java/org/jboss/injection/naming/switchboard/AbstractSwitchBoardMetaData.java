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
package org.jboss.injection.naming.switchboard;

import org.jboss.metadata.ejb.jboss.JBossEnvironmentRefsGroupMetaData;
import org.jboss.metadata.javaee.jboss.JBossServiceReferenceMetaData;
import org.jboss.metadata.javaee.jboss.JBossServiceReferencesMetaData;
import org.jboss.metadata.javaee.spec.AnnotatedEJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.DataSourceMetaData;
import org.jboss.metadata.javaee.spec.DataSourcesMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBLocalReferencesMetaData;
import org.jboss.metadata.javaee.spec.EJBReferenceMetaData;
import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.EnvironmentEntriesMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.javaee.spec.LifecycleCallbacksMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferencesMetaData;
import org.jboss.metadata.javaee.spec.PersistenceContextReferenceMetaData;
import org.jboss.metadata.javaee.spec.PersistenceContextReferencesMetaData;
import org.jboss.metadata.javaee.spec.PersistenceUnitReferenceMetaData;
import org.jboss.metadata.javaee.spec.PersistenceUnitReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferencesMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferenceMetaData;
import org.jboss.metadata.javaee.spec.ServiceReferencesMetaData;
import org.jboss.xb.annotations.JBossXmlCollection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Abstract class extended by SwithBoard metadata types to support environment references.
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public abstract class AbstractSwitchBoardMetaData implements Environment, Serializable
{
   private static final long serialVersionUID = -1;

   private JBossEnvironmentRefsGroupMetaData jndiEnvironmentRefsGroup = new JBossEnvironmentRefsGroupMetaData();

   public EJBLocalReferencesMetaData getEjbLocalReferences()
   {
      return jndiEnvironmentRefsGroup.getEjbLocalReferences();
   }

   @XmlElement(name="ejb-local-ref")
   public void setEjbLocalReferences(EJBLocalReferencesMetaData ejbLocalReferenceMetaData)
   {
      jndiEnvironmentRefsGroup.setEjbLocalReferences(ejbLocalReferenceMetaData);
   }
   
   public EJBLocalReferenceMetaData getEjbLocalReferenceByName(final String name)
   {
      return jndiEnvironmentRefsGroup.getEjbLocalReferenceByName(name);
   }

   public PersistenceContextReferencesMetaData getPersistenceContextRefs()
   {
      return jndiEnvironmentRefsGroup.getPersistenceContextRefs();
   }

   @XmlElement(name="persistence-context-ref")
   public void setPersistenceContextRefs(PersistenceContextReferencesMetaData persistenceContextReferencesMetaData)
   {
      jndiEnvironmentRefsGroup.setPersistenceContextRefs(persistenceContextReferencesMetaData);
   }

   public PersistenceContextReferenceMetaData getPersistenceContextReferenceByName(final String name)
   {
      return jndiEnvironmentRefsGroup.getPersistenceContextReferenceByName(name);
   }

   public DataSourcesMetaData getDataSources()
   {
      return jndiEnvironmentRefsGroup.getDataSources();
   }

   @XmlElement(name="data-source")
   public void setDataSources(DataSourcesMetaData dataSources)
   {
      jndiEnvironmentRefsGroup.setDataSources(dataSources);
   }

   public DataSourceMetaData getDataSourceByName(final String name)
   {
      return jndiEnvironmentRefsGroup.getDataSourceByName(name);
   }

   public EnvironmentEntriesMetaData getEnvironmentEntries()
   {
      return jndiEnvironmentRefsGroup.getEnvironmentEntries();
   }

   @XmlElement(name="env-entry")
   public void setEnvironmentEntries(EnvironmentEntriesMetaData environmentEntries)
   {
      jndiEnvironmentRefsGroup.setEnvironmentEntries(environmentEntries);
   }

   public EnvironmentEntryMetaData getEnvironmentEntryByName(final String name)
   {
      return jndiEnvironmentRefsGroup.getEnvironmentEntryByName(name);
   }

   public EJBReferencesMetaData getEjbReferences()
   {
      return jndiEnvironmentRefsGroup.getEjbReferences();
   }

   @XmlElement(name="ejb-ref")
   public void setEjbReferences(EJBReferencesMetaData ejbReferences)
   {
      jndiEnvironmentRefsGroup.setEjbReferences(ejbReferences);
   }

   public EJBReferenceMetaData getEjbReferenceByName(final String name)
   {
      return jndiEnvironmentRefsGroup.getEjbReferenceByName(name);
   }

   @XmlTransient
   public AnnotatedEJBReferencesMetaData getAnnotatedEjbReferences()
   {
      return jndiEnvironmentRefsGroup.getAnnotatedEjbReferences();
   }

   @XmlTransient
   public void setAnnotatedEjbReferences(AnnotatedEJBReferencesMetaData annotatedEjbReferences)
   {
      jndiEnvironmentRefsGroup.setAnnotatedEjbReferences(annotatedEjbReferences);
   }

   public ServiceReferencesMetaData getServiceReferences()
   {
      return jndiEnvironmentRefsGroup.getServiceReferences();
   }

   @JBossXmlCollection(type= JBossServiceReferencesMetaData.class)
   @XmlElement(name="service-ref", type= JBossServiceReferenceMetaData.class)
   public void setServiceReferences(ServiceReferencesMetaData serviceReferences)
   {
      jndiEnvironmentRefsGroup.setServiceReferences(serviceReferences);
   }

   public ServiceReferenceMetaData getServiceReferenceByName(final String name)
   {
      return jndiEnvironmentRefsGroup.getServiceReferenceByName(name);
   }

   public ResourceReferencesMetaData getResourceReferences()
   {
      return jndiEnvironmentRefsGroup.getResourceReferences();
   }

   @XmlElement(name="resource-ref")
   public void setResourceReferences(ResourceReferencesMetaData resourceReferences)
   {
      jndiEnvironmentRefsGroup.setResourceReferences(resourceReferences);
   }

   public ResourceReferenceMetaData getResourceReferenceByName(final String name)
   {
      return jndiEnvironmentRefsGroup.getResourceReferenceByName(name);
   }

   public ResourceEnvironmentReferencesMetaData getResourceEnvironmentReferences()
   {
      return jndiEnvironmentRefsGroup.getResourceEnvironmentReferences();
   }

   @XmlElement(name="resource-env-ref")
   public void setResourceEnvironmentReferences(ResourceEnvironmentReferencesMetaData resourceEnvironmentReferences)
   {
      jndiEnvironmentRefsGroup.setResourceEnvironmentReferences(resourceEnvironmentReferences);
   }

   public ResourceEnvironmentReferenceMetaData getResourceEnvironmentReferenceByName(final String name)
   {
      return jndiEnvironmentRefsGroup.getResourceEnvironmentReferenceByName(name);
   }

   public MessageDestinationReferencesMetaData getMessageDestinationReferences()
   {
      return jndiEnvironmentRefsGroup.getMessageDestinationReferences();
   }

   @XmlElement(name="message-destination-ref")
   public void setMessageDestinationReferences(MessageDestinationReferencesMetaData messageDestinationReferences)
   {
      jndiEnvironmentRefsGroup.setMessageDestinationReferences(messageDestinationReferences);
   }

   public MessageDestinationReferenceMetaData getMessageDestinationReferenceByName(final String name)
   {
      return jndiEnvironmentRefsGroup.getMessageDestinationReferenceByName(name);
   }

   public PersistenceUnitReferencesMetaData getPersistenceUnitRefs()
   {
      return jndiEnvironmentRefsGroup.getPersistenceUnitRefs();
   }

   @XmlElement(name="persistence-unit-ref")
   public void setPersistencenitRefs(PersistenceUnitReferencesMetaData persistencenitRefs)
   {
      jndiEnvironmentRefsGroup.setPersistenceUnitRefs(persistencenitRefs);
   }

   public PersistenceUnitReferenceMetaData getPersistenceUnitReferenceByName(final String name)
   {
      return jndiEnvironmentRefsGroup.getPersistenceUnitReferenceByName(name);
   }

   public LifecycleCallbacksMetaData getPostConstructs()
   {
      throw new UnsupportedOperationException("Unsupported");
   }

   public LifecycleCallbacksMetaData getPreDestroys()
   {
      throw new UnsupportedOperationException("Unsupported");
   }
}