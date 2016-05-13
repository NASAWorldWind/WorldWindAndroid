/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

public interface Renderable {

    String getDisplayName();

    void setDisplayName(String displayName);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    Object getPickDelegate();

    void setPickDelegate(Object pickDelegate);

    Object getUserProperty(Object key);

    Object putUserProperty(Object key, Object value);

    Object removeUserProperty(Object key);

    boolean hasUserProperty(Object key);

    void render(RenderContext rc);
}
