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

import org.jboss.xb.annotations.JBossXmlSchema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Metadata representing a set of references and components used to create a SwitchBoardOperator
 *
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
@JBossXmlSchema(namespace = "urn:jboss:switchboard:1.0", elementFormDefault = XmlNsForm.QUALIFIED)
@XmlRootElement(name="switchboard")
@XmlType(name="switchBoardType", propOrder={"components", "environmentEntries", "ejbReferences", "ejbLocalReferences",
      "serviceReferences", "resourceReferences", "resourceEnvironmentReferences",
      "messageDestinationReferences", "persistenceContextRefs", "persistenceUnitRefs",
      "dataSources"})
public class SwitchBoardMetaData extends AbstractSwitchBoardMetaData
{
   private List<SwitchBoardComponentMetaData> components;

   /**
    * Get the component level metadata
    *
    * @return The components
    */
   public List<SwitchBoardComponentMetaData> getComponents()
   {
      return components;
   }

   /**
    * Add component metadata
    *
    * @param switchBoardComponentMetaData The component metdata
    */
   public void addComponent(SwitchBoardComponentMetaData switchBoardComponentMetaData)
   {
      if(components == null)
         components = new ArrayList<SwitchBoardComponentMetaData>();
      components.add(switchBoardComponentMetaData);
   }

   /**
    * Set the component metadata
    *
    * @param components The component metadata
    */
   @XmlElement(name="component")
   public void setComponents(final List<SwitchBoardComponentMetaData> components)
   {
      this.components = components;
   }
}
