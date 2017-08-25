/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

public class GmlPoint extends GmlAbstractGeometricPrimitive {

    protected GmlDirectPosition pos;

    public GmlPoint() {
    }

    public GmlDirectPosition getPos() {
        return pos;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "pos":
                pos = (GmlDirectPosition) value;
                break;
        }
    }
}
