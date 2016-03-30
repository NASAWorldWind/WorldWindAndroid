/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logger;

public class RenderableLayer extends AbstractLayer implements Iterable<Renderable> {

    protected List<Renderable> renderables = new ArrayList<>();

    public RenderableLayer() {
    }

    public RenderableLayer(String displayName) {
        super(displayName);
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

        for (int i = 0; i < this.renderables.size(); i++) {
            String renderableName = this.renderables.get(i).getDisplayName();
            if ((renderableName == null) ? (name == null) : renderableName.equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public int indexOfRenderableWithProperty(Object key, Object value) {

        for (int i = 0; i < this.renderables.size(); i++) {
            Renderable renderable = this.renderables.get(i);
            if (renderable.hasUserProperty(key)) {
                Object layerValue = renderable.getUserProperty(key);
                if ((layerValue == null) ? (value == null) : layerValue.equals(value)) {
                    return i;
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

    public void addAllRenderables(Iterable<? extends Renderable> renderables) {
        if (renderables == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "addAllRenderables", "missingList"));
        }

        for (Renderable renderable : renderables) {
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
    protected void doRender(DrawContext dc) {

        for (Renderable renderable : this.renderables) {
            try {
                renderable.render(dc);
            } catch (Exception e) {
                Logger.logMessage(Logger.ERROR, "RenderableLayer", "doRender",
                    "Exception while rendering shape \'" + renderable.getDisplayName() + "\'", e);
                // Keep going. Draw the remaining renderables.
            }
        }
    }
}
