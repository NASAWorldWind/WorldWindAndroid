/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.Logger;

public class BasicTerrain implements Terrain {

    protected List<TerrainTile> tiles = new ArrayList<>();

    protected Sector sector = new Sector();

    protected short[] triStripElements;

    private Vec3 intersectPoint = new Vec3();

    public BasicTerrain() {
    }

    public void addTile(TerrainTile tile) {
        if (tile == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "addTile", "missingTile"));
        }

        this.tiles.add(tile);
        this.sector.union(tile.sector);
    }

    public void clear() {
        this.tiles.clear();
        this.sector.setEmpty();
        this.triStripElements = null;
    }

    public short[] getTriStripElements() {
        return this.triStripElements;
    }

    public void setTriStripElements(short[] elements) {
        this.triStripElements = elements;
    }

    @Override
    public Sector getSector() {
        return this.sector;
    }

    @Override
    public Vec3 geographicToCartesian(double latitude, double longitude, double altitude, @WorldWind.AltitudeMode int altitudeMode, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "geographicToCartesian", "missingResult"));
        }

        return null; // TODO
    }

    @Override
    public boolean intersect(Line line, Vec3 result) {
        if (line == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "intersect", "missingLine"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "intersect", "missingResult"));
        }

        double minDist2 = Double.POSITIVE_INFINITY;

        for (int idx = 0, len = this.tiles.size(); idx < len; idx++) {
            // Translate the line to the terrain tile's local coordinate system.
            TerrainTile tile = this.tiles.get(idx);
            line.origin.subtract(tile.vertexOrigin);

            // Compute the first intersection of the terrain tile with the line. The line is interpreted as a ray;
            // intersection points behind the line's origin are ignored. Store the nearest intersection found so far
            // in the result argument.
            if (line.triStripIntersection(tile.vertexPoints, 3, this.triStripElements, this.intersectPoint)) {
                double dist2 = line.origin.distanceToSquared(this.intersectPoint);
                if (minDist2 > dist2) {
                    minDist2 = dist2;
                    result.set(this.intersectPoint).add(tile.vertexOrigin);
                }
            }

            // Restore the line's origin to it's previous coordinate system.
            line.origin.add(tile.vertexOrigin);
        }

        return minDist2 != Double.POSITIVE_INFINITY;
    }
}
