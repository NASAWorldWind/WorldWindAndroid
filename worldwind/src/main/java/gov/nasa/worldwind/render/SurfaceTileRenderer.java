/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

public interface SurfaceTileRenderer {

    void renderTile(DrawContext dc, SurfaceTile surfaceTile);

    void renderTiles(DrawContext dc, Iterable<? extends SurfaceTile> surfaceTiles);
}
