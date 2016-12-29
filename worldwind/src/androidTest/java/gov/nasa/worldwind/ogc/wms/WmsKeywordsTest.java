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

import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsKeywordsTest {

    @Test
    public void testWmsKeywords_ReadAndParseSampleWms() throws Exception {

        // Sample XML
        String xml = "<KeywordList xmlns=\"http://www.opengis.net/wms\">\n" +
            "            <Keyword>bird</Keyword>\n" +
            "            <Keyword>roadrunner</Keyword>\n" +
            "            <Keyword>ambush</Keyword>\n" +
            "        </KeywordList>";
        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        context.setParserInput(is);
        WmsKeywords elementModel = new WmsKeywords(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;

        do {
            o = elementModel.read(context);
        } while (o != null);

        assertEquals("test list size", 3, elementModel.getKeywords().size());
        assertEquals("test list contents", true, elementModel.getKeywords().contains("bird"));
        assertEquals("test list contents", true, elementModel.getKeywords().contains("roadrunner"));
        assertEquals("test list contents", true, elementModel.getKeywords().contains("ambush"));
    }
}
