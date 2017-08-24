/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

public class GmlRectifiedGrid extends GmlGrid {

    // TODO GmlPointProperty origin

    // TODO List<GmlVector> offsetVector

    public GmlRectifiedGrid() {
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);
    }
}
