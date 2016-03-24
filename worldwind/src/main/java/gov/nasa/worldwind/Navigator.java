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

    void setLatitude(double latitude);

    double getLongitude();

    void setLongitude(double longitude);

    double getAltitude();

    void setAltitude(double altitude);

    double getHeading();

    void setHeading(double headingDegrees);

    double getTilt();

    void setTilt(double tiltDegrees);

    double getRoll();

    void setRoll(double rollDegrees);

    double getFieldOfView();

    void setFieldOfView(double fovyDegrees);

    Camera getAsCamera(WorldWindow wwd, Camera result);

    void setAsCamera(WorldWindow wwd, Camera camera);

    LookAt getAsLookAt(WorldWindow wwd, LookAt result);

    void setAsLookAt(WorldWindow wwd, LookAt lookAt);

    void applyState(DrawContext dc);
}
