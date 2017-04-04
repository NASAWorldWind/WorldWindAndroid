/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import java.util.ArrayList;
import java.util.Iterator;

import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Logger;

public class LayerList implements Iterable<Layer> {

    protected ArrayList<Layer> layers = new ArrayList<>();

    public LayerList() {
    }

    public LayerList(LayerList layerList) {
        if (layerList == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "constructor", "missingList"));
        }

        this.addAllLayers(layerList);
    }

    public LayerList(Iterable<? extends Layer> layers) {
        if (layers == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "constructor", "missingList"));
        }

        this.addAllLayers(layers);
    }

    public int count() {
        return this.layers.size();
    }

    public Layer getLayer(int index) {
        if (index < 0 || index >= this.layers.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "getLayer", "invalidIndex"));
        }

        return this.layers.get(index);
    }

    public Layer setLayer(int index, Layer layer) {
        if (index < 0 || index >= this.layers.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "setLayer", "invalidIndex"));
        }

        if (layer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "setLayer", "missingLayer"));
        }

        return this.layers.set(index, layer);
    }

    public int indexOfLayer(Layer layer) {
        if (layer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "indexOfLayer", "missingLayer"));
        }

        return this.layers.indexOf(layer);
    }

    public int indexOfLayerNamed(String name) {

        for (int idx = 0, len = this.layers.size(); idx < len; idx++) {
            String layerName = this.layers.get(idx).getDisplayName();
            if ((layerName == null) ? (name == null) : layerName.equals(name)) {
                return idx;
            }
        }

        return -1;
    }

    public int indexOfLayerWithProperty(Object key, Object value) {

        for (int idx = 0, len = this.layers.size(); idx < len; idx++) {
            Layer layer = this.layers.get(idx);
            if (layer.hasUserProperty(key)) {
                Object layerValue = layer.getUserProperty(key);
                if ((layerValue == null) ? (value == null) : layerValue.equals(value)) {
                    return idx;
                }
            }
        }

        return -1;
    }

    public void addLayer(Layer layer) {
        if (layer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "addLayer", "missingLayer"));
        }

        this.layers.add(layer);
    }

    public void addLayer(int index, Layer layer) {
        if (index < 0 || index > this.layers.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "addLayer", "invalidIndex"));
        }

        if (layer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "addLayer", "missingLayer"));
        }

        this.layers.add(index, layer);
    }

    public void addAllLayers(LayerList list) {
        if (list == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "addAllLayers", "missingList"));
        }

        ArrayList<Layer> thisList = this.layers;
        ArrayList<Layer> thatList = list.layers;
        thisList.ensureCapacity(thatList.size());

        for (int idx = 0, len = thatList.size(); idx < len; idx++) {
            thisList.add(thatList.get(idx)); // we know the contents of layerList.layers is valid
        }
    }

    public void addAllLayers(Iterable<? extends Layer> iterable) {
        if (iterable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "addAllLayers", "missingIterable"));
        }

        for (Layer layer : iterable) {
            if (layer == null) {
                throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "addAllLayers", "missingLayer"));
            }

            this.layers.add(layer);
        }
    }

    public boolean removeLayer(Layer layer) {
        if (layer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "removeLayer", "missingLayer"));
        }

        return this.layers.remove(layer);
    }

    public Layer removeLayer(int index) {
        if (index < 0 || index >= this.layers.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "removeLayer", "invalidIndex"));
        }

        return this.layers.remove(index);
    }

    public boolean removeAllLayers(Iterable<? extends Layer> layers) {
        if (layers == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "LayerList", "removeAllLayers", "missingList"));
        }

        boolean removed = false;

        for (Layer layer : layers) {
            if (layer == null) {
                throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "LayerList", "removeAllLayers", "missingLayer"));
            }

            removed |= this.layers.remove(layer);
        }

        return removed;
    }

    public void clearLayers() {
        this.layers.clear();
    }

    @Override
    public Iterator<Layer> iterator() {
        return this.layers.iterator();
    }

    public void render(RenderContext rc) {
        for (int idx = 0, len = this.layers.size(); idx < len; idx++) {
            rc.currentLayer = this.layers.get(idx);
            try {
                rc.currentLayer.render(rc);
            } catch (Exception e) {
                Logger.logMessage(Logger.ERROR, "LayerList", "render",
                    "Exception while rendering layer \'" + rc.currentLayer.getDisplayName() + "\'", e);
                // Keep going. Draw the remaining layers.
            }
        }

        rc.currentLayer = null;
    }
}
