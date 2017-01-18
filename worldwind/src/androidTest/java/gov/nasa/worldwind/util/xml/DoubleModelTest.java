/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DoubleModelTest {

    public static final String TEST_ELEMENT_NAME = "MyDoubleValue";

    public static final double DELTA = 1e-9;

    @Test
    public void testGetValue() throws Exception {

        double elementValue = 3.14159018;
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<" + TEST_ELEMENT_NAME + ">\n" +
            "\t" + elementValue + "\n" +
            "</" + TEST_ELEMENT_NAME + ">";
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        XmlPullParserContext ctx = new XmlPullParserContext();
        ctx.setParserInput(is);
        DoubleModel doubleModel = new DoubleModel();

        doubleModel.read(ctx);

        assertEquals("Double Value", elementValue, doubleModel.getValue(), DELTA);
    }
}
