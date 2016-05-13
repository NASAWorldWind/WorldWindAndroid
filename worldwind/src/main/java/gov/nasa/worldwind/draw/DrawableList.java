/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import java.util.ArrayList;

public class DrawableList {

    protected ArrayList<Drawable> drawables = new ArrayList<>();

    public DrawableList() {
    }

    public int count() {
        return this.drawables.size();
    }

    public void offerDrawable(Drawable drawable) {
        if (drawable != null) {
            this.drawables.add(drawable);
        }
    }

    public Drawable getDrawable(int index) {
        return (index < this.drawables.size()) ? this.drawables.get(index) : null;
    }

    public void clearDrawables() {
        for (int idx = 0, len = this.drawables.size(); idx < len; idx++) {
            this.drawables.get(idx).recycle();
        }

        this.drawables.clear();
    }
}
