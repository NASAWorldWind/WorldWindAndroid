/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import gov.nasa.worldwind.util.xml.XmlModelParser;

public class GmlParser extends XmlModelParser {

    protected String gml32Namespace = "http://www.opengis.net/gml/3.2";

    public GmlParser() {
        this.registerGmlModels(this.gml32Namespace);
    }

    protected void registerGmlModels(String namespace) {
        this.registerTxtModel(namespace, "axisLabels");
        this.registerXmlModel(namespace, "AbstractFeature", GmlAbstractFeature.class);
        this.registerXmlModel(namespace, "AbstractGML", GmlAbstractGml.class);
        this.registerXmlModel(namespace, "boundedBy", GmlBoundingShape.class);
        this.registerXmlModel(namespace, "domainSet", GmlDomainSet.class);
        this.registerXmlModel(namespace, "Envelope", GmlEnvelope.class);
        this.registerXmlModel(namespace, "lowerCorner", GmlDirectPosition.class);
        this.registerTxtModel(namespace, "nilReason");
        this.registerTxtModel(namespace, "srsName");
        this.registerTxtModel(namespace, "srsDimension");
        this.registerTxtModel(namespace, "uomLabels");
        this.registerXmlModel(namespace, "upperCorner", GmlDirectPosition.class);
    }
}
