/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import gov.nasa.worldwind.util.xml.XmlModelParser;

public class GmlParser extends XmlModelParser {

    protected String gml32Namespace = "http://www.opengis.net/gml/3.2";

    public GmlParser() {
        registerGmlModels(gml32Namespace);
    }

    protected void registerGmlModels(String namespace) {
        registerXmlModel(namespace, "AbstractFeature", GmlAbstractFeature.class);
        registerXmlModel(namespace, "AbstractGeometry", GmlAbstractGeometry.class);
        registerXmlModel(namespace, "AbstractGML", GmlAbstractGml.class);
        registerTxtModel(namespace, "axisLabels");
        registerTxtModel(namespace, "axisName");
        registerXmlModel(namespace, "boundedBy", GmlBoundingShape.class);
        registerTxtModel(namespace, "dimension");
        registerXmlModel(namespace, "domainSet", GmlDomainSet.class);
        registerXmlModel(namespace, "Envelope", GmlEnvelope.class);
        registerXmlModel(namespace, "_GeometricPrimitive", GmlAbstractGeometricPrimitive.class);
        registerTxtModel(namespace, "gid");
        registerXmlModel(namespace, "Grid", GmlGrid.class);
        registerXmlModel(namespace, "GridEnvelope", GmlGridEnvelope.class);
        registerXmlModel(namespace, "high", GmlIntegerList.class);
        registerXmlModel(namespace, "limits", GmlGridLimits.class);
        registerXmlModel(namespace, "low", GmlIntegerList.class);
        registerXmlModel(namespace, "lowerCorner", GmlDirectPosition.class);
        registerTxtModel(namespace, "nilReason");
        registerXmlModel(namespace, "offsetVector", GmlVector.class);
        registerXmlModel(namespace, "origin", GmlPointProperty.class);
        registerXmlModel(namespace, "Point", GmlPoint.class);
        registerXmlModel(namespace, "pos", GmlDirectPosition.class);
        registerXmlModel(namespace, "RectifiedGrid", GmlRectifiedGrid.class);
        registerTxtModel(namespace, "srsName");
        registerTxtModel(namespace, "srsDimension");
        registerTxtModel(namespace, "uomLabels");
        registerXmlModel(namespace, "upperCorner", GmlDirectPosition.class);
    }
}
