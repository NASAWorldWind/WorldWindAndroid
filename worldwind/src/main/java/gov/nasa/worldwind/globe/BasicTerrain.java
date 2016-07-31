/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.Logger;

public class BasicTerrain implements Terrain {

    protected Globe globe;

    protected double verticalExaggeration = 1;

    protected Sector sector = new Sector();

    protected List<TerrainTile> tiles = new ArrayList<>();

    protected short[] triStripElements;

    private Vec3 intersectPoint = new Vec3();

    private Vec3 normal = new Vec3();

    public BasicTerrain() {
    }

    public Globe getGlobe() {
        return this.globe;
    }

    public void setGlobe(Globe globe) {
        this.globe = globe;
    }

    @Override
    public double getVerticalExaggeration() {
        return this.verticalExaggeration;
    }

    public void setVerticalExaggeration(double verticalExaggeration) {
        this.verticalExaggeration = verticalExaggeration;
    }

    @Override
    public Sector getSector() {
        return this.sector;
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
        this.globe = null;
        this.verticalExaggeration = 1;
        this.sector.setEmpty();
        this.tiles.clear();
        this.triStripElements = null;
    }

    public short[] getTriStripElements() {
        return this.triStripElements;
    }

    public void setTriStripElements(short[] elements) {
        this.triStripElements = elements;
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
            if (line.triStripIntersection(tile.vertexPoints, 3, this.triStripElements, this.triStripElements.length, this.intersectPoint)) {
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

    @Override
    public boolean surfacePoint(double latitude, double longitude, double offset, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "surfacePoint", "missingResult"));
        }

        for (int idx = 0, len = this.tiles.size(); idx < len; idx++) {
            TerrainTile tile = this.tiles.get(idx);
            Sector sector = tile.sector;

            // Find the first tile that contains the specified location.
            if (sector.contains(latitude, longitude)) {
                // Compute the location's parameterized coordinates (s, t) within the tile grid, along with the
                // fractional component (sf, tf) and integral component (si, ti).
                int tileWidth = tile.level.tileWidth;
                int tileHeight = tile.level.tileHeight;
                double s = (longitude - sector.minLongitude()) / sector.deltaLongitude() * (tileWidth - 1);
                double t = (latitude - sector.minLatitude()) / sector.deltaLatitude() * (tileHeight - 1);
                double sf = (s < tileWidth - 1) ? (s - (int) s) : 1;
                double tf = (t < tileHeight - 1) ? (t - (int) t) : 1;
                int si = (s < tileWidth - 1) ? (int) s : (tileWidth - 2);
                int ti = (t < tileHeight - 1) ? (int) t : (tileHeight - 2);

                // Compute the location in the tile's local coordinate system. Perform a bilinear interpolation of
                // the cell's four points based on the fractional portion of the location's parameterized coordinates.
                // Tile coordinates are organized in the vertexPoints array in row major order, starting at the tile's
                // Southwest corner.
                int i00 = (si + ti * tileWidth) * 3;       // lower left coordinate
                int i10 = i00 + 3;                         // lower right coordinate
                int i01 = (si + (ti + 1) * tileWidth) * 3; // upper left coordinate
                int i11 = i01 + 3;                         // upper right coordinate
                double f00 = (1 - sf) * (1 - tf);
                double f10 = sf * (1 - tf);
                double f01 = (1 - sf) * tf;
                double f11 = sf * tf;
                float[] points = tile.vertexPoints;
                result.x = (points[i00] * f00) + (points[i10] * f10) + (points[i01] * f01) + (points[i11] * f11);
                result.y = (points[i00 + 1] * f00) + (points[i10 + 1] * f10) + (points[i01 + 1] * f01) + (points[i11 + 1] * f11);
                result.z = (points[i00 + 2] * f00) + (points[i10 + 2] * f10) + (points[i01 + 2] * f01) + (points[i11 + 2] * f11);

                // Translate the point along a the vector 'offset' meters relative to the tile's surface.
                if (offset != 0) {
                    this.globe.geographicToCartesianNormal(latitude, longitude, this.normal);
                    result.x += this.normal.x * offset;
                    result.y += this.normal.y * offset;
                    result.z += this.normal.z * offset;
                }

                // Translate the surface point from the tile's local coordinate system to Cartesian coordinates.
                result.x += tile.vertexOrigin.x;
                result.y += tile.vertexOrigin.y;
                result.z += tile.vertexOrigin.z;

                return true;
            }
        }

        // No tile was found that contains the location.
        return false;
    }
}
