/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import java.util.ArrayList;
import java.util.Iterator;

import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logger;

public class RenderableLayer extends AbstractLayer implements Iterable<Renderable> {

    protected ArrayList<Renderable> renderables = new ArrayList<>();

    public RenderableLayer() {
    }

    public RenderableLayer(String displayName) {
        super(displayName);
    }

    public RenderableLayer(RenderableLayer layer) {
        if (layer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "constructor", "missingLayer"));
        }

        this.setDisplayName(layer.displayName);
        this.addAllRenderables(layer);
    }

    public RenderableLayer(Iterable<? extends Renderable> renderables) {
        if (renderables == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "constructor", "missingList"));
        }

        this.addAllRenderables(renderables);
    }

    public int count() {
        return this.renderables.size();
    }

    public Renderable getRenderable(int index) {
        if (index < 0 || index >= this.renderables.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "getRenderable", "invalidIndex"));
        }

        return this.renderables.get(index);
    }

    public Renderable setRenderable(int index, Renderable renderable) {
        if (index < 0 || index >= this.renderables.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "setRenderable", "invalidIndex"));
        }

        if (renderable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "setRenderable", "missingRenderable"));
        }

        return this.renderables.set(index, renderable);
    }

    public int indexOfRenderable(Renderable renderable) {
        if (renderable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "indexOfRenderable", "missingRenderable"));
        }

        return this.renderables.indexOf(renderable);
    }

    public int indexOfRenderableNamed(String name) {

        for (int idx = 0, len = this.renderables.size(); idx < len; idx++) {
            String renderableName = this.renderables.get(idx).getDisplayName();
            if ((renderableName == null) ? (name == null) : renderableName.equals(name)) {
                return idx;
            }
        }

        return -1;
    }

    public int indexOfRenderableWithProperty(Object key, Object value) {

        for (int idx = 0, len = this.renderables.size(); idx < len; idx++) {
            Renderable renderable = this.renderables.get(idx);
            if (renderable.hasUserProperty(key)) {
                Object layerValue = renderable.getUserProperty(key);
                if ((layerValue == null) ? (value == null) : layerValue.equals(value)) {
                    return idx;
                }
            }
        }

        return -1;
    }

    public void addRenderable(Renderable renderable) {
        if (renderable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "addRenderable", "missingRenderable"));
        }

        this.renderables.add(renderable);
    }

    public void addRenderable(int index, Renderable renderable) {
        if (index < 0 || index > this.renderables.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "addRenderable", "invalidIndex"));
        }

        if (renderable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "addRenderable", "missingRenderable"));
        }

        this.renderables.add(index, renderable);
    }

    public void addAllRenderables(RenderableLayer layer) {
        if (layer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "addAllRenderables", "missingLayer"));
        }

        ArrayList<Renderable> thisList = this.renderables;
        ArrayList<Renderable> thatList = layer.renderables;
        thisList.ensureCapacity(thatList.size());

        for (int idx = 0, len = thatList.size(); idx < len; idx++) {
            thisList.add(thatList.get(idx)); // we know the contents of layer.renderables is valid
        }
    }

    public void addAllRenderables(Iterable<? extends Renderable> iterable) {
        if (iterable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "addAllRenderables", "missingIterable"));
        }

        for (Renderable renderable : iterable) {
            if (renderable == null) {
                throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "RenderableLayer", "addAllRenderables", "missingRenderable"));
            }

            this.renderables.add(renderable);
        }
    }

    public boolean removeRenderable(Renderable renderable) {
        if (renderable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "removeRenderable", "missingRenderable"));
        }

        return this.renderables.remove(renderable);
    }

    public Renderable removeRenderable(int index) {
        if (index < 0 || index >= this.renderables.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "removeRenderable", "invalidIndex"));
        }

        return this.renderables.remove(index);
    }

    public boolean removeAllRenderables(Iterable<? extends Renderable> renderables) {
        if (renderables == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "removeAllRenderables", "missingList"));
        }

        boolean removed = false;

        for (Renderable renderable : renderables) {
            if (renderable == null) {
                throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "RenderableLayer", "removeAllRenderables", "missingRenderable"));
            }

            removed |= this.renderables.remove(renderable);
        }

        return removed;
    }

    public void clearRenderables() {
        this.renderables.clear();
    }

    @Override
    public Iterator<Renderable> iterator() {
        return this.renderables.iterator();
    }

    @Override
    protected void doRender(RenderContext rc) {
        for (int idx = 0, len = this.renderables.size(); idx < len; idx++) {
            Renderable renderable = this.renderables.get(idx);
            try {
                renderable.render(rc);
            } catch (Exception e) {
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "doRender",
                    "Exception while rendering shape \'" + renderable.getDisplayName() + "\'", e);
                // Keep going. Draw the remaining renderables.
            }
        }
    }
}
