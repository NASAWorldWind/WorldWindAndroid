/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Position;

public interface Navigator {

    Position getEyePosition();

    void setEyePosition(Position eyePosition);

    double getHeading();

    void setHeading(double heading);

    double getTilt();

    void setTilt(double tilt);

    double getRoll();

    void setRoll(double roll);

    double getFieldOfView();

    void setFieldOfView(double fieldOfView);

    // TODO methods for setting from kml-style camera and look-at arguments

    // TODO method for applying state to DrawContext, such as NavigatorState
}
