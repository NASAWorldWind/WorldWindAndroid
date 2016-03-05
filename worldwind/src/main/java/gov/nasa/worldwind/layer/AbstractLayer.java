/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import gov.nasa.worldwind.render.DrawContext;

public abstract class AbstractLayer implements Layer {

    protected String displayName;

    protected boolean enabled;

    protected boolean pickEnabled;

    protected double opacity;

    protected double minActiveAltitude;

    protected double maxActiveAltitude;

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isPickEnabled() {
        return pickEnabled;
    }

    @Override
    public void setPickEnabled(boolean pickEnabled) {
        this.pickEnabled = pickEnabled;
    }

    @Override
    public double getOpacity() {
        return opacity;
    }

    @Override
    public void setOpacity(double opacity) {
        this.opacity = opacity;
    }

    @Override
    public double getMinActiveAltitude() {
        return minActiveAltitude;
    }

    @Override
    public void setMinActiveAltitude(double minActiveAltitude) {
        this.minActiveAltitude = minActiveAltitude;
    }

    @Override
    public double getMaxActiveAltitude() {
        return maxActiveAltitude;
    }

    @Override
    public void setMaxActiveAltitude(double maxActiveAltitude) {
        this.maxActiveAltitude = maxActiveAltitude;
    }

    @Override
    public void render(DrawContext dc) {
        if (!this.enabled) {
            return;
        }

        if (dc.isPickingMode() && !this.pickEnabled) {
            return;
        }

        if (!this.isWithinActiveAltitudes(dc)) {
            return;
        }

        this.doRender(dc);
    }

    @Override
    public boolean isWithinActiveAltitudes(DrawContext dc) {
        double eyeAltitude = dc.getEyePosition().altitude;
        return eyeAltitude >= this.minActiveAltitude && eyeAltitude <= this.maxActiveAltitude;
    }

    protected abstract void doRender(DrawContext dc);
}
