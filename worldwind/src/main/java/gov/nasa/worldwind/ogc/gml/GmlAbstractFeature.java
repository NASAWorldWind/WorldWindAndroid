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

    public GmlBoundingShape getBoundedBy() {
        return boundedBy;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "boundedBy":
                boundedBy = (GmlBoundingShape) value;
                break;
        }
    }
}
