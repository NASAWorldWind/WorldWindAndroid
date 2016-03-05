/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.layer;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

public class RenderableLayer extends AbstractLayer implements List<Renderable> {

    protected List<Renderable> renderables;

    @Override
    public void add(int location, Renderable object) {

    }

    @Override
    public boolean add(Renderable object) {
        return false;
    }

    @Override
    public boolean addAll(int location, Collection<? extends Renderable> collection) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Renderable> collection) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean contains(Object object) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return false;
    }

    @Override
    public Renderable get(int location) {
        return null;
    }

    @Override
    public int indexOf(Object object) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @NonNull
    @Override
    public Iterator<Renderable> iterator() {
        return null;
    }

    @Override
    public int lastIndexOf(Object object) {
        return 0;
    }

    @Override
    public ListIterator<Renderable> listIterator() {
        return null;
    }

    @NonNull
    @Override
    public ListIterator<Renderable> listIterator(int location) {
        return null;
    }

    @Override
    public Renderable remove(int location) {
        return null;
    }

    @Override
    public boolean remove(Object object) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    @Override
    public Renderable set(int location, Renderable object) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @NonNull
    @Override
    public List<Renderable> subList(int start, int end) {
        return null;
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] array) {
        return null;
    }

    @Override
    protected void doRender(DrawContext dc) {

    }
}
