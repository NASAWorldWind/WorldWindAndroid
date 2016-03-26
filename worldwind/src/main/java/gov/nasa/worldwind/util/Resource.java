/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.content.Context;

public class Resource {

    public final Context context;

    public final int id;

    public Resource(Context context, int id) {
        if (context == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Resource", "constructor", "missingContext"));
        }

        this.context = context;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        Resource that = (Resource) o;
        return this.context.equals(that.context) && this.id == that.id;

    }

    @Override
    public int hashCode() {
        return 31 * this.context.hashCode() + this.id;
    }

    @Override
    public String toString() {
        return "Resource{" +
            "context=" + this.context +
            ", id=" + this.id +
            '}';
    }
}
