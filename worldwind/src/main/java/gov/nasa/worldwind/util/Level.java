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

    /**
     * Indicates whether this level is the lowest resolution level (level 0) within the parent level set.
     *
     * @return true if this level is the lowest resolution in the parent level set, otherwise false
     */
    public boolean isFirstLevel() {
        return this.levelNumber == 0;
    }

    /**
     * Indicates whether this level is the highest resolution level within the parent level set.
     *
     * @return true if this level is the highest resolution in the parent level set, otherwise false
     */
    public boolean isLastLevel() {
        return this.levelNumber == this.parent.numLevels() - 1;
    }

    /**
     * Returns the level whose ordinal occurs immediately before this level's ordinal in the parent level set, or null
     * if this is the fist level.
     *
     * @return the previous level, or null if this is the first level
     */
    public Level previousLevel() {
        return this.parent.level(this.levelNumber - 1);
    }

    /**
     * Returns the level whose ordinal occurs immediately after this level's ordinal in the parent level set, or null if
     * this is the last level.
     *
     * @return the next level, or null if this is the last level
     */
    public Level nextLevel() {
        return this.parent.level(this.levelNumber + 1);
    }
}
