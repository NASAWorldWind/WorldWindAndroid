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
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class XmlElementModelParseTesting {

    @Test
    public void testSimpleModelParse() throws Exception {

        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext();
        Resources resources = getInstrumentation().getTargetContext().getResources();
        InputStream is = resources.openRawResource(R.raw.gov_nasa_worldwind_basic_xml);
        context.setParserInput(is);

        // Just assure this thing runs with the instrumentation
        XmlElementModel basicElementModel = new XmlElementModel();
        Object o = context;
        do {
            o = basicElementModel.read(context);
        } while (o != null);

        assertNull("ensure test works", o);
    }

}
