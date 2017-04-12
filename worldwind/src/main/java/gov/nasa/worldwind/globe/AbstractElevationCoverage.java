/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logger;

public abstract class AbstractElevationCoverage implements ElevationCoverage {

    protected String displayName;

    protected boolean enabled = true;

    protected long timestamp;

    protected Map<Object, Object> userProperties;

    public AbstractElevationCoverage() {
        this.updateTimestamp();
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updateTimestamp();
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    protected void updateTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public Object getUserProperty(Object key) {
        return (this.userProperties != null) ? this.userProperties.get(key) : null;
    }

    @Override
    public Object putUserProperty(Object key, Object value) {
        if (this.userProperties == null) {
            this.userProperties = new HashMap<>();
        }

        return this.userProperties.put(key, value);
    }

    @Override
    public Object removeUserProperty(Object key) {
        return (this.userProperties != null) ? this.userProperties.remove(key) : null;
    }

    @Override
    public boolean hasUserProperty(Object key) {
        return (this.userProperties != null) && this.userProperties.containsKey(key);
    }

    @Override
    public void getHeightGrid(Sector gridSector, int gridWidth, int gridHeight, float[] result) {
        if (gridSector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "AbstractElevationCoverage", "getHeightGrid", "missingSector"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "AbstractElevationCoverage", "getHeightGrid", "missingResult"));
        }

        if (!this.isEnabled()) {
            return;
        }

        this.doGetHeightGrid(gridSector, gridWidth, gridHeight, result);
    }

    @Override
    public void getHeightLimits(Sector sector, float[] result) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "AbstractElevationCoverage", "getHeightLimits", "missingSector"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "AbstractElevationCoverage", "getHeightLimits", "missingResult"));
        }

        if (!this.isEnabled()) {
            return;
        }

        this.doGetHeightLimits(sector, result);
    }

    protected abstract void doGetHeightGrid(Sector gridSector, int gridWidth, int gridHeight, float[] result);

    protected abstract void doGetHeightLimits(Sector sector, float[] result);
}
