/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.util.DisplayMetrics;

import java.util.Arrays;
import java.util.Collection;

import gov.nasa.worldwind.geom.BoundingBox;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.RenderContext;

/**
 * Geographically rectangular tile within a {@link LevelSet}, typically representing terrain or imagery. Provides a base
 * class for tiles used by tiled image layers and elevation models. Applications typically do not interact with this
 * class.
 */
public class Tile {

    /**
     * The sector spanned by this tile.
     */
    public final Sector sector;

    /**
     * The level at which this tile lies within a {@link LevelSet}.
     */
    public final Level level;

    /**
     * The tile's row within its level.
     */
    public final int row;

    /**
     * The tile's column within its level.
     */
    public final int column;

    /**
     * A key that uniquely identifies this tile within a level set. Tile keys are not unique to a specific level set.
     */
    public final String tileKey;

    /**
     * A factor expressing the size of a pixel or elevation cell at the center of this tile, in radians per pixel (or
     * cell).
     * <p>
     * Texel size in meters is computed as <code>(tileDelta / tileWidth) * cos(lat) * R</code>, where lat is the
     * centroid latitude and R is the globe's equatorial radius. This is derived by considering that texels are laid out
     * continuously on the arc of constant latitude connecting the tile's east and west edges and passing through its
     * centroid. The radii for the corresponding circle of constant latitude is <code>cos(lat) * R</code>, and the arc
     * length is therefore <code>tileDelta * cos(lat) * R</code>. The size of a texel along this arc is then found by
     * dividing by the number of texels along that arc, defined by the property Level.tileWidth.
     * <p>
     * This property stores the constant part of the texel size computation, <code>(tileDelta / tileWidth) *
     * cos(lat)</code>, leaving the globe-dependant variable <code>R</code> to be incorporated by the globe attached to
     * the RenderContext.
     */
    protected double texelSizeFactor;

    /**
     * The tile's Cartesian bounding box.
     */
    protected BoundingBox extent;

    /**
     * Cartesian points used for determining distance when the {@link gov.nasa.worldwind.geom.Camera} is not above this
     * tile.
     */
    protected float[] samplePoints;

    protected float[] heightLimits;

    protected long heightLimitsTimestamp;

    protected double extentExaggeration;

    protected double distanceToCamera;

    /**
     * Constructs a tile with a specified sector, level, row and column.
     *
     * @param sector the sector spanned by the tile
     * @param level  the tile's level in a {@link LevelSet}
     * @param row    the tile's row within the specified level
     * @param column the tile's column within the specified level
     *
     * @throws IllegalArgumentException if either the sector or the level is null
     */
    public Tile(Sector sector, Level level, int row, int column) {
        if (sector == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Tile", "constructor", "missingSector"));
        }

        if (level == null) {
            throw new IllegalArgumentException(Logger.logMessage(Logger.ERROR, "Tile", "constructor", "missingLevel"));
        }

        this.sector = sector;
        this.level = level;
        this.row = row;
        this.column = column;
        this.tileKey = level.levelNumber + "." + row + "." + column;
        this.texelSizeFactor = Math.toRadians(level.tileDelta / level.tileWidth) * Math.cos(Math.toRadians(sector.centroidLatitude()));
    }

    /**
     * Computes a row number for a tile within a level given the tile's latitude.
     *
     * @param tileDelta the level's tile delta in degrees
     * @param latitude  the tile's minimum latitude in degrees
     *
     * @return the computed row number
     */
    public static int computeRow(double tileDelta, double latitude) {
        int row = (int) Math.floor((latitude + 90) / tileDelta);

        if (latitude == 90) {
            row -= 1; // if latitude is at the end of the grid, subtract 1 from the computed row to return the last row
        }

        return row;
    }

    /**
     * Computes a column number for a tile within a level given the tile's longitude.
     *
     * @param tileDelta the level's tile delta in degrees
     * @param longitude the tile's minimum longitude in degrees
     *
     * @return The computed column number
     */
    public static int computeColumn(double tileDelta, double longitude) {
        int col = (int) Math.floor((longitude + 180) / tileDelta);

        if (longitude == 180) {
            col -= 1; // if longitude is at the end of the grid, subtract 1 from the computed column to return the last column
        }

        return col;
    }

    /**
     * Computes the last row number for a tile within a level given the tile's maximum latitude.
     *
     * @param tileDelta   the level's tile delta in degrees
     * @param maxLatitude the tile's maximum latitude in degrees
     *
     * @return the computed row number
     */
    public static int computeLastRow(double tileDelta, double maxLatitude) {
        int row = (int) Math.ceil((maxLatitude + 90) / tileDelta - 1);

        if (maxLatitude + 90 < tileDelta) {
            row = 0; // if max latitude is in the first row, set the max row to 0
        }

        return row;
    }

