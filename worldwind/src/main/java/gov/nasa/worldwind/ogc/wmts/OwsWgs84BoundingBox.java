/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.geom.Sector;

public class OwsWgs84BoundingBox extends OwsBoundingBox {

    @Override
    public Sector getSector() {
        double[] lowerLeft = this.parse2dCornerString(this.lowerCorner, true);
        if (lowerLeft == null) {
            return null;
        }
        double[] upperRight = this.parse2dCornerString(this.upperCorner, true);
        if (upperRight == null) {
            return null;
        }
        return new Sector(lowerLeft[1], lowerLeft[0], upperRight[1] - lowerLeft[1], upperRight[0] - lowerLeft[0]);
    }
}
