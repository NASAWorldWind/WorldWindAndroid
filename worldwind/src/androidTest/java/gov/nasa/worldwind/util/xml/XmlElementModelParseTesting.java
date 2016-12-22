/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import android.content.res.Resources;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import gov.nasa.worldwind.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class XmlElementModelParseTesting {

    @Test
    public void testBasicModelParse() throws Exception {

        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext();
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_basic_xml);
        context.setParserInput(is);

        // Just assure this thing runs with the instrumentation
        XmlElementModel basicElementModel = new XmlElementModel();
        Object o;
        do {
            o = basicElementModel.read(context);
        } while (o != null);

        assertEquals("fields captured", 5, basicElementModel.getFields().size());
        assertEquals("to field captured", "Tove",
            ((XmlElementModel) basicElementModel.getFields().get("to"))
                .getField(XmlElementModel.CHARACTERS_CONTENT).toString());
        assertEquals("from field captured", "Jani",
            ((XmlElementModel) basicElementModel.getFields().get("from"))
                .getField(XmlElementModel.CHARACTERS_CONTENT).toString());
        assertEquals("heading field captured", "Reminder",
            ((XmlElementModel) basicElementModel.getFields().get("heading"))
                .getField(XmlElementModel.CHARACTERS_CONTENT).toString());
        assertEquals("body field captured", "Don't forget me this weekend!",
            ((XmlElementModel) basicElementModel.getFields().get("body"))
                .getField(XmlElementModel.CHARACTERS_CONTENT).toString());
    }

    @Test
    public void testAdvancedModelParse() throws Exception {

        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext();
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_advanced_xml);
        context.setParserInput(is);

        // Just assure this thing runs with the instrumentation
        XmlElementModel advancedElementModel = new XmlElementModel();
        Object o;
        do {
            o = advancedElementModel.read(context);
        } while (o != null);

        assertEquals("fields captured", 5, advancedElementModel.getFields().size());
        assertEquals("root element attributes", "nice", advancedElementModel.getField("style").toString());
        assertEquals("root element attributes", "the web", advancedElementModel.getField("source").toString());
        XmlElementModel schoolModel = (XmlElementModel) advancedElementModel.getField("MySchool");
        assertEquals("school element attributes", "undergraduate", schoolModel.getField("level").toString());
        XmlElementModel schoolIdentityModel = (XmlElementModel) schoolModel.getField("Identity");
        assertEquals("school identity elements", "Colorado State University",
            ((XmlElementModel) schoolIdentityModel.getField("Name")).getField(XmlElementModel.CHARACTERS_CONTENT).toString());
        assertEquals("school identity elements", "1870",
            ((XmlElementModel) schoolIdentityModel.getField("Established")).getField(XmlElementModel.CHARACTERS_CONTENT).toString());
        assertEquals("school identity elements", "combined",
            ((XmlElementModel) schoolIdentityModel.getField("StudentBody")).getField("population").toString());
        assertEquals("school identity elements", "33468",
            ((XmlElementModel) schoolIdentityModel.getField("StudentBody")).getField("size").toString());
        // TODO additional tests of the other elements
    }

}
