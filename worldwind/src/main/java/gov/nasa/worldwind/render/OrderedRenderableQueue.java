/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import gov.nasa.worldwind.util.Logger;

public class OrderedRenderableQueue {

    protected ArrayList<Entry> renderables = new ArrayList<>();

    protected boolean mustSortRenderables;

    /**
     * Sorts ordered renderables by depth from front to back and then by insertion time. The peek and poll methods
     * access the back of the ordered renderable list, thereby causing ordered renderables to be processed from back to
     * front.
     */
    protected Comparator<Entry> frontToBackComparator = new Comparator<Entry>() {
        @Override
        public int compare(Entry lhs, Entry rhs) {
            if (lhs.eyeDistance < rhs.eyeDistance) { // lhs is closer than rhs; sort lhs before rhs
                return -1;
            } else if (lhs.eyeDistance > rhs.eyeDistance) { // lhs is farther than rhs; sort rhs before lhs
                return 1;
            } else { // lhs and rhs have the same depth; sort by inverse insertion order (newest one is polled first)
                return (int) (rhs.ordinal - lhs.ordinal);
            }
        }
    };

    public void offerRenderable(OrderedRenderable renderable, double depth) {
        if (renderable != null) { // ignore null arguments
            this.renderables.add(new Entry(renderable, depth, this.renderables.size()));
            this.mustSortRenderables = true;
        }
    }

    public OrderedRenderable peekRenderable() {
        this.sortIfNeeded();
        int last = this.renderables.size() - 1;
        return (last < 0) ? null : this.renderables.get(last).or;
    }

    public OrderedRenderable pollRenderable() {
        this.sortIfNeeded();
        int last = this.renderables.size() - 1;
        return (last < 0) ? null : this.renderables.remove(last).or;
    }

    public void clearRenderables() {
        this.renderables.clear();
        this.mustSortRenderables = false;
    }

    protected void sortIfNeeded() {
        if (this.mustSortRenderables) {
            Collections.sort(this.renderables, this.frontToBackComparator);
            this.mustSortRenderables = false;
        }
    }

    protected static class Entry {

        public OrderedRenderable or;

        public double eyeDistance;

        public long ordinal;

        public Entry(OrderedRenderable or, double eyeDistance, long ordinal) {
            this.or = or;
            this.eyeDistance = eyeDistance;
            this.ordinal = ordinal;
        }
    }
}
