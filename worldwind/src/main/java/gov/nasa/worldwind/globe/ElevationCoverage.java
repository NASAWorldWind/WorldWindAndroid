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

    boolean hasCoverage(Sector sector);

    float getHeight(double latitude, double longitude);

    float[] getHeight(Tile tile, float[] result);

    float[] getHeightLimits(Tile tile, float[] result);
}
