/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import gov.nasa.worldwind.render.Color;

public class DrawableElements {

    protected int mode;

    protected int count;

    protected int type;

    protected int offset;

    public Color color = new Color();

    public float lineWidth = 1;

    public DrawableElements set(int mode, int count, int type, int offset) {
        this.mode = mode;
        this.count = count;
        this.type = type;
        this.offset = offset;
        return this;
    }
}
