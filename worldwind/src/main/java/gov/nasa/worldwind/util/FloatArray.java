/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

public class FloatArray {

    protected static final int MIN_CAPACITY_INCREMENT = 12;

    protected static final float[] EMPTY_ARRAY = new float[0];

    protected float[] array;

    protected int size;

    public FloatArray() {
        this.array = EMPTY_ARRAY;
    }

    public FloatArray(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "FloatArray", "constructor", "invalidCapacity"));
        }

        this.array = new float[initialCapacity];
    }

    public float[] array() {
        return this.array;
    }

    public int size() {
        return this.size;
    }

    public float get(int index) {
        return this.array[index];
    }

    public FloatArray set(int index, float value) {
        this.array[index] = value;
        return this;
    }

    public FloatArray add(float value) {
        int capacity = this.array.length;
        if (capacity == this.size) {
            int increment = Math.max(capacity >> 1, MIN_CAPACITY_INCREMENT);
            float[] newArray = new float[capacity + increment];
            System.arraycopy(this.array, 0, newArray, 0, capacity);
            this.array = newArray;
        }

        this.array[this.size++] = value;
        return this;
    }

    public FloatArray trimToSize() {
        int size = this.size;
        if (size == this.array.length) {
            return this; // array is already trimmed to size
        }

        if (size == 0) {
            this.array = EMPTY_ARRAY;
        } else {
            float[] newArray = new float[size];
            System.arraycopy(this.array, 0, newArray, 0, size);
            this.array = newArray;
        }

        return this;
    }

    public FloatArray clear() {
        this.array = EMPTY_ARRAY;
        this.size = 0;
        return this;
    }
}
