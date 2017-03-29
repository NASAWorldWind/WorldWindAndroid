/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Tile;

public interface ElevationCoverage {

    String getDisplayName();

    void setDisplayName(String displayName);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    long getTimestamp();

    Object getUserProperty(Object key);

    Object putUserProperty(Object key, Object value);

    Object removeUserProperty(Object key);

    boolean hasUserProperty(Object key);

    boolean hasCoverage(double latitude, double longitude);

    boolean hasCoverage(Sector sector);

    boolean getHeight(double latitude, double longitude, float[] result);

    boolean getHeightGrid(Sector gridSector, int gridWidth, int gridHeight, double radiansPerPixel, float[] result);

    boolean getHeightLimits(Sector sector, double radiansPerPixel, float[] result);
}
