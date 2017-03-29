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
    public final Sector sector = new Sector();

    /**
     * The geographic width and height in degrees of tiles in the first level (lowest resolution) of this level set.
     */
    public final double firstLevelDelta;

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
     * Constructs an empty level set with no levels. The methods <code>level</code>, <code>levelForResolution</code>,
     * <code>firstLevel</code> and <code>lastLevel</code> always return null.
     */
    public LevelSet() {
        this.firstLevelDelta = 0;
        this.tileWidth = 0;
        this.tileHeight = 0;
        this.levels = new Level[0];
    }

    /**
     * Constructs a level set with specified parameters.
     *
     * @param sector          the sector spanned by this level set
     * @param firstLevelDelta the geographic width and height in degrees of tiles in the first level (lowest resolution)
     *                        of the level set
     * @param numLevels       the number of levels in the level set
     * @param tileWidth       the height in pixels of images associated with tiles in this level set, or the number of
     *                        sample points in the longitudinal direction of elevation tiles associate with this level
     *                        set
     * @param tileHeight      the height in pixels of images associated with tiles in this level set, or the number of
     *                        sample points in the latitudinal direction of elevation tiles associate with this level
     *                        set
     *
     * @throws IllegalArgumentException If any argument is null, or if any dimension is zero
     */
    public LevelSet(Sector sector, double firstLevelDelta, int numLevels, int tileWidth, int tileHeight) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "missingSector"));
        }

        if (firstLevelDelta <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidTileDelta"));
        }

        if (numLevels < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidNumLevels"));
        }

        if (tileWidth < 1 || tileHeight < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidWidthOrHeight"));
        }

        this.sector.set(sector);
        this.firstLevelDelta = firstLevelDelta;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.levels = new Level[numLevels];
        this.assembleLevels();
    }

    /**
     * Constructs a level set with parameters from a specified configuration. The configuration's sector must be
     * non-null, its first level delta must be positive, its number of levels must be 1 or more, and its tile width and
     * tile height must be 1 or greater.
     *
     * @param config the configuration for this level set
     *
     * @throws IllegalArgumentException If the configuration is null, or if any configuration value is invalid
     */
    public LevelSet(LevelSetConfig config) {
        if (config == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "missingConfig"));
        }

        if (config.sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "missingSector"));
        }

        if (config.firstLevelDelta <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidTileDelta"));
        }

        if (config.numLevels < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidNumLevels"));
        }

        if (config.tileWidth < 1 || config.tileHeight < 1) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSet", "constructor", "invalidWidthOrHeight"));
        }

        this.sector.set(config.sector);
        this.firstLevelDelta = config.firstLevelDelta;
        this.tileWidth = config.tileWidth;
        this.tileHeight = config.tileHeight;
        this.levels = new Level[config.numLevels];
        this.assembleLevels();
    }

    protected void assembleLevels() {
        for (int i = 0, len = this.levels.length; i < len; i++) {
            double n = Math.pow(2, i);
            double delta = firstLevelDelta / n;
            this.levels[i] = new Level(this, i, delta);
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
     * @return the requested level, or null if the level does not exist
     */
    public Level level(int levelNumber) {
        if (levelNumber < 0 || levelNumber >= this.levels.length) {
            return null;
        } else {
            return this.levels[levelNumber];
        }
    }

    /**
     * Returns the level that most closely approximates the specified resolution.
     *
     * @param radiansPerPixel the desired resolution in radians per pixel
     *
     * @return the level for the specified resolution, or null if this level set is empty
     *
     * @throws IllegalArgumentException If the resolution is not positive
     */
    public Level levelForResolution(double radiansPerPixel) {
        if (radiansPerPixel <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSetConfig", "levelForResolution", "invalidResolution"));
        }

        if (this.levels.length == 0) {
            return null; // this level set is empty
        }

        double degreesPerPixel = Math.toDegrees(radiansPerPixel);
        double firstLevelDegreesPerPixel = this.firstLevelDelta / Math.min(this.tileWidth, this.tileHeight);
        double level = Math.log(firstLevelDegreesPerPixel / degreesPerPixel) / Math.log(2); // fractional level address
        int levelNumber = (int) Math.round(level); // nearest neighbor level

        if (levelNumber < 0) {
            return this.levels[0]; // unable to match the resolution; return the first level
        } else if (levelNumber < this.levels.length) {
            return this.levels[levelNumber]; // nearest neighbor level is in this level set
        } else {
            return this.levels[this.levels.length - 1]; // unable to match the resolution; return the last level
        }
    }

    /**
     * Returns the first level (lowest resolution) of this level set.
     *
     * @return the level of lowest resolution, or null if this level set is empty
     */
    public Level firstLevel() {
        return this.levels.length > 0 ? this.levels[0] : null;
    }

    /**
     * Returns the last level (highest resolution) of this level set.
     *
     * @return the level of highest resolution, or null if this level set is empty
     */
    public Level lastLevel() {
        return this.levels.length > 0 ? this.levels[this.levels.length - 1] : null;
    }
}
