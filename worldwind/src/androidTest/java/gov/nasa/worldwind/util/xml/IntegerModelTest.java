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
public class IntegerModelTest {

    public static final String TEST_ELEMENT_NAME = "MyIntegerValue";

    @Test
    public void testGetValue() throws Exception {

        String namespace = "";
        int elementValue = 24601;
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<" + TEST_ELEMENT_NAME + ">\n" +
            "\t" + elementValue + "\n" +
            "</" + TEST_ELEMENT_NAME + ">";
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        XmlPullParserContext ctx = new XmlPullParserContext(namespace);
        ctx.setParserInput(is);
        IntegerModel integerModel = new IntegerModel(namespace);

        integerModel.read(ctx);

        assertEquals("Integer Value", elementValue, integerModel.getValue().intValue());
    }
}
