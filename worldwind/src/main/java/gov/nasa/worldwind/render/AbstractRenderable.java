/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRenderable implements Renderable {

    protected String displayName;

    protected boolean enabled = true;

    protected Object pickDelegate;

    protected Map<Object, Object> userProperties;

    public AbstractRenderable() {
    }

    public AbstractRenderable(String displayName) {
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
    public Object getPickDelegate() {
        return pickDelegate;
    }

    @Override
    public void setPickDelegate(Object pickDelegate) {
        this.pickDelegate = pickDelegate;
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

        this.doRender(rc);
    }

    protected abstract void doRender(RenderContext rc);
}
