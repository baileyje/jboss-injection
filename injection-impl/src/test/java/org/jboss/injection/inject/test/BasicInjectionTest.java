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
package org.jboss.injection.inject.test;

import org.jboss.injection.inject.InjectionFactory;
import org.jboss.injection.inject.support.SimpleObject;
import org.jboss.injection.inject.support.SimpleValueRetriever;
import org.jboss.injection.inject.pojo.FieldInjectionPoint;
import org.jboss.injection.inject.pojo.MethodInjectionPoint;
import org.jboss.injection.inject.pojo.PojoInjector;
import org.jboss.injection.inject.spi.InjectionPoint;
import org.junit.Test;
import org.junit.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Basic test to verify injection facilities using pojo targets and values.
 *
 * @author <a href=mailto:jbailey@redhat.com">John Bailey</a>
 */
public class BasicInjectionTest {

    private PojoInjector injector = new PojoInjector();
    private SimpleValueRetriever valueRetriever = new SimpleValueRetriever("Test Value");
    private SimpleObject simpleObject = new SimpleObject();

    @Test
    public void testFieldInjection() throws Exception {
        InjectionPoint injectionPoint = new FieldInjectionPoint(SimpleObject.class.getDeclaredField("simpleProperty"));

        injector.inject(simpleObject, injectionPoint, valueRetriever);

        Assert.assertNotNull(simpleObject.getSimpleProperty());
        Assert.assertEquals("Test Value", simpleObject.getSimpleProperty());
    }

    @Test
    public void testMethodInjection() throws Exception {
        InjectionPoint injectionPoint = new MethodInjectionPoint(SimpleObject.class.getDeclaredMethod("setSimpleProperty", String.class));

        injector.inject(simpleObject, injectionPoint, valueRetriever);

        Assert.assertNotNull(simpleObject.getSimpleProperty());
        Assert.assertEquals("Test Value", simpleObject.getSimpleProperty());
    }

    @Test
    public void testMethodInjectionInvalidMethod() throws Exception {
        Method method = SimpleObject.class.getDeclaredMethod("getSimpleProperty");
        try {
            new MethodInjectionPoint(method);
            Assert.fail("Should not be able to create a MethodInjectionPoint for a not setter method");
        } catch (AssertionError expected){}
    }

    @Test
    public void testInjectionPointReuse() throws Exception {
        // Setup common injection point and injector
        Field simpleField = SimpleObject.class.getDeclaredField("simpleProperty");
        InjectionPoint injectionPoint = new FieldInjectionPoint(simpleField);

        // Reuse same injection point and injector for multiple targets
        injector.inject(simpleObject, injectionPoint, new SimpleValueRetriever("Test Value One"));
        org.junit.Assert.assertEquals("Test Value One", simpleObject.getSimpleProperty());

        simpleObject = new SimpleObject();
        injector.inject(simpleObject, injectionPoint, new SimpleValueRetriever("Test Value Two"));
        org.junit.Assert.assertEquals("Test Value Two", simpleObject.getSimpleProperty());

        simpleObject = new SimpleObject();
        injector.inject(simpleObject, injectionPoint, new SimpleValueRetriever("Test Value Three"));
        org.junit.Assert.assertEquals("Test Value Three", simpleObject.getSimpleProperty());

        simpleObject = new SimpleObject();
        injector.inject(simpleObject, injectionPoint, new SimpleValueRetriever("Test Value Four"));
        org.junit.Assert.assertEquals("Test Value Four", simpleObject.getSimpleProperty());
    }
}
