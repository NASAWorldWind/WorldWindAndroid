/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

/**
 * Factory for delegating construction of URLs associated with tiles within a {@link LevelSet}.
 */
public interface TileUrlFactory {

    /**
     * Constructs the URL string associated with a specified tile and image format.
     *
     * @param tile        the tile for which to create the URL
     * @param imageFormat an optional image format used to create the URL, may be null in which case a default image
     *                    format is used
     *
     * @return the URL string for the specified tile and image format
     *
     * @throws IllegalArgumentException if the tile is null
     */
    String urlForTile(Tile tile, String imageFormat);
}
