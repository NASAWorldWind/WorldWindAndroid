/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import gov.nasa.worldwind.util.xml.XmlModel;

public class GmlDomainSet extends XmlModel {

    protected GmlAbstractGeometry geometry;

    public GmlDomainSet() {
    }

    public GmlAbstractGeometry getGeometry() {
        return geometry;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        if (value instanceof GmlAbstractGeometry) { // we know the element type at parse time, but not it's name
            geometry = (GmlAbstractGeometry) value;
        }
    }
}
