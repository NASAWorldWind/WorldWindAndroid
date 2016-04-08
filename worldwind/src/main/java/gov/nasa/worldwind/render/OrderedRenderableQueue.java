/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OrderedRenderableQueue {

    protected ArrayList<Entry> renderables;

    protected boolean mustSortRenderables;

    private int last = -1;

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

    /**
     * Constructs a OrderedRenderableQueue with the default capacity of the underlying container.
     */
    public OrderedRenderableQueue() {
        this.renderables = new ArrayList<>();
    }

    /**
     * Constructs a OrderedRenderableQueue with the specified initial for the queue. Larger values prevent reallocations
     * as renderables are added to the queue.
     *
     * @param initialCapacity The initial capacity of the queue.
     */
    public OrderedRenderableQueue(int initialCapacity) {
        this.renderables = new ArrayList<>(initialCapacity);
    }

    public void offerRenderable(OrderedRenderable renderable, double depth) {
        if (renderable != null) { // ignore null arguments
            this.renderables.add(new Entry(renderable, depth, ++this.last));
            this.mustSortRenderables = true;
        }
    }

    public OrderedRenderable peekRenderable() {
        this.sortIfNeeded();
        return (this.last < 0) ? null : this.renderables.get(this.last).or;
    }

    public OrderedRenderable pollRenderable() {
        this.sortIfNeeded();

        if (this.last < 0) {
            return null;
        }

        OrderedRenderable or = this.renderables.get(this.last).or;
        this.renderables.set(this.last--, null); // prevent memory leak
        return or;
    }

    public void clearRenderables() {
        this.renderables.clear();
        this.mustSortRenderables = false;
        this.last = -1;
    }

    protected void sortIfNeeded() {
        if (this.mustSortRenderables) {
            // We may need to trim the array to remove null references if we were polled since the last sort.
            // After polling, the "last" index is before last array element.
            while (this.renderables.size() - 1 > this.last) {
                this.renderables.remove(this.last + 1); // remove our null entries
            }
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
