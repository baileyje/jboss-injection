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

import org.jboss.metadata.javaee.spec.EJBReferencesMetaData;
import org.jboss.metadata.javaee.spec.ResourceInjectionTargetMetaData;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:jbailey@redhat.com">John Bailey</a>
 */
public class SwitchBoardMetaDataTest
{
   /**
    * Verify SwitchBoardMetaData can be produced from XML. 
    */
   @Test
   public void testParseSwitchBoardXml() throws Exception
   {
      URL xmlUrl = SwitchBoardMetaDataTest.class.getResource("/switchboard.xml");

      SwitchBoardMetaData metaData = parseXml(xmlUrl);

      EJBReferencesMetaData ejbReferencesMetaData = metaData.getEjbReferences();
      Assert.assertNotNull(ejbReferencesMetaData);
      Assert.assertEquals(1, ejbReferencesMetaData.size());
      Assert.assertEquals("a", ejbReferencesMetaData.get("a").getEjbRefName());
      Assert.assertEquals("OtherBean", ejbReferencesMetaData.get("a").getLink());
      Set<ResourceInjectionTargetMetaData> injectionTargets = ejbReferencesMetaData.get("a").getInjectionTargets();
      Assert.assertEquals(1, injectionTargets.size());
      ResourceInjectionTargetMetaData injectionTarget = injectionTargets.iterator().next();
      Assert.assertEquals("B", injectionTarget.getInjectionTargetClass());
      Assert.assertEquals("someBean", injectionTarget.getInjectionTargetName());

      List<SwitchBoardComponentMetaData> compoents = metaData.getComponents();
      Assert.assertNotNull(compoents);
      Assert.assertEquals(1, compoents.size());

      SwitchBoardComponentMetaData componentMetaData = compoents.get(0);

      Assert.assertEquals("MyBean", componentMetaData.getComponentName());

      ejbReferencesMetaData = componentMetaData.getEjbReferences();
      Assert.assertNotNull(ejbReferencesMetaData);
      Assert.assertEquals(1, ejbReferencesMetaData.size());
      Assert.assertEquals("a", ejbReferencesMetaData.get("a").getEjbRefName());
      Assert.assertEquals("OtherBean", ejbReferencesMetaData.get("a").getLink());
      injectionTargets = ejbReferencesMetaData.get("a").getInjectionTargets();
      Assert.assertEquals(1, injectionTargets.size());
      injectionTarget = injectionTargets.iterator().next();
      Assert.assertEquals("A", injectionTarget.getInjectionTargetClass());
      Assert.assertEquals("someBean", injectionTarget.getInjectionTargetName());
   }

   private SwitchBoardMetaData parseXml(final URL xmlUrl)
      throws JBossXBException, IOException
   {
      final Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();

      unmarshaller.setSchemaValidation(false);
      unmarshaller.setValidation(false);
      SwitchBoardMetaData metaData;
      InputStream in = new BufferedInputStream(xmlUrl.openStream());
      try
      {
         DefaultSchemaResolver resolver = new DefaultSchemaResolver();
         resolver.addClassBinding("urn:jboss:switchboard:1.0", SwitchBoardMetaData.class);

         metaData = (SwitchBoardMetaData)unmarshaller.unmarshal(in, resolver);

      }
      finally
      {
         in.close();
      }
      return metaData;
   }

}
