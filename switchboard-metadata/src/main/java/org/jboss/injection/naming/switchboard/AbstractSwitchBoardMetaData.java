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

import org.jboss.metadata.javaee.jboss.JBossServiceReferenceMetaData;
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

   private EnvironmentEntriesMetaData environmentEntries;
   private AnnotatedEJBReferencesMetaData annotatedEjbReferences;
   private EJBReferencesMetaData ejbReferences;
   private ResourceReferencesMetaData resourceReferences;
   private ResourceEnvironmentReferencesMetaData resourceEnvironmentReferences;
   private MessageDestinationReferencesMetaData messageDestinationReferences;
   private PersistenceUnitReferencesMetaData persistenceUnitRefs;
   private EJBLocalReferencesMetaData ejbLocalReferences;
   private ServiceReferencesMetaData serviceReferences;
   private PersistenceContextReferencesMetaData persistenceContextRefs;
   private DataSourcesMetaData dataSources;


   public AbstractSwitchBoardMetaData()
   {
   }

   public AbstractSwitchBoardMetaData(Environment existingEnvironment)
   {
      if(existingEnvironment.getAnnotatedEjbReferences() != null)
         setAnnotatedEjbReferences(existingEnvironment.getAnnotatedEjbReferences());
      if(existingEnvironment.getDataSources() != null)
         setDataSources(existingEnvironment.getDataSources());
      if(existingEnvironment.getEjbLocalReferences() != null)
         setEjbLocalReferences(existingEnvironment.getEjbLocalReferences());
      if(existingEnvironment.getEjbReferences() != null)
         setEjbReferences(existingEnvironment.getEjbReferences());
      if(existingEnvironment.getEnvironmentEntries() != null)
         setEnvironmentEntries(existingEnvironment.getEnvironmentEntries());
      if(existingEnvironment.getMessageDestinationReferences() != null)
         setMessageDestinationReferences(existingEnvironment.getMessageDestinationReferences());
      if(existingEnvironment.getPersistenceContextRefs() != null)
         setPersistenceContextRefs(existingEnvironment.getPersistenceContextRefs());
      if(existingEnvironment.getPersistenceUnitRefs() != null)
         setPersistenceUnitRefs(existingEnvironment.getPersistenceUnitRefs());
      if(existingEnvironment.getResourceEnvironmentReferences() != null)
         setResourceEnvironmentReferences(existingEnvironment.getResourceEnvironmentReferences());
      if(existingEnvironment.getResourceReferences() != null)
         setResourceReferences(existingEnvironment.getResourceReferences());
      if(existingEnvironment.getServiceReferences() != null)
         setServiceReferences(existingEnvironment.getServiceReferences());
   }

   public EJBLocalReferencesMetaData getEjbLocalReferences()
   {
      if(ejbLocalReferences == null)
         ejbLocalReferences = new EJBLocalReferencesMetaData();
      return ejbLocalReferences;
   }

   @XmlElement(name="ejb-local-ref")
   public void setEjbLocalReferences(EJBLocalReferencesMetaData ejbLocalReferences)
   {
      this.ejbLocalReferences = ejbLocalReferences;
   }
   
   public EJBLocalReferenceMetaData getEjbLocalReferenceByName(final String name)
   {
      return getEjbLocalReferences().get(name);
   }

   public PersistenceContextReferencesMetaData getPersistenceContextRefs()
   {
      if(persistenceContextRefs == null)
         persistenceContextRefs = new PersistenceContextReferencesMetaData();
      return persistenceContextRefs;
   }

   @XmlElement(name="persistence-context-ref")
   public void setPersistenceContextRefs(PersistenceContextReferencesMetaData persistenceContextRefs)
   {
      this.persistenceContextRefs = persistenceContextRefs;
   }

   public PersistenceContextReferenceMetaData getPersistenceContextReferenceByName(final String name)
   {
      return getPersistenceContextRefs().get(name);
   }

   public DataSourcesMetaData getDataSources()
   {
      if(dataSources == null)
         dataSources = new DataSourcesMetaData();
      return dataSources;
   }

   @XmlElement(name="data-source")
   public void setDataSources(DataSourcesMetaData dataSources)
   {
      this.dataSources = dataSources;
   }

   public DataSourceMetaData getDataSourceByName(final String name)
   {
      return getDataSources().get(name);
   }

   public EnvironmentEntriesMetaData getEnvironmentEntries()
   {
      if(environmentEntries == null)
         environmentEntries = new EnvironmentEntriesMetaData();
      return environmentEntries;
   }

   @XmlElement(name="env-entry")
   public void setEnvironmentEntries(EnvironmentEntriesMetaData environmentEntries)
   {
      this.environmentEntries = environmentEntries;
   }

   public EnvironmentEntryMetaData getEnvironmentEntryByName(final String name)
   {
      return getEnvironmentEntries().get(name);
   }

   public EJBReferencesMetaData getEjbReferences()
   {
      if(ejbReferences == null)
         ejbReferences = new EJBReferencesMetaData();
      return ejbReferences;
   }

   @XmlElement(name="ejb-ref")
   public void setEjbReferences(EJBReferencesMetaData ejbReferences)
   {
      this.ejbReferences = ejbReferences;
   }

   public EJBReferenceMetaData getEjbReferenceByName(final String name)
   {
      return getEjbReferences().get(name);
   }

   @XmlTransient
   public AnnotatedEJBReferencesMetaData getAnnotatedEjbReferences()
   {
      if(annotatedEjbReferences == null)
         annotatedEjbReferences = new AnnotatedEJBReferencesMetaData();
      return annotatedEjbReferences;
   }

   @XmlTransient
   public void setAnnotatedEjbReferences(AnnotatedEJBReferencesMetaData annotatedEjbReferences)
   {
      this.annotatedEjbReferences = annotatedEjbReferences;
   }

   public ServiceReferencesMetaData getServiceReferences()
   {
      if(serviceReferences == null)
         serviceReferences = new ServiceReferencesMetaData();
      return serviceReferences;
   }

   @XmlElement(name="service-ref", type= JBossServiceReferenceMetaData.class)
   public void setServiceReferences(ServiceReferencesMetaData serviceReferences)
   {
      this.serviceReferences = serviceReferences;
   }

   public ServiceReferenceMetaData getServiceReferenceByName(final String name)
   {
      return getServiceReferences().get(name);
   }

   public ResourceReferencesMetaData getResourceReferences()
   {
      if(resourceReferences == null)
         resourceReferences = new ResourceReferencesMetaData();
      return resourceReferences;
   }

   @XmlElement(name="resource-ref")
   public void setResourceReferences(ResourceReferencesMetaData resourceReferences)
   {
      this.resourceReferences = resourceReferences;
   }

   public ResourceReferenceMetaData getResourceReferenceByName(final String name)
   {
      return getResourceReferences().get(name);
   }

   public ResourceEnvironmentReferencesMetaData getResourceEnvironmentReferences()
   {
      if(resourceEnvironmentReferences == null)
         resourceEnvironmentReferences = new ResourceEnvironmentReferencesMetaData();
      return resourceEnvironmentReferences;
   }

   @XmlElement(name="resource-env-ref")
   public void setResourceEnvironmentReferences(ResourceEnvironmentReferencesMetaData resourceEnvironmentReferences)
   {
      this.resourceEnvironmentReferences = resourceEnvironmentReferences;
   }

   public ResourceEnvironmentReferenceMetaData getResourceEnvironmentReferenceByName(final String name)
   {
      return getResourceEnvironmentReferences().get(name);
   }

   public MessageDestinationReferencesMetaData getMessageDestinationReferences()
   {
      if(messageDestinationReferences == null)
         messageDestinationReferences = new MessageDestinationReferencesMetaData();
      return messageDestinationReferences;
   }

   @XmlElement(name="message-destination-ref")
   public void setMessageDestinationReferences(MessageDestinationReferencesMetaData messageDestinationReferences)
   {
      this.messageDestinationReferences = messageDestinationReferences;
   }

   public MessageDestinationReferenceMetaData getMessageDestinationReferenceByName(final String name)
   {
      return getMessageDestinationReferences().get(name);
   }

   public PersistenceUnitReferencesMetaData getPersistenceUnitRefs()
   {
      if(persistenceUnitRefs == null)
         persistenceUnitRefs = new PersistenceUnitReferencesMetaData();
      return persistenceUnitRefs;
   }

   @XmlElement(name="persistence-unit-ref")
   public void setPersistenceUnitRefs(PersistenceUnitReferencesMetaData persistenceUnitRefs)
   {
      this.persistenceUnitRefs = persistenceUnitRefs;
   }

   public PersistenceUnitReferenceMetaData getPersistenceUnitReferenceByName(final String name)
   {
      return getPersistenceUnitRefs().get(name);
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