/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class WorldWindowTest {

    @Mock
    Context mockContext;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testConstructor_WithContext() throws Exception {
        assertNotNull("null Context", new WorldWindow(null));
        assertNotNull("mock Context", new WorldWindow(mockContext));
    }

    @Ignore("not implemented")
    @Test
    public void testConstructor_WithContextAndAttr() throws Exception {
        fail("The test case is a stub");
    }

    @Ignore
    @Test
    public void testGetNavigator() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testSetNavigator() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testGetNavigatorController() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testSetNavigatorController() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testGetFrameController() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testSetFrameController() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testGetFrameStatistics() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testGetGlobe() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testSetGlobe() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testGetLayers() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testSetLayers() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testGetVerticalExaggeration() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testSetVerticalExaggeration() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testOnSurfaceCreated() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testOnSurfaceChanged() throws Exception {
        fail("The test case is a stub");

    }

    @Ignore
    @Test
    public void testOnDrawFrame() throws Exception {
        fail("The test case is a stub");

    }
}