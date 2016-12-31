/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.Sector;

/**
 * Configuration values for a multi-resolution, hierarchical collection of tiles organized into levels of increasing
 * resolution.
 */
public class LevelSetConfig {

    /**
     * The sector spanned by the level set.
     */
    public final Sector sector = new Sector().setFullSphere();

    /**
     * The geographic width and height in degrees of tiles in the first level (lowest resolution) of the level set.
     */
    public double firstLevelDelta = 90;

    /**
     * The number of levels in the level set.
     */
    public int numLevels = 1;

    /**
     * The width in pixels of images associated with tiles in the level set, or the number of sample points in the
     * longitudinal direction of elevation tiles associated with the level set.
     */
    public int tileWidth = 256;

    /**
     * The height in pixels of images associated with tiles in the level set, or the number of sample points in the
     * latitudinal direction of elevation tiles associated with the level set.
     */
    public int tileHeight = 256;

    /**
     * Constructs a level set configuration with default values. <ul> <li>sector = -90 to +90 latitude and -180 to +180
     * longitude</li> <li>firstLevelDelta = 90 degrees</li> <li>numLevels = 1</li> <li>tileWidth = 256</li>
     * <li>tileHeight = 256</li> </ul>
     */
    public LevelSetConfig() {
    }

    /**
     * Constructs a level set configuration with specified parameters.
     *
     * @param sector          the sector spanned by the level set, may be null in which case the sector spanning the
     *                        full sphere is used
     * @param firstLevelDelta the geographic width and height in degrees of tiles in the first level (lowest resolution)
     *                        of the level set
     * @param numLevels       the number of levels in the level set
     * @param tileWidth       the height in pixels of images associated with tiles in the level set, or the number of
     *                        sample points in the longitudinal direction of elevation tiles associate with the level
     *                        set
     * @param tileHeight      the height in pixels of images associated with tiles in the level set, or the number of
     *                        sample points in the latitudinal direction of elevation tiles associate with the level
     *                        set
     */
    public LevelSetConfig(Sector sector, double firstLevelDelta, int numLevels, int tileWidth, int tileHeight) {
        if (sector != null) {
            this.sector.set(sector);
        }

        this.firstLevelDelta = firstLevelDelta;
        this.numLevels = numLevels;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    /**
     * Returns the number of levels necessary to achieve the specified resolution. The result is correct for this
     * configuration's current firstLevelDelta, tileWidth and tileHeight, and is invalid if any of these values change.
     *
     * @param radiansPerPixel the desired resolution in radians per pixel
     *
     * @return the number of levels
     *
     * @throws IllegalArgumentException If the resolution is not positive
     */
    public int numLevelsForResolution(double radiansPerPixel) {
        if (radiansPerPixel <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSetConfig", "numLevelsForResolution", "invalidResolution"));
        }

        double degreesPerPixel = Math.toDegrees(radiansPerPixel);
        double firstLevelDegreesPerPixel = this.firstLevelDelta / Math.min(this.tileWidth, this.tileHeight);
        double level = Math.log(firstLevelDegreesPerPixel / degreesPerPixel) / Math.log(2); // fractional level address
        int levelNumber = (int) Math.ceil(level); // ceiling captures the resolution

        if (levelNumber < 0) {
            levelNumber = 0; // need at least one level, even if it exceeds the desired resolution
        }

        return levelNumber + 1; // convert level number to level count
    }

    /**
     * Returns the number of levels closest to the specified resolution, but does not exceed it. May be used to
     * configure level sets where a not to exceed resolution is mandated. The result is correct for this configuration's
     * current firstLevelDelta, tileWidth and tileHeight, and is invalid if any of these values change.
     *
     * @param radiansPerPixel the desired not to exceed resolution in radians per pixel
     *
     * @return the number of levels
     *
     * @throws IllegalArgumentException If the resolution is not positive
     */
    public int numLevelsForMinResolution(double radiansPerPixel) {
        if (radiansPerPixel <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LevelSetConfig", "numLevelsForMinResolution", "invalidResolution"));
        }

        double degreesPerPixel = Math.toDegrees(radiansPerPixel);
        double firstLevelDegreesPerPixel = this.firstLevelDelta / this.tileHeight;
        double level = Math.log(firstLevelDegreesPerPixel / degreesPerPixel) / Math.log(2); // fractional level address
        int levelNumber = (int) Math.floor(level); // floor prevents exceeding the min scale

        if (levelNumber < 0) {
            levelNumber = 0; // need at least one level, even if it exceeds the desired resolution
        }

        return levelNumber + 1; // convert level number to level count
    }
}
