/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import java.util.HashMap;
import java.util.Map;

import gov.nasa.worldwind.render.RenderContext;

public abstract class AbstractLayer implements Layer {

    protected String displayName;

    protected boolean enabled = true;

    protected boolean pickEnabled = true;

    protected double opacity = 1;

    protected double minActiveAltitude = Double.NEGATIVE_INFINITY;

    protected double maxActiveAltitude = Double.POSITIVE_INFINITY;

    protected Map<Object, Object> userProperties;

    public AbstractLayer() {
    }

    public AbstractLayer(String displayName) {
        this.displayName = displayName;
    }

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
    public void render(RenderContext rc) {
        if (!this.enabled) {
            return;
        }

        if (!this.pickEnabled && rc.pickMode) {
            return;
        }

        if (!this.isWithinActiveAltitudes(rc)) {
            return;
        }

        this.doRender(rc);
    }

    @Override
    public boolean isWithinActiveAltitudes(RenderContext rc) {
        double cameraAltitude = rc.camera.altitude;
        return cameraAltitude >= this.minActiveAltitude && cameraAltitude <= this.maxActiveAltitude;
    }

    protected abstract void doRender(RenderContext rc);
}
