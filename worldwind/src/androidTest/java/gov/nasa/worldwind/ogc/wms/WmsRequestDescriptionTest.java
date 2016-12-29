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
import java.util.HashSet;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsRequestDescriptionTest {

    @Test
    public void testRequestDescription_ParseAndReadSampleWms() throws IOException, XmlPullParserException {

        // Sample XML
        String XML = "<GetCapabilities xmlns=\"http://www.opengis.net/wms\">\n" +
            "\n" +
            "<Format>text/xml</Format>\n" +
            "\n" +
            "<DCPType>\n" +
            "\n" +
            "<HTTP>\n" +
            "\n" +
            "<Get>\n" +
            "\n" +
            "<OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
            "\n" +
            "xlink:type=\"simple\"\n" +
            "\n" +
            "xlink:href=\"http://hostname/path?\" />\n" +
            "\n" +
            "</Get>\n" +
            "\n" +
            "<Post>\n" +
            "\n" +
            "<OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
            "\n" +
            "xlink:type=\"simple\"\n" +
            "\n" +
            "xlink:href=\"http://hostname/path?\" />\n" +
            "\n" +
            "</Post>\n" +
            "\n" +
            "</HTTP>\n" +
            "\n" +
            "</DCPType>\n" +
            "\n" +
            "</GetCapabilities>\n";
        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        InputStream is = new ByteArrayInputStream(XML.getBytes());
        context.setParserInput(is);
        WmsRequestDescription elementModel = new WmsRequestDescription(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;
        Set<String> expectedFormats = new HashSet<>();
        expectedFormats.add("text/xml");

        do {
            o = elementModel.read(context);
        } while (o != null);

        assertEquals("request description test names", "GetCapabilities", elementModel.getRequestName());
        assertEquals("request description formats", expectedFormats, elementModel.getFormats());
        assertEquals(
            "request description online resource",
            "http://hostname/path?", elementModel.getDcpTypes().iterator().next()
                .getDcpInfos().get(0).onlineResource.getHref());
    }
}
