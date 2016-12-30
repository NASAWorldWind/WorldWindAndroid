/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import android.content.res.Resources;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import gov.nasa.worldwind.test.R;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsCapabilitiesTest {

    @Test
    public void testCapabilities_readAndParseSample111Xml() throws Exception {

        // Initialize the context and basic model
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_wms_111_sample);

        WmsCapabilities capabilities = WmsCapabilities.getCapabilities(is, "");

        assertNotNull("image formats", capabilities.getImageFormats());
        assertNotNull("service information", capabilities.getServiceInformation());
        assertNotNull("capability information", capabilities.getCapabilityInformation());
        assertEquals("test version", "1.1.1", capabilities.getVersion());
    }

    @Test
    public void testCapabilities_readAndParseSpec111Xml() throws Exception {

        // Initialize the context and basic model
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_wms_111_spec);

        WmsCapabilities capabilities = WmsCapabilities.getCapabilities(is, "");

        assertNotNull("image formats", capabilities.getImageFormats());
        assertNotNull("service information", capabilities.getServiceInformation());
        assertNotNull("capability information", capabilities.getCapabilityInformation());
        assertEquals("test version", "1.1.1", capabilities.getVersion());
    }

    @Test
    public void testCapabilities_readAndParseSample130Xml() throws Exception {

        // Initialize the context and basic model
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_wms_130_sample);

        WmsCapabilities capabilities = WmsCapabilities.getCapabilities(is, XmlPullParserContext.DEFAULT_NAMESPACE);

        assertNotNull("image formats", capabilities.getImageFormats());
        assertNotNull("service information", capabilities.getServiceInformation());
        assertNotNull("capability information", capabilities.getCapabilityInformation());
        assertEquals("test version", "1.3.0", capabilities.getVersion());
    }

    @Test
    public void testCapabilities_readAndParseSpec130Xml() throws Exception {

        // Initialize the context and basic model
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_wms_130_spec);

        WmsCapabilities capabilities = WmsCapabilities.getCapabilities(is, XmlPullParserContext.DEFAULT_NAMESPACE);

        assertNotNull("image formats", capabilities.getImageFormats());
        assertNotNull("service information", capabilities.getServiceInformation());
        assertNotNull("capability information", capabilities.getCapabilityInformation());
        assertEquals("test version", "1.3.0", capabilities.getVersion());
    }
}
