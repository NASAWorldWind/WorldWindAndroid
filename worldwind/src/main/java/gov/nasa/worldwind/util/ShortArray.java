/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

public class ShortArray {

    protected static final int MIN_CAPACITY_INCREMENT = 12;

    protected static final short[] EMPTY_ARRAY = new short[0];

    protected short[] array;

    protected int size;

    public ShortArray() {
        this.array = EMPTY_ARRAY;
    }

    public ShortArray(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ShortArray", "constructor", "invalidCapacity"));
        }

        this.array = new short[initialCapacity];
    }

    public short[] array() {
        return this.array;
    }

    public int size() {
        return this.size;
    }

    public short get(int index) {
        return this.array[index];
    }

    public ShortArray set(int index, short value) {
        this.array[index] = value;
        return this;
    }

    public ShortArray add(short value) {
        int capacity = this.array.length;
        if (capacity == this.size) {
            int increment = Math.max(capacity >> 1, MIN_CAPACITY_INCREMENT);
            short[] newArray = new short[capacity + increment];
            System.arraycopy(this.array, 0, newArray, 0, capacity);
            this.array = newArray;
        }

        this.array[this.size++] = value;
        return this;
    }

    public ShortArray trimToSize() {
        int size = this.size;
        if (size == this.array.length) {
            return this; // array is already trimmed to size
        }

        if (size == 0) {
            this.array = EMPTY_ARRAY;
        } else {
            short[] newArray = new short[size];
            System.arraycopy(this.array, 0, newArray, 0, size);
            this.array = newArray;
        }

        return this;
    }

    public ShortArray clear() {
        this.array = new short[0];
        this.size = 0;
        return this;
    }
}
