/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.ogc.gml.GmlParser;
import gov.nasa.worldwind.util.xml.XmlModelParser;

public class WcsXmlParser extends XmlModelParser {

    protected String wcs20Namespace = "http://www.opengis.net/wcs/2.0";

    public WcsXmlParser() {
        registerGmlModels();
        registerWcs20Models(wcs20Namespace);
    }

    protected void registerGmlModels() {
        registerAll(new GmlParser());
    }

    protected void registerWcs20Models(String namespace) {
        registerXmlModel(namespace, "CoverageDescriptions", Wcs201CoverageDescriptions.class);
        registerXmlModel(namespace, "CoverageDescription", Wcs201CoverageDescription.class);
        registerTxtModel(namespace, "CoverageId");
    }
}
