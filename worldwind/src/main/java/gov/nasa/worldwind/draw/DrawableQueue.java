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
     * Sorts drawables by depth from front to back and then by insertion time.
     */
    protected Comparator<Entry> backToFrontComparator = new Comparator<Entry>() {
        @Override
        public int compare(Entry lhs, Entry rhs) {
            if (lhs.depth > rhs.depth) { // lhs is farther than rhs; sort lhs before rhs
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

    public void recycle() {
        for (int i = 0, len = this.size; i < len; i++) {
            this.entries[i].recycle();
        }

        this.size = 0;
        this.position = 0;
    }

    public void offerDrawable(Drawable drawable, double depth) {
        if (drawable != null) {
            if (this.entries.length <= this.size) {
                Entry[] newArray = new Entry[this.size + (this.size >> 1)];
                System.arraycopy(this.entries, 0, newArray, 0, this.size);
                this.entries = newArray;
            }

            if (this.entries[this.size] == null) {
                this.entries[this.size] = new Entry();
            }

            this.entries[this.size].set(drawable, depth, this.size);
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

    public void sortBackToFront() {
        Arrays.sort(this.entries, 0, this.size, this.backToFrontComparator);
    }

    protected static class Entry {

        public Drawable drawable;

        public double depth;

        public int ordinal;

        public final void set(Drawable drawable, double depth, int ordinal) {
            this.drawable = drawable;
            this.depth = depth;
            this.ordinal = ordinal;
        }

        public final void recycle() {
            this.drawable.recycle();
            this.drawable = null;
        }
    }
}
