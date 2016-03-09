/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;

public interface Navigator {

    Position getPosition();

    void setPosition(Position position);

    double getHeading();

    void setHeading(double headingDegrees);

    double getTilt();

    void setTilt(double tiltDegrees);

    double getRoll();

    void setRoll(double rollDegrees);

    double getFieldOfView();

    void setFieldOfView(double fovyDegrees);

    void applyState(DrawContext dc);
}
