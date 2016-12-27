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
import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsBoundingBoxTest {

    public static final double DELTA = 1e-9;

    @Test
    public void testBoundingBox_ParseAndReadSampleWms() throws XmlPullParserException, IOException {

        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_sample_wms_xml);
        context.setParserInput(is);
        context.registerParsableModel(
            new QName(XmlPullParserContext.DEFAULT_NAMESPACE, "BoundingBox"),
            new WmsBoundingBox(XmlPullParserContext.DEFAULT_NAMESPACE));

        XmlModel elementModel = new XmlModel(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;
        do {
            o = elementModel.read(context);
        } while (o != null);
        WmsBoundingBox boundingBox = (WmsBoundingBox) ((XmlModel) ((XmlModel) elementModel.getField("Capability"))
            .getField("Layer")).getField("BoundingBox");

        assertEquals("reference system",
            "EPSG:3857",
            boundingBox.getCRS());
        assertEquals("minx", Double.parseDouble("-20074053.901178"), boundingBox.getMinx(), DELTA);
        assertEquals("miny", Double.parseDouble("-30281451.060015"), boundingBox.getMiny(), DELTA);
        assertEquals("maxx", Double.parseDouble("20074053.888922"), boundingBox.getMaxx(), DELTA);
        assertEquals("maxy", Double.parseDouble("30281451.072272"), boundingBox.getMaxy(), DELTA);
    }
}
