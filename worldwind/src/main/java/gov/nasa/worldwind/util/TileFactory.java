/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.Sector;

/**
 * Factory for delegating construction of {@link Tile} instances.
 */
public interface TileFactory {

    /**
     * Returns a tile for a specified sector, level within a {@link LevelSet}, and row and column within that level.
     *
     * @param sector the sector spanned by the tile
     * @param level  the level at which the tile lies within a LevelSet
     * @param row    the row within the specified level
     * @param column the column within the specified level
     *
     * @return a tile constructed with the specified arguments
     *
     * @throws IllegalArgumentException if either the sector or the level is null
     */
    Tile createTile(Sector sector, Level level, int row, int column);
}
