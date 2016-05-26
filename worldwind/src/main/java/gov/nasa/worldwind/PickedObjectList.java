/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.util.SparseArray;

import gov.nasa.worldwind.util.Logger;

public class PickedObjectList {

    protected SparseArray<PickedObject> entries = new SparseArray<>();

    public PickedObjectList() {
    }

    public int count() {
        return this.entries.size();
    }

    public void offerPickedObject(PickedObject pickedObject) {
        if (pickedObject != null) {
            this.entries.put(pickedObject.identifier, pickedObject);
        }
    }

    public PickedObject pickedObjectAt(int index) {
        if (index < 0 || index >= this.entries.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "PickedObjectList", "getPickedObject", "invalidIndex"));
        }

        return this.entries.valueAt(index);
    }

    public PickedObject pickedObjectWithId(int identifier) {
        return this.entries.get(identifier);
    }

    public PickedObject topPickedObject() {
        for (int idx = 0, len = this.entries.size(); idx < len; idx++) {
            PickedObject po = this.entries.valueAt(idx);
            if (po.isOnTop()) {
                return po;
            }
        }

        return null;
    }

    public PickedObject terrainPickedObject() {
        for (int idx = 0, len = this.entries.size(); idx < len; idx++) {
            PickedObject po = this.entries.valueAt(idx);
            if (po.isTerrain()) {
                return po;
            }
        }

        return null;
    }

    public boolean hasNonTerrainObjects() {
        for (int idx = 0, len = this.entries.size(); idx < len; idx++) {
            PickedObject po = this.entries.valueAt(idx);
            if (!po.isTerrain()) {
                return true;
            }
        }

        return false;
    }

    public void clearPickedObjects() {
        this.entries.clear();
    }
}
