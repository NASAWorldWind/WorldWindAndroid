/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.Sector;

/**
 * Multi-resolution, hierarchical collection of tiles organized into levels of increasing resolution. Applications
 * typically do not interact with this class.
 */
public class LevelSet {

    /**
     * The sector spanned by this level set.
     */
    public final Sector sector;

    /**
     * The geographic width and height in degrees of the lowest resolution (level 0) tiles in this level set.
     */
    public final double topLevelDelta;

    /**
     * The width in pixels of images associated with tiles in this level set, or the number of sample points in the
     * longitudinal direction of elevation tiles associated with this level set.
     */
    public final int tileWidth;

    /**
     * The height in pixels of images associated with tiles in this level set, or the number of sample points in the
     * latitudinal direction of elevation tiles associated with this level set.
     */
    public final int tileHeight;

    /**
     * The hierarchical levels, sorted from lowest to highest resolution.
     */
    protected final Level[] levels;

    /**
     * Constructs a level set with specified parameters.
     *
     * @param sector        the sector spanned by this level set
     * @param topLevelDelta the geographic width and height in degrees of tiles in the lowest resolution level of this
     *                      level set
     * @param numLevels     the number of levels in the level set
     * @param tileWidth     the height in pixels of images associated with tiles in this level set, or the number of
     *                      sample points in the longitudinal direction of elevation tiles associate with this level
     *                      set.
     * @param tileHeight    the height in pixels of images associated with tiles in this level set, or the number of
     *                      sample points in the latitudinal direction of elevation tiles associate with this level
     *                      set.
     *
     * @throws IllegalArgumentException If any argument is null, or if any dimension is zero
     */
    public LevelSet(Sector sector, double topLevelDelta, int numLevels, int tileWidth, int tileHeight) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "missingSector"));
        }

        if (topLevelDelta <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "The top level delta is zero"));
        }

        if (numLevels < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "The number of levels is zero"));
        }

        if (tileWidth < 1 || tileHeight < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "The tile width or tile height is zero"));
        }

        this.sector = sector;
        this.topLevelDelta = topLevelDelta;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.levels = new Level[numLevels];


        for (int levelNumber = 0; levelNumber < numLevels; levelNumber++) {
            double n = Math.pow(2, levelNumber);
            double delta = topLevelDelta / n;
            this.levels[levelNumber] = new Level(this, levelNumber, delta);
        }
    }

    /**
     * Returns the number of levels in this level set.
     *
     * @return the number of levels
     */
    public int numLevels() {
        return this.levels.length;
    }

    /**
     * Returns the {@link Level} for a specified level number.
     *
     * @param levelNumber the number of the desired level
     *
     * @return the requested level, or null if the level does not exist.
     */
    public Level level(int levelNumber) {
        if (levelNumber < 0 || levelNumber >= this.levels.length) {
            return null;
        } else {
            return this.levels[levelNumber];
        }
    }

    /**
     * Returns the first (lowest resolution) level of this level set.
     *
     * @return the level of lowest resolution
     */
    public Level firstLevel() {
        return this.levels[0];
    }

    /**
     * Returns the last (highest resolution) level of this level set.
     *
     * @return the level of highest resolution
     */
    public Level lastLevel() {
        return this.levels[this.levels.length - 1];
    }
}
