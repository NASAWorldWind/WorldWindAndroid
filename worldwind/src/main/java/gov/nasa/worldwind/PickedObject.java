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

    protected int identifier;

    protected Object userObject;

    protected Layer layer;

    protected Position terrainPosition;

    protected PickedObject() {
    }

    public static PickedObject fromRenderable(int identifier, Renderable renderable, Layer layer) {
        if (renderable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObject", "fromRenderable", "missingRenderable"));
        }

        if (layer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObject", "fromRenderable", "missingLayer"));
        }

        PickedObject po = new PickedObject();
        po.identifier = identifier;
        po.userObject = (renderable.getPickDelegate() != null) ? renderable.getPickDelegate() : renderable;
        po.layer = layer;
        return po;
    }

    public static PickedObject fromTerrain(int identifier, Position position) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObject", "fromTerrain", "missingPosition"));
        }

        Position positionCopy = new Position(position);
        PickedObject po = new PickedObject();
        po.identifier = identifier;
        po.userObject = positionCopy;
        po.terrainPosition = positionCopy;
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
            ", identifier=" + this.identifier +
            ", userObject=" + this.userObject +
            ", layer=" + this.layer +
            ", terrainPosition=" + this.terrainPosition +
            '}';
    }

    public boolean isOnTop() {
        return this.isOnTop;
    }

    protected void markOnTop() {
        this.isOnTop = true;
    }

    public int getIdentifier() {
        return this.identifier;
    }

    public Object getUserObject() {
        return this.userObject;
    }

    public Layer getLayer() {
        return this.layer;
    }

    public boolean isTerrain() {
        return this.terrainPosition != null;
    }

    public Position getTerrainPosition() {
        return this.terrainPosition;
    }
}
