/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsLayerStyleTest {

    @Test
    public void testLayerStyle_parseAndReadSampleWms() throws Exception {

        // Sample XML data
        String xml = "<Style xmlns=\"http://www.opengis.net/wms\">\n" +
            "\n" +
            "<Name>USGS</Name>\n" +
            "\n" +
            "<Title>USGS Topo Map Style</Title>\n" +
            "\n" +
            "<Abstract>Features are shown in a style like that used in USGS topographic maps.</Abstract>\n" +
            "\n" +
            "<!-- A picture of a legend for a Layer in this Style -->\n" +
            "\n" +
            "<LegendURL width=\"72\" height=\"72\">\n" +
            "\n" +
            "<Format>image/gif</Format>\n" +
            "\n" +
            "<OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
            "\n" +
            "xlink:type=\"simple\"\n" +
            "\n" +
            "xlink:href=\"http://www.university.edu/legends/usgs.gif\" />\n" +
            "\n" +
            "</LegendURL>\n" +
            "\n" +
            "<!-- An XSL stylesheet describing how feature data will rendered to create\n" +
            "\n" +
            "a map of this layer. -->\n" +
            "\n" +
            "<StyleSheetURL>\n" +
            "\n" +
            "<Format>text/xsl</Format>\n" +
            "\n" +
            "<OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
            "\n" +
            "xlink:type=\"simple\"\n" +
            "\n" +
            "xlink:href=\"http://www.university.edu/stylesheets/usgs.xsl\" />\n" +
            "\n" +
            "</StyleSheetURL>\n" +
            "\n" +
            "</Style>";
        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        context.setParserInput(is);
        WmsLayerStyle elementModel = new WmsLayerStyle(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;

        do {
            o = elementModel.read(context);
        } while (o != null);

        assertEquals("style name", "USGS", elementModel.getName());
        assertEquals("style title", "USGS Topo Map Style", elementModel.getTitle());
        assertEquals("style abstract", "Features are shown in a style like that used in USGS topographic maps.", elementModel.getStyleAbstract());
        Set<WmsLogoUrl> legendUrls = elementModel.getLegendUrls();
        assertEquals("style legend count", 1, legendUrls.size());
        assertEquals("style legend width", 72, legendUrls.iterator().next().getWidth().intValue());
        assertEquals("style legend height", 72, legendUrls.iterator().next().getWidth().intValue());
        // TODO update WmsLogoUrl to provide getters to all nested properties like Format and OnlineResource
        //assertEquals("style legend format", "image/gif", legendUrls.iterator().next())
        WmsLayerInfoUrl styleSheetUrl = elementModel.getStyleSheetUrl();
        assertEquals("style sheet url format", "text/xsl", styleSheetUrl.getFormat());
        assertEquals("style sheet url online resource", "http://www.university.edu/stylesheets/usgs.xsl", styleSheetUrl.getOnlineResource().getHref());
    }
}
