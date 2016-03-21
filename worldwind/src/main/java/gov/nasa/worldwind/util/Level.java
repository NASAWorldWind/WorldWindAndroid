/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

/**
 * Represents a level of a specific resolution in a {@link LevelSet}.
 */
public class Level {

    /**
     * The LevelSet that this level is a member of.
     */
    public final LevelSet parent;

    /**
     * The level's ordinal in its parent level set.
     */
    public final int levelNumber;

    /**
     * The geographic width and height in degrees of tiles within this level.
     */
    public final double tileDelta;

    /**
     * The parent LevelSet's tileWidth.
     */
    public final int tileWidth;

    /**
     * The parent LevelSet's tileHeight.
     */
    public final int tileHeight;

    /**
     * The size of pixels or elevation cells within this level, in radians per pixel (or per cell).
     */
    public final double texelHeight;

    /**
     * Constructs a Level within a {@link LevelSet}. Applications typically do not interact with this class.
     *
     * @param parent      the level set that this level is a member of
     * @param levelNumber the level's ordinal within its parent level set
     * @param tileDelta   the geographic width and height in degrees of tiles within this level
     */
    public Level(LevelSet parent, int levelNumber, double tileDelta) {
        if (parent == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Level", "constructor", "The parent level set is null"));
        }

        if (tileDelta <= 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Level", "constructor", "The tile delta is zero"));
        }

        this.parent = parent;
        this.levelNumber = levelNumber;
        this.tileDelta = tileDelta;
        this.tileWidth = parent.tileWidth;
        this.tileHeight = parent.tileHeight;
        this.texelHeight = Math.toRadians(tileDelta) / parent.tileHeight;
    }
}
