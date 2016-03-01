/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

/**
 * Path type indicates how World Wind create a geographic path between two locations.
 */
public enum PathType {
    /**
     * Indicates a great circle arc between two locations.
     */
    GREAT_CIRCLE,
    /**
     * Indicates simple linear interpolation between two locations.
     */
    LINEAR,
    /**
     * Indicates a line of constant bearing between two locations.
     */
    RHUMB_LINE
}
