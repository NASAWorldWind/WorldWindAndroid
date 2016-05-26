/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logger;

public class PickedObject {

    protected boolean isOnTop;

    protected Object userObject;

    protected Position position;

    @WorldWind.AltitudeMode
    protected int altitudeMode = WorldWind.ABSOLUTE;

    protected Layer layer;

    protected int identifier;

    protected PickedObject() {
    }

    public static PickedObject fromRenderable(Renderable renderable, Position position, @WorldWind.AltitudeMode int altitudeMode, Layer layer, int identifier) {
        if (renderable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObject", "fromRenderable", "missingRenderable"));
        }

        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObject", "fromRenderable", "missingPosition"));
        }

        if (layer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObject", "fromRenderable", "missingLayer"));
        }

        PickedObject po = new PickedObject();
        po.userObject = (renderable.getPickDelegate() != null) ? renderable.getPickDelegate() : renderable;
        po.position = new Position(position);
        po.altitudeMode = altitudeMode;
        po.layer = layer;
        po.identifier = identifier;
        return po;
    }

    public static PickedObject fromRenderable(Renderable renderable, Layer layer, int identifier) {
        if (renderable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObject", "fromRenderable", "missingRenderable"));
        }

        if (layer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObject", "fromRenderable", "missingLayer"));
        }

        PickedObject po = new PickedObject();
        po.userObject = (renderable.getPickDelegate() != null) ? renderable.getPickDelegate() : renderable;
        po.layer = layer;
        po.identifier = identifier;
        return po;
    }

    public static PickedObject fromTerrain(Position position, int identifier) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObject", "fromTerrain", "missingPosition"));
        }

        PickedObject po = new PickedObject();
        po.position = new Position(position);
        po.altitudeMode = WorldWind.ABSOLUTE;
        po.userObject = po.position;
        po.identifier = identifier;
        return po;
    }

    public static Color identifierToUniqueColor(int identifier, Color result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObject", "identifierToUniqueColor", "missingResult"));
        }

        int r8 = (identifier >> 16) & 0xFF;
        int g8 = (identifier >> 8) & 0xFF;
        int b8 = (identifier) & 0xFF;

        result.red = r8 / (float) 0xFF;
        result.green = g8 / (float) 0xFF;
        result.blue = b8 / (float) 0xFF;
        result.alpha = 1;

        return result;
    }

    public static int uniqueColorToIdentifier(Color color) {
        if (color == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObject", "uniqueColorToIdentifier", "missingColor"));
        }

        int r8 = Math.round(color.red * 0xFF);
        int g8 = Math.round(color.green * 0xFF);
        int b8 = Math.round(color.blue * 0xFF);

        return (r8 << 16) | (g8 << 8) | (b8);
    }

    @Override
    public String toString() {
        return "PickedObject{" +
            "isOnTop=" + this.isOnTop +
            ", userObject=" + this.userObject +
            ", position=" + this.position +
            ", altitudeMode=" + this.altitudeMode +
            ", layer=" + this.layer +
            ", identifier=" + this.identifier +
            '}';
    }

    public boolean isOnTop() {
        return this.isOnTop;
    }

    protected void markOnTop() {
        this.isOnTop = true;
    }

    public boolean isTerrain() {
        return this.userObject == this.position;
    }

    public Object getUserObject() {
        return this.userObject;
    }

    public Position getPosition() {
        return this.position;
    }

    @WorldWind.AltitudeMode
    public int getAltitudeMode() {
        return this.altitudeMode;
    }

    public Layer getLayer() {
        return this.layer;
    }

    public int getIdentifier() {
        return this.identifier;
    }
}
