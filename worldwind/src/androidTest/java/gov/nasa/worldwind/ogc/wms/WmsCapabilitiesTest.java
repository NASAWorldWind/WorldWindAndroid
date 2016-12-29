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

import gov.nasa.worldwind.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsCapabilitiesTest {

    @Test
    public void testCapabilities_readAndParseSpecXml() throws Exception {

        // Initialize the context and basic model
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_wms_130_spec);

        WmsCapabilities capabilities = WmsCapabilities.getCapabilities(is);

        assertEquals("test version", "1.3.0", capabilities.getVersion());
    }
}
