/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.draw;

import java.util.Arrays;
import java.util.Comparator;

public class DrawableQueue {

    protected Entry[] entries = new Entry[32];

    protected int size;

    protected int position;

    /**
     * Sorts drawables by ascending group ID, then descending depth, then by ascending ordinal.
     */
    protected Comparator<Entry> sortComparator = new Comparator<Entry>() {
        @Override
        public int compare(Entry lhs, Entry rhs) {
            if (lhs.groupId < rhs.groupId) { // lhs group is first; sort lhs before rhs
                return -1;
            } else if (lhs.groupId > rhs.groupId) { // rhs group is first; sort rhs before lhs
                return 1;
            } else if (lhs.depth > rhs.depth) { // lhs is farther than rhs; sort lhs before rhs
                return -1;
            } else if (lhs.depth < rhs.depth) {  // lhs is closer than rhs; sort rhs before lhs
                return 1;
            } else { // lhs and rhs have the same depth; sort by insertion order
                return lhs.ordinal - rhs.ordinal;
            }
        }
    };

    public DrawableQueue() {
    }

    public void offerDrawable(Drawable drawable, int groupId, double depth) {
        if (drawable != null) {
            if (this.entries.length <= this.size) {
                Entry[] newArray = new Entry[this.size + (this.size >> 1)];
                System.arraycopy(this.entries, 0, newArray, 0, this.size);
                this.entries = newArray;
            }

            if (this.entries[this.size] == null) {
                this.entries[this.size] = new Entry();
            }

            this.entries[this.size].set(drawable, groupId, depth, this.size);
            this.size++;
        }
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

    public void clearDrawables() {
        for (int i = 0, len = this.size; i < len; i++) {
            this.entries[i].recycle();
        }

        this.size = 0;
        this.position = 0;
    }

    public void sortDrawables() {
        Arrays.sort(this.entries, 0, this.size, this.sortComparator);
    }

    protected static class Entry {

        public Drawable drawable;

        public int groupId;

        public double depth;

        public int ordinal;

        public void set(Drawable drawable, int groupId, double depth, int ordinal) {
            this.drawable = drawable;
            this.groupId = groupId;
            this.depth = depth;
            this.ordinal = ordinal;
        }

        public void recycle() {
            this.drawable.recycle();
            this.drawable = null;
        }
    }
}
