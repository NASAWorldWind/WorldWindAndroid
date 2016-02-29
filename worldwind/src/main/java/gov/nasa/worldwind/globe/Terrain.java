/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.AltitudeMode;
import gov.nasa.worldwind.geom.Vec3;

public interface Terrain {

    Vec3 computeSurfacePoint(double latitude, double longitude, double altitude, AltitudeMode altitudeMode, Vec3 result);
}
