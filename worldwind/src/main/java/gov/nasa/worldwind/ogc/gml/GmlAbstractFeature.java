/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

public class GmlAbstractFeature extends GmlAbstractGml {

    protected GmlBoundingShape boundedBy;

    // TODO location

    public GmlAbstractFeature() {
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        if (keyName.equals("boundedBy")) {
            this.boundedBy = (GmlBoundingShape) value;
        }
    }
}
