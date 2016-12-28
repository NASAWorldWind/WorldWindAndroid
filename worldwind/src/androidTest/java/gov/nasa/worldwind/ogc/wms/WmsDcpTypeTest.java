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
import java.util.List;

import gov.nasa.worldwind.R;
import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsDcpTypeTest {

    @Test
    public void testDcpType_ParseAndReadSampleWms() throws Exception {

        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_sample_wms_xml);
        context.setParserInput(is);
        XmlModel elementModel = new XmlModel(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;

        do {
            o = elementModel.read(context);
        } while (o != null);
        XmlModel stepOne = (XmlModel) elementModel.getField("Capability");
        stepOne = (XmlModel) stepOne.getField("Request");
        stepOne = (XmlModel) stepOne.getField("GetCapabilities");
        WmsDcpType dcpType = (WmsDcpType) stepOne.getField("DCPType");
        List<WmsDcpType.DcpInfo> types = dcpType.getDcpInfos();

        assertEquals("test DCP protocol", "HTTP", types.get(0).protocol);
        assertEquals("test DCP method", "Get", types.get(0).method);
        assertEquals(
            "test DCP resource url",
            "https://basemap.nationalmap.gov/arcgis/services/USGSTopo/MapServer/WmsServer?",
            types.get(0).onlineResource.getHref());
    }
}
