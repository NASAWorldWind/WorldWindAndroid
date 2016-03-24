/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.graphics.Rect;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logger;

public class BasicNavigator implements Navigator {

    protected double latitude;

    protected double longitude;

    protected double altitude;

    protected double heading;

    protected double tilt;

    protected double roll;

    protected double fieldOfView = 45;

    protected Matrix4 modelview = new Matrix4();

    protected Matrix4 projection = new Matrix4();

    protected Matrix4 localCartesian = new Matrix4();

    public BasicNavigator() {
    }

    @Override
    public double getLatitude() {
        return this.latitude;
    }

    @Override
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public double getAltitude() {
        return this.altitude;
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public double getHeading() {
        return heading;
    }

    @Override
    public void setHeading(double headingDegrees) {
        this.heading = headingDegrees;
    }

    @Override
    public double getTilt() {
        return tilt;
    }

    @Override
    public void setTilt(double tiltDegrees) {
        this.tilt = tiltDegrees;
    }

    @Override
    public double getRoll() {
        return roll;
    }

    @Override
    public void setRoll(double rollDegrees) {
        this.roll = rollDegrees;
    }

    @Override
    public double getFieldOfView() {
        return fieldOfView;
    }

    @Override
    public void setFieldOfView(double fovyDegrees) {
        if (fovyDegrees <= 0 || fovyDegrees >= 180) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "setPosition", "invalidFieldOfView"));
        }

        this.fieldOfView = fovyDegrees;
    }

    @Override
    public Camera getAsCamera(WorldWindow wwd, Camera result) {
        if (wwd == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsCamera", "missingWorldWindow"));
        }

        if (result == null) {
            result = new Camera();
        }

        result.latitude = this.latitude;
        result.longitude = this.longitude;
        result.altitude = this.altitude;
        result.altitudeMode = WorldWind.ABSOLUTE;
        result.heading = this.heading;
        result.tilt = this.tilt;
        result.roll = this.roll;

        return result;
    }

    @Override
    public void setAsCamera(WorldWindow wwd, Camera camera) {
        if (wwd == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsCamera", "missingWorldWindow"));
        }

        if (camera == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsCamera", "missingCamera"));
        }

        this.latitude = camera.latitude;
        this.longitude = camera.longitude;
        this.altitude = camera.altitude; // TODO interpret based on altitude mode
        this.heading = camera.heading;
        this.tilt = camera.tilt;
        this.roll = camera.roll;
    }

    @Override
    public LookAt getAsLookAt(WorldWindow wwd, LookAt result) {
        if (wwd == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "getAsLookAt", "missingWorldWindow"));
        }

        if (result == null) {
            result = new LookAt();
        }

        // TODO

        return result;
    }

    @Override
    public void setAsLookAt(WorldWindow wwd, LookAt lookAt) {
        if (wwd == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsLookAt", "missingWorldWindow"));
        }

        if (lookAt == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicNavigator", "setAsLookAt", "missingLookAt"));
        }

        // TODO
    }

    public void applyState(DrawContext dc) {
        // TODO consider merging Navigator with WorldWindow
        //
        // TODO what happens if the responsibility of applying "camera" state is moved to the WorldWindow, or the
        // TODO FrameController? what other component needs access to computations done here? (see NavigatorState in
        // TODO WebWW)
        // TODO - seems that only rayFromScreenPoint is needed in WebWW, but possibly other similar utilities
        // TODO - so what happens if we put this in WorldWindow? can the NavigationController get what it needs?
        // TODO - what about the application?
        // TODO - what about applications desiring to override this behavior. framecontroller seems like the best place
        // TODO   for that
        //
        // TODO if framecontroller takes this responsibility, what's involved with computing a ray from a screen point
        // TODO in a controller, or in an application?
        // TODO - what about intersecting that ray with the terrain?
        // TODO - what about getting the pixel size at a distance?

        dc.setEyePosition(new Position(this.latitude, this.longitude, this.altitude)); // TODO eliminate allocation
        dc.setHeading(this.heading);
        dc.setTilt(this.tilt);
        dc.setRoll(this.roll);
        dc.setFieldOfView(this.fieldOfView);

        this.computeModelview(dc.getGlobe(), this.modelview);
        this.computeProjection(dc, this.projection);
        dc.setModelviewProjection(this.modelview, this.projection);
    }

    protected Matrix4 computeModelview(Globe globe, Matrix4 result) {

        // Initialize to the identity matrix.
        result.setToIdentity();

        // Transform by heading, tilt and roll, inverting the rotation angles.
        result.multiplyByRotation(0, 0, 1, -this.roll); // rotate clockwise about the Z axis
        result.multiplyByRotation(1, 0, 0, -this.tilt); // rotate clockwise about the X axis
        result.multiplyByRotation(0, 0, 1, this.heading); // rotate counter-clockwise about the Z axis (again)

        // Transform by the inverse of the local cartesian transform at the navigator's position.
        globe.geographicToCartesianTransform(this.latitude, this.longitude, this.altitude, this.localCartesian);
        this.localCartesian.invertOrthonormal();
        result.multiplyByMatrix(this.localCartesian);

        return result;
    }

    protected Matrix4 computeProjection(DrawContext dc, Matrix4 result) {

        // TODO compute clip plane distances appropriate for the current frame
        double near = 1e3;
        double far = dc.getGlobe().getEquatorialRadius() + this.altitude;
        Rect viewport = dc.getViewport();
        result.setToPerspectiveProjection(viewport.width(), viewport.height(), this.fieldOfView, near, far);

        return result;
    }
}
