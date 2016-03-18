/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import java.util.Collection;

import gov.nasa.worldwind.geom.Sector;

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
                Logger.logMessage(Logger.ERROR, "Tile", "assembleTilesForLevel", "The tile factory is null"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Tile", "assembleTilesForLevel", "missingResult"));
        }

        Sector sector = level.parent.sector;
        double tileDelta = level.tileDelta;

        int firstRow = Tile.computeRow(tileDelta, sector.minLatitude);
        int lastRow = Tile.computeLastRow(tileDelta, sector.maxLatitude);
        int firstCol = Tile.computeColumn(tileDelta, sector.minLongitude);
        int lastCol = Tile.computeLastColumn(tileDelta, sector.maxLongitude);

        double firstRowLat = -90 + firstRow * tileDelta;
        double firstRowLon = -180 + firstCol * tileDelta;

        double minLat = firstRowLat;
        double minLon;
        double maxLat;
        double maxLon;

        for (int row = firstRow; row <= lastRow; row++) {
            maxLat = minLat + tileDelta;
            minLon = firstRowLon;

            for (int col = firstCol; col <= lastCol; col++) {
                maxLon = minLon + tileDelta;
                Sector tileSector = new Sector(minLat, maxLat, minLon, maxLon);
                result.add(tileFactory.createTile(tileSector, level, row, col));

                minLon = maxLon;
            }

            minLat = maxLat;
        }

        return result;
    }
}
