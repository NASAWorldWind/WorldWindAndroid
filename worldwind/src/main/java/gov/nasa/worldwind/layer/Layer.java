/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import gov.nasa.worldwind.render.DrawContext;

public interface Layer {

    String getDisplayName();

    void setDisplayName(String name);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean isPickEnabled();

    void setPickEnabled(boolean pickEnabled);

    double getOpacity();

    void setOpacity(double opacity);

    double getMinActiveAltitude();

    void setMinActiveAltitude(double minActiveAltitude);

    double getMaxActiveAltitude();

    void setMaxActiveAltitude(double maxActiveAltitude);

    void render(DrawContext dc);

    boolean isWithinActiveAltitudes(DrawContext dc);
}
