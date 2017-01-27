/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.Arrays;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logger;

public class OwsWgs84BoundingBox extends OwsBoundingBox {

    public Sector getSector() {

        String[] lowerValues = this.lowerCorner.split("\\s+");
        String[] upperValues = this.upperCorner.split("\\s+");

        if (lowerValues.length != 2 || upperValues.length != 2) {
            Logger.logMessage(Logger.ERROR, "OwsWgs84BoundingBox", "getSector", "Error parsing the UpperCorner or " +
                "LowerCorner values: " + this.lowerCorner + " and " + this.upperCorner);
            return null;
        }

        try {
            double minLon = Double.parseDouble(lowerValues[0]);
            double minLat = Double.parseDouble(lowerValues[1]);
            double maxLon = Double.parseDouble(upperValues[0]);
            double maxLat = Double.parseDouble(upperValues[1]);

            return new Sector(minLat, minLon, maxLat - minLat, maxLon - minLon);
        } catch (NumberFormatException ex) {
            Logger.logMessage(Logger.ERROR, "OwsWgs84BoundingBox", "getSector", "Error parsing values from upper " +
                "and lower corner: " + Arrays.toString(lowerValues) + " and " + Arrays.toString(upperValues));
            return null;
        }
    }
}
