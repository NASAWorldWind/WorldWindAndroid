/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import java.util.ArrayList;
import java.util.List;

public class GmlRectifiedGrid extends GmlGrid {

    protected GmlPointProperty origin;

    protected List<GmlVector> offsetVector = new ArrayList<>();

    public GmlRectifiedGrid() {
    }

    public GmlPointProperty getOrigin() {
        return origin;
    }

    public List<GmlVector> getOffsetVector() {
        return offsetVector;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "origin":
                origin = (GmlPointProperty) value;
                break;
            case "offsetVector":
                offsetVector.add((GmlVector) value);
                break;
        }
    }
}
