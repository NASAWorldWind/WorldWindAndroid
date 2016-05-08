/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;

public interface Terrain {

    Sector getSector();

    Vec3 geographicToCartesian(double latitude, double longitude, double altitude,
                               @WorldWind.AltitudeMode int altitudeMode, Vec3 result);
}
