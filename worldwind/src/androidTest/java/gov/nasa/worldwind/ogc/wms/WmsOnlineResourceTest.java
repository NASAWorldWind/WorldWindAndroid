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
public class WmsOnlineResourceTest {

    @Test
    public void testOnlineResource_ParseAndReadSampleWms() throws IOException, XmlPullParserException {

        // Sample XML
        String XML = "<OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
            "xlink:type=\"simple\"\n" +
            "xlink:href=\"http://www.university.edu/stylesheets/usgs.xsl\" />\n";
        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        InputStream is = new ByteArrayInputStream(XML.getBytes());
        context.setParserInput(is);
        WmsOnlineResource elementModel = new WmsOnlineResource(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;

        do {
            o = elementModel.read(context);
        } while (o != null);

        assertEquals("test href link",
            "http://www.university.edu/stylesheets/usgs.xsl",
            elementModel.getHref());
        assertEquals("test link type",
            "simple",
            elementModel.getType());

    }

}
