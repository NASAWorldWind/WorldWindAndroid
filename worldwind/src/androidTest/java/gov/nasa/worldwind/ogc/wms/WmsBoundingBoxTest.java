/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsBoundingBoxTest {

    public static final double DELTA = 1e-9;

    @Test
    public void testBoundingBox_ParseAndReadSampleWms() throws XmlPullParserException, IOException {

        // Sample xml
        String xml = "<BoundingBox xmlns=\"http://www.opengis.net/wms\" CRS=\"EPSG:3857\" minx=\"-20074053.901178\" miny=\"-30281451.060015\" maxx=\"20074053.888922\" maxy=\"30281451.072272\"/>";
        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        context.setParserInput(is);
        WmsBoundingBox boundingBox = new WmsBoundingBox(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;

        do {
            o = boundingBox.read(context);
        } while (o != null);

        assertEquals("reference system",
            "EPSG:3857",
            boundingBox.getCRS());
        assertEquals("minx", Double.parseDouble("-20074053.901178"), boundingBox.getMinx(), DELTA);
        assertEquals("miny", Double.parseDouble("-30281451.060015"), boundingBox.getMiny(), DELTA);
        assertEquals("maxx", Double.parseDouble("20074053.888922"), boundingBox.getMaxx(), DELTA);
        assertEquals("maxy", Double.parseDouble("30281451.072272"), boundingBox.getMaxy(), DELTA);
    }
}
