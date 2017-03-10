/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Xml;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TextModelTest {

    public static final String TEST_ELEMENT_NAME = "MyTextValue";

    @Test
    public void testGetValue() throws Exception {
        String elementValue = "3.14159018";
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<" + TEST_ELEMENT_NAME + ">\n" +
            "\t" + elementValue + "\n" +
            "</" + TEST_ELEMENT_NAME + ">";
        XmlPullParser xpp = Xml.newPullParser();
        xpp.setInput(new ByteArrayInputStream(xml.getBytes()), null /*inputEncoding*/);

        XmlModelParser parser = new XmlModelParser();
        parser.setPullParser(xpp);
        parser.registerTxtModel("", TEST_ELEMENT_NAME);
        Object result = parser.parse();

        assertEquals("Text value", elementValue, result);
    }
}
