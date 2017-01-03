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

import javax.xml.namespace.QName;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class NameStringModelTest {

    public static final String TEST_ELEMENT_NAME = "MyImportantTagName";

    @Test
    public void testGetValue() throws Exception {

        String namespace = "";
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<EnclosingElement>\n" +
            "\t<" + TEST_ELEMENT_NAME + ">\n" +
            "\t\t<SelfClosingTagWithAttrValuesNotAssociatedWithTest zoom=\"100\"/>\n" +
            "\t</" + TEST_ELEMENT_NAME + ">\n" +
            "</EnclosingElement>";
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        XmlPullParserContext ctx = new XmlPullParserContext(namespace);
        ctx.setParserInput(is);
        ctx.registerParsableModel(new QName(namespace, TEST_ELEMENT_NAME), new NameStringModel(namespace));
        XmlModel xmlModel = new XmlModel(namespace);

        xmlModel.read(ctx);
        NameStringModel nameStringModel = (NameStringModel) xmlModel.getField(new QName(namespace, TEST_ELEMENT_NAME));

        assertEquals("Integer Value", TEST_ELEMENT_NAME, nameStringModel.getCharactersContent());
    }
}
