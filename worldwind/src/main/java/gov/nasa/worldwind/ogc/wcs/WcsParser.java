/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.util.xml.XmlModelParser;

public class WcsParser extends XmlModelParser {

    protected String wcs20Namespace = "http://www.opengis.net/wcs/2.0";

    public WcsParser() {
        this.registerWcs20Models(this.wcs20Namespace);
        // TODO GML
    }

    protected void registerWcs20Models(String namespace) {
        this.registerXmlModel(namespace, "CoverageDescriptions", Wcs201CoverageDescriptions.class);
        this.registerXmlModel(namespace, "CoverageDescription", Wcs201CoverageDescription.class);
        this.registerTxtModel(namespace, "CoverageId");
    }
}
