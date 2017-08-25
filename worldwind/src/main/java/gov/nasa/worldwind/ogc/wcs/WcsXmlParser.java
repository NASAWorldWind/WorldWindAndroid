/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import gov.nasa.worldwind.ogc.gml.GmlParser;
import gov.nasa.worldwind.ogc.ows.OwsXmlParser;
import gov.nasa.worldwind.util.xml.XmlModelParser;

public class WcsXmlParser extends XmlModelParser {

    protected String wcs20Namespace = "http://www.opengis.net/wcs/2.0";

    public WcsXmlParser() {
        registerOwsModels();
        registerGmlModels();
        registerWcs20Models(wcs20Namespace);
    }

    public static Object parse(InputStream inputStream) throws IOException, XmlPullParserException {
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputStream, null /*inputEncoding*/);

        XmlModelParser modelParser = new WcsXmlParser();
        modelParser.setPullParser(pullParser);

        return modelParser.parse();
    }

    protected void registerOwsModels() {
        registerAllModels(new OwsXmlParser());
    }

    protected void registerGmlModels() {
        registerAllModels(new GmlParser());
    }

    protected void registerWcs20Models(String namespace) {
        registerXmlModel(namespace, "CoverageDescriptions", Wcs201CoverageDescriptions.class);
        registerXmlModel(namespace, "CoverageDescription", Wcs201CoverageDescription.class);
        registerTxtModel(namespace, "CoverageId");
    }
}
