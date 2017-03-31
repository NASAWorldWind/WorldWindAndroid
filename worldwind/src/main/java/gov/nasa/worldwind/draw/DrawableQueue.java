/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import java.util.Arrays;
import java.util.Comparator;

public class DrawableQueue {

    protected static final int MIN_CAPACITY_INCREMENT = 12;

    protected Entry[] entries = new Entry[0];

    protected int size;

    protected int position;

    /**
     * Sorts drawables by ascending group ID, then ascending order, then by ascending ordinal.
     */
    protected Comparator<Entry> sortComparator = new Comparator<Entry>() {
        @Override
        public int compare(Entry lhs, Entry rhs) {
            if (lhs.groupId < rhs.groupId) { // sort by ascending group ID
                return -1;
            } else if (lhs.groupId > rhs.groupId) {
                return 1;
            } else if (lhs.order < rhs.order) { // sort by ascending order
                return -1;
            } else if (lhs.order > rhs.order) {
                return 1;
            } else { // sort by ascending ordinal
                return lhs.ordinal - rhs.ordinal;
            }
        }
    };

    public DrawableQueue() {
    }

    public int count() {
        return this.size;
    }

    public void offerDrawable(Drawable drawable, int groupId, double depth) {
        if (drawable != null) {
            int capacity = this.entries.length;
            if (capacity == this.size) {
                int increment = Math.max(capacity >> 1, MIN_CAPACITY_INCREMENT);
                Entry[] newEntries = new Entry[capacity + increment];
                System.arraycopy(this.entries, 0, newEntries, 0, capacity);
                this.entries = newEntries;
            }

            if (this.entries[this.size] == null) {
                this.entries[this.size] = new Entry();
            }

            this.entries[this.size].set(drawable, groupId, depth, this.size);
            this.size++;
        }
    }

    public Drawable getDrawable(int index) {
        return (index < this.size) ? this.entries[index].drawable : null;
    }

    public Drawable peekDrawable() {
        return (this.position < this.size) ? this.entries[this.position].drawable : null;
    }

    public Drawable pollDrawable() {
        return (this.position < this.size) ? this.entries[this.position++].drawable : null;
    }

    public void rewindDrawables() {
        this.position = 0;
    }

    public void sortDrawables() {
        Arrays.sort(this.entries, 0, this.size, this.sortComparator);
        this.position = 0;
    }

    public void clearDrawables() {
        for (int idx = 0, len = this.size; idx < len; idx++) {
            this.entries[idx].recycle();
        }

        this.size = 0;
        this.position = 0;
    }

    protected static class Entry {

        public Drawable drawable;

        public int groupId;

        public double order;

        public int ordinal;

        public void set(Drawable drawable, int groupId, double order, int ordinal) {
            this.drawable = drawable;
            this.groupId = groupId;
            this.order = order;
            this.ordinal = ordinal;
        }

        public void recycle() {
            this.drawable.recycle();
            this.drawable = null;
        }
    }
}
