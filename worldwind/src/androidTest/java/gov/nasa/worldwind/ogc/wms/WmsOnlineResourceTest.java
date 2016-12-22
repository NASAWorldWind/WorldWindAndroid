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

import javax.xml.namespace.QName;

import gov.nasa.worldwind.R;
import gov.nasa.worldwind.util.xml.XmlElementModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsOnlineResourceTest {

    @Test
    public void testOnlineResource_ParseAndReadSampleWms() throws IOException, XmlPullParserException {

        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_sample_wms_xml);
        context.setParserInput(is);
        context.registerParsableModel(
            new QName(XmlPullParserContext.DEFAULT_NAMESPACE, "OnlineResource"),
            new WmsOnlineResource());

        XmlElementModel elementModel = new XmlElementModel(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;
        do {
            o = elementModel.read(context);
        } while (o != null);
        String actualLink = ((WmsOnlineResource) ((XmlElementModel) elementModel.getField("Service"))
            .getField("OnlineResource")).getHref();

        assertEquals("test href link",
            "https://basemap.nationalmap.gov/arcgis/services/USGSTopo/MapServer/WmsServer?",
            actualLink);

    }

}
