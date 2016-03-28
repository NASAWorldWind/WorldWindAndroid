/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.render.DrawContext;

public interface Navigator {

    double getLatitude();

    Navigator setLatitude(double latitude);

    double getLongitude();

    Navigator setLongitude(double longitude);

    double getAltitude();

    Navigator setAltitude(double altitude);

    double getHeading();

    Navigator setHeading(double headingDegrees);

    double getTilt();

    Navigator setTilt(double tiltDegrees);

    double getRoll();

    Navigator setRoll(double rollDegrees);

    double getFieldOfView();

    Navigator setFieldOfView(double fovyDegrees);

    Camera getAsCamera(Globe globe, Camera result);

    Navigator setAsCamera(Globe globe, Camera camera);

    LookAt getAsLookAt(Globe globe, LookAt result);

    Navigator setAsLookAt(Globe globe, LookAt lookAt);

    void applyState(DrawContext dc);
}
