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

import gov.nasa.worldwind.test.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class XmlModelParseTest {

    @Test
    public void testBasicModelParse() throws Exception {

        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(null);
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_basic_xml);
        context.setParserInput(is);

        XmlModel basicElementModel = new XmlModel(null);
        Object o;
        do {
            o = basicElementModel.read(context);
        } while (o != null);

        assertEquals("fields captured", 5, basicElementModel.getFields().size());
        assertEquals("to field captured", "Tove",
            ((XmlModel) basicElementModel.getFields().get("to"))
                .getField(XmlModel.CHARACTERS_CONTENT).toString());
        assertEquals("from field captured", "Jani",
            ((XmlModel) basicElementModel.getFields().get("from"))
                .getField(XmlModel.CHARACTERS_CONTENT).toString());
        assertEquals("heading field captured", "Reminder",
            ((XmlModel) basicElementModel.getFields().get("heading"))
                .getField(XmlModel.CHARACTERS_CONTENT).toString());
        assertEquals("body field captured", "Don't forget me this weekend!",
            ((XmlModel) basicElementModel.getFields().get("body"))
                .getField(XmlModel.CHARACTERS_CONTENT).toString());
    }

    @Test
    public void testAdvancedModelParse() throws Exception {

        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(null);
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_advanced_xml);
        context.setParserInput(is);

        XmlModel advancedElementModel = new XmlModel(null);
        Object o;
        do {
            o = advancedElementModel.read(context);
        } while (o != null);

        assertEquals("fields captured", 5, advancedElementModel.getFields().size());
        assertEquals("root element attributes", "nice", advancedElementModel.getField("style").toString());
        assertEquals("root element attributes", "the web", advancedElementModel.getField("source").toString());
        XmlModel schoolModel = (XmlModel) advancedElementModel.getField("MySchool");
        assertEquals("school element attributes", "undergraduate", schoolModel.getField("level").toString());
        XmlModel schoolIdentityModel = (XmlModel) schoolModel.getField("Identity");
        assertEquals("school identity elements", "Colorado State University",
            ((XmlModel) schoolIdentityModel.getField("Name")).getField(XmlModel.CHARACTERS_CONTENT).toString());
        assertEquals("school identity elements", "1870",
            ((XmlModel) schoolIdentityModel.getField("Established")).getField(XmlModel.CHARACTERS_CONTENT).toString());
        assertEquals("school identity elements", "combined",
            ((XmlModel) schoolIdentityModel.getField("StudentBody")).getField("population").toString());
        assertEquals("school identity elements", "33468",
            ((XmlModel) schoolIdentityModel.getField("StudentBody")).getField("size").toString());
        // TODO additional tests of the other elements
    }

}
