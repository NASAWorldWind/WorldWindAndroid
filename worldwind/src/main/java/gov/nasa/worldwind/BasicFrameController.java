/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import android.opengl.GLES20;

import java.util.Set;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.draw.DrawableSurfaceColor;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;

public class BasicFrameController implements FrameController {

    private Color pickColor;

    private Vec3 pickPoint = new Vec3();

    private Position pickPos = new Position();

    public BasicFrameController() {
    }

    @Override
    public void renderFrame(RenderContext rc) {
        rc.terrainTessellator.tessellate(rc);

        if (rc.pickMode) {
            this.renderTerrainPickedObject(rc);
        }

        rc.layers.render(rc);
        rc.sortDrawables();
    }

    protected void renderTerrainPickedObject(RenderContext rc) {
        if (rc.terrain.getSector().isEmpty()) {
            return; // no terrain to pick
        }

        // Acquire a unique picked object ID for terrain.
        int pickedObjectId = rc.nextPickedObjectId();

        // Enqueue a drawable for processing on the OpenGL thread that displays terrain in the unique pick color.
        Pool<DrawableSurfaceColor> pool = rc.getDrawablePool(DrawableSurfaceColor.class);
        DrawableSurfaceColor drawable = DrawableSurfaceColor.obtain(pool);
        drawable.color = PickedObject.identifierToUniqueColor(pickedObjectId, drawable.color);
        drawable.program = (BasicShaderProgram) rc.getShaderProgram(BasicShaderProgram.KEY);
        if (drawable.program == null) {
            drawable.program = (BasicShaderProgram) rc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(rc.resources));
        }
        rc.offerSurfaceDrawable(drawable, Double.NEGATIVE_INFINITY /*z-order before all other surface drawables*/);

        // If the pick ray intersects the terrain, enqueue a picked object that associates the terrain drawable with its
        // picked object ID and the intersection position.
        if (rc.pickRay != null && rc.terrain.intersect(rc.pickRay, this.pickPoint)) {
            rc.globe.cartesianToGeographic(this.pickPoint.x, this.pickPoint.y, this.pickPoint.z, this.pickPos);
            this.pickPos.altitude = 0; // report the actual altitude, which may not lie on the terrain's surface
            rc.offerPickedObject(PickedObject.fromTerrain(pickedObjectId, this.pickPos));
        }
    }

    @Override
    public void drawFrame(DrawContext dc) {
        this.clearFrame(dc);
        this.drawDrawables(dc);

        if (dc.pickMode && dc.pickPoint != null) {
            this.resolvePick(dc);
        } else if (dc.pickMode) {
            this.resolvePickRect(dc);
        }
    }

    protected void clearFrame(DrawContext dc) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    protected void drawDrawables(DrawContext dc) {
        dc.rewindDrawables();

        Drawable next;
        while ((next = dc.pollDrawable()) != null) {
            try {
                next.draw(dc);
            } catch (Exception e) {
                Logger.logMessage(Logger.ERROR, "BasicFrameController", "drawDrawables",
                    "Exception while drawing \'" + next + "\'", e);
                // Keep going. Draw the remaining drawables.
            }
        }
    }

    protected void resolvePick(DrawContext dc) {
        if (dc.pickedObjects.count() == 0) {
            return; // no eligible objects; avoid expensive calls to glReadPixels
        }

        // Read the fragment color at the pick point.
        this.pickColor = dc.readPixelColor((int) Math.round(dc.pickPoint.x), (int) Math.round(dc.pickPoint.y), this.pickColor);

        // Convert the fragment color to a picked object ID. This returns zero if the color cannot indicate a picked
        // object ID, in which case no objects have been drawn at the pick point.
        int topObjectId = PickedObject.uniqueColorToIdentifier(this.pickColor);
        if (topObjectId != 0) {
            PickedObject terrainObject = dc.pickedObjects.terrainPickedObject();
            PickedObject topObject = dc.pickedObjects.pickedObjectWithId(topObjectId);
            if (topObject != null) {
                topObject.markOnTop();
                dc.pickedObjects.clearPickedObjects();
                dc.pickedObjects.offerPickedObject(topObject);
                dc.pickedObjects.offerPickedObject(terrainObject); // handles null objects and duplicate objects
            } else {
                dc.pickedObjects.clearPickedObjects(); // no eligible objects drawn at the pick point
            }
        } else {
            dc.pickedObjects.clearPickedObjects(); // no objects drawn at the pick point
        }
    }

    protected void resolvePickRect(DrawContext dc) {
        if (dc.pickedObjects.count() == 0) {
            return; // no eligible objects; avoid expensive calls to glReadPixels
        }

        // Read the unique fragment colors in the pick rectangle.
        Set<Color> pickColors = dc.readPixelColors(dc.pickViewport.x, dc.pickViewport.y, dc.pickViewport.width, dc.pickViewport.height);

        for (Color pickColor : pickColors) {
            // Convert the fragment color to a picked object ID. This returns zero if the color cannot indicate a picked
            // object ID.
            int topObjectId = PickedObject.uniqueColorToIdentifier(pickColor);
            if (topObjectId != 0) {
                PickedObject topObject = dc.pickedObjects.pickedObjectWithId(topObjectId);
                if (topObject != null) {
                    topObject.markOnTop();
                }
            }
        }

        // Remove all picked objects not marked as on top.
        dc.pickedObjects.keepTopObjects();
    }
}