    /**
     * Computes the last column number for a tile within a level given the tile's maximum longitude.
     *
     * @param tileDelta    the level's tile delta in degrees
     * @param maxLongitude the tile's maximum longitude in degrees
     *
     * @return The computed column number
     */
    public static int computeLastColumn(double tileDelta, double maxLongitude) {
        int col = (int) Math.ceil((maxLongitude + 180) / tileDelta - 1);

        if (maxLongitude + 180 < tileDelta) {
            col = 0; // if max longitude is in the first column, set the max column to 0
        }

        return col;
    }

    /**
     * Creates all tiles for a specified level within a {@link LevelSet}.
     *
     * @param level       the level to create the tiles for
     * @param tileFactory the tile factory to use for creating tiles.
     * @param result      an pre-allocated Collection in which to store the results
     *
     * @return the result argument populated with the tiles for the specified level
     *
     * @throws IllegalArgumentException If any argument is null
     */
    public static Collection<Tile> assembleTilesForLevel(Level level, TileFactory tileFactory, Collection<Tile> result) {
        if (level == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "assembleTilesForLevel", "missingLevel"));
        }

        if (tileFactory == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "assembleTilesForLevel", "missingTileFactory"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "assembleTilesForLevel", "missingResult"));
        }

        Sector sector = level.parent.sector;
        double tileDelta = level.tileDelta;

        int firstRow = Tile.computeRow(tileDelta, sector.minLatitude());
        int lastRow = Tile.computeLastRow(tileDelta, sector.maxLatitude());
        int firstCol = Tile.computeColumn(tileDelta, sector.minLongitude());
        int lastCol = Tile.computeLastColumn(tileDelta, sector.maxLongitude());

        double firstRowLat = -90 + firstRow * tileDelta;
        double firstRowLon = -180 + firstCol * tileDelta;
        double lat = firstRowLat;
        double lon;

        for (int row = firstRow; row <= lastRow; row++) {
            lon = firstRowLon;

            for (int col = firstCol; col <= lastCol; col++) {
                Sector tileSector = new Sector(lat, lon, tileDelta, tileDelta);
                result.add(tileFactory.createTile(tileSector, level, row, col));

                lon += tileDelta;
            }

            lat += tileDelta;
        }

        return result;
    }

    /**
     * Indicates whether this tile's Cartesian extent intersects a specified frustum.
     *
     * @param rc      the current render context
     * @param frustum the frustum of interest
     *
     * @return true if the specified frustum intersects this tile's extent, otherwise false
     *
     * @throws IllegalArgumentException If the frustum is null
     */
    public boolean intersectsFrustum(RenderContext rc, Frustum frustum) {
        if (frustum == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "intersectsFrustum", "missingFrustum"));
        }

        return this.getExtent(rc).intersectsFrustum(frustum);
    }

    /**
     * Indicates whether this tile intersects a specified sector.
     *
     * @param sector the sector of interest
     *
     * @return true if the specified sector intersects this tile's sector, otherwise false
     *
     * @throws IllegalArgumentException If the sector is null
     */
    public boolean intersectsSector(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "intersectsSector", "missingSector"));
        }

        return this.sector.intersects(sector);
    }

    /**
     * Indicates whether this tile should be subdivided based on the current navigation state and a specified detail
     * factor.
     *
     * @param rc           the current render context
     * @param detailFactor the detail factor to consider
     *
     * @return true if the tile should be subdivided, otherwise false
     */
    public boolean mustSubdivide(RenderContext rc, double detailFactor) {
        this.distanceToCamera = this.distanceToCamera(rc);
        double texelSize = this.texelSizeFactor * rc.globe.getEquatorialRadius();
        double pixelSize = rc.pixelSizeAtDistance(this.distanceToCamera);
        double densityFactor = 1.0;

        // Adjust the subdivision factory when the display density is low. Values of detailFactor have been calibrated
        // against high density devices. Low density devices need roughly half the detailFactor.
        if (rc.resources.getDisplayMetrics().densityDpi <= DisplayMetrics.DENSITY_MEDIUM) {
            densityFactor = 0.5;
        }

        return texelSize > (pixelSize * detailFactor * densityFactor);
    }

    /**
     * Returns the four children formed by subdividing this tile. This tile's sector is subdivided into four quadrants
     * as follows: Southwest; Southeast; Northwest; Northeast. A new tile is then constructed for each quadrant and
     * configured with the next level within this tile's LevelSet and its corresponding row and column within that
     * level. This returns null if this tile's level is the last level within its {@link LevelSet}.
     *
     * @param tileFactory the tile factory to use to create the children
     *
     * @return an array containing the four child tiles, or null if this tile's level is the last level
     *
     * @throws IllegalArgumentException If the tile factory is null
     */
    public Tile[] subdivide(TileFactory tileFactory) {
        if (tileFactory == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "subdivide", "missingTileFactory"));
        }

        Level childLevel = this.level.nextLevel();
        if (childLevel == null) {
            return null;
        }

        Tile[] children = new Tile[4];
        double latMin = this.sector.minLatitude();
        double lonMin = this.sector.minLongitude();
        double latMid = this.sector.centroidLatitude();
        double lonMid = this.sector.centroidLongitude();
        double childDelta = this.level.tileDelta * 0.5;

        int childRow = 2 * this.row;
        int childCol = 2 * this.column;
        Sector childSector = new Sector(latMin, lonMin, childDelta, childDelta);
        children[0] = tileFactory.createTile(childSector, childLevel, childRow, childCol); // Southwest

        childRow = 2 * this.row;
        childCol = 2 * this.column + 1;
        childSector = new Sector(latMin, lonMid, childDelta, childDelta);
        children[1] = tileFactory.createTile(childSector, childLevel, childRow, childCol); // Southeast

        childRow = 2 * this.row + 1;
        childCol = 2 * this.column;
        childSector = new Sector(latMid, lonMin, childDelta, childDelta);
        children[2] = tileFactory.createTile(childSector, childLevel, childRow, childCol); // Northwest

        childRow = 2 * this.row + 1;
        childCol = 2 * this.column + 1;
        childSector = new Sector(latMid, lonMid, childDelta, childDelta);
        children[3] = tileFactory.createTile(childSector, childLevel, childRow, childCol); // Northeast

        return children;
    }

    /**
     * Returns the four children formed by subdividing this tile, drawing those children from a specified cache. The
     * cache is checked for a child collection prior to subdividing. If one exists in the cache it is returned rather
     * than creating a new collection of children. If a new collection is created in the same manner as {@link
     * #subdivide(TileFactory)} and added to the cache.
     *
     * @param tileFactory the tile factory to use to create the children
     * @param cache       a memory cache that may contain pre-existing child tiles.
     * @param cacheSize   the cached size of the four child tiles
     *
     * @return an array containing the four child tiles, or null if this tile's level is the last level
     *
     * @throws IllegalArgumentException If any argument is null
     */
    public Tile[] subdivideToCache(TileFactory tileFactory, LruMemoryCache<String, Tile[]> cache, int cacheSize) {
        if (tileFactory == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "subdivideToCache", "missingTileFactory"));
        }

        if (cache == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "subdivideToCache", "missingCache"));
        }

        Tile[] children = cache.get(this.tileKey);
        if (children == null) {
            children = this.subdivide(tileFactory);
            if (children != null) {
                cache.put(this.tileKey, children, cacheSize);
            }
        }

        return children;
    }

    protected BoundingBox getExtent(RenderContext rc) {
        if (this.heightLimits == null) {
            this.heightLimits = new float[2];
        }

        if (this.extent == null) {
            this.extent = new BoundingBox();
        }

        long elevationTimestamp = rc.globe.getElevationModel().getTimestamp();
        if (elevationTimestamp != this.heightLimitsTimestamp) {
            Arrays.fill(this.heightLimits, 0);
            rc.globe.getElevationModel().getHeightLimits(this.sector, this.heightLimits);
        }

        double verticalExaggeration = rc.verticalExaggeration;
        if (verticalExaggeration != this.extentExaggeration ||
            elevationTimestamp != this.heightLimitsTimestamp) {
            float minHeight = (float) (this.heightLimits[0] * verticalExaggeration);
            float maxHeight = (float) (this.heightLimits[1] * verticalExaggeration);
            this.extent.setToSector(this.sector, rc.globe, minHeight, maxHeight);
        }

        this.heightLimitsTimestamp = elevationTimestamp;
        this.extentExaggeration = verticalExaggeration;

        return this.extent;
    }

    /**
     * Calculates the distance from this tile to the camera point associated with the specified render context.
     *
     * @param rc the render context which provides the current camera point
     *
     * @return the distance in meters
     */
    protected double distanceToCamera(RenderContext rc) {
        if (this.sector.contains(rc.camera.latitude, rc.camera.longitude)) {
            return rc.camera.altitude;
        }

        if (this.samplePoints == null) {
            this.samplePoints = rc.globe.geographicToCartesianGrid(this.sector, 3, 3, null, 1.0f, null, new float[27], 0, 0);
        }

        double minDistanceSq = Double.MAX_VALUE;
        for (int i = 0, len = this.samplePoints.length; i < len; i += 3) {
            double dx = rc.cameraPoint.x - this.samplePoints[i];
            double dy = rc.cameraPoint.y - this.samplePoints[i + 1];
            double dz = rc.cameraPoint.z - this.samplePoints[i + 2];
            double distanceSq = (dx * dx) + (dy * dy) + (dz * dz);
            if (minDistanceSq > distanceSq) {
                minDistanceSq = distanceSq;
            }
        }

        return Math.sqrt(minDistanceSq);
    }
}
