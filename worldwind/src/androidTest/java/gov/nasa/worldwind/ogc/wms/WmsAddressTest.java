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
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import gov.nasa.worldwind.R;
import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsAddressTest {

    @Test
    public void testAddress_ParseAndReadSampleWms() throws XmlPullParserException, IOException {

        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_wms_130_sample);
        context.setParserInput(is);
        XmlModel elementModel = new XmlModel(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;

        do {
            o = elementModel.read(context);
        } while (o != null);
        WmsAddress wmsAddress = (WmsAddress) ((XmlModel) ((XmlModel) elementModel.getField("Service"))
            .getField("ContactInformation")).getField("ContactAddress");

        assertEquals("test address type", "Business", wmsAddress.getAddressType());
        assertEquals("test address", "1234 Hollywood Lane", wmsAddress.getAddress());
        assertEquals("test city", "Springfield", wmsAddress.getCity());
        assertEquals("test state", "Unknown", wmsAddress.getStateOrProvince());
        assertEquals("test postal code", "90210", wmsAddress.getPostCode());
        assertEquals("test country", "United States of America", wmsAddress.getCountry());

    }

}
