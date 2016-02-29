/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

public interface Renderable {

    String getDisplayName();

    void setDisplayName(String name);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    Object getPickDelegate();

    void setPickDelegate(Object pickDelegate);

    void render(DrawContext dc);
}
