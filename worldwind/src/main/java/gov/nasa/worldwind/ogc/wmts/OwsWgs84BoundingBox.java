/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logger;

public class OwsWgs84BoundingBox extends OwsBoundingBox {

    public Sector getSector() {
        try {
            String[] lowerValues = this.lowerCorner.split("\\s+");
            String[] upperValues = this.upperCorner.split("\\s+");

            double minLon = Double.parseDouble(lowerValues[0]);
            double minLat = Double.parseDouble(lowerValues[1]);
            double maxLon = Double.parseDouble(upperValues[0]);
            double maxLat = Double.parseDouble(upperValues[1]);

            return new Sector(minLat, minLon, maxLat - minLat, maxLon - minLon);
        } catch (Exception ex) {
            Logger.logMessage(Logger.ERROR, "OwsWgs84BoundingBox", "getSector", "Error parsing bounding box corners, " +
                "LowerCorner=" + this.lowerCorner + " UpperCorner=" + this.upperCorner, ex);
            return null;
        }
    }
}
