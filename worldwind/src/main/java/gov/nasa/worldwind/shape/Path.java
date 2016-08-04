/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.draw.DrawableElements;
import gov.nasa.worldwind.draw.DrawableShape;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.FloatArray;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.ShortArray;

public class Path extends AbstractShape {

    protected List<Position> positions;

    protected boolean extrude;

    protected FloatArray vertexArray = new FloatArray();

    protected ShortArray interiorElements = new ShortArray();

    protected ShortArray outlineElements = new ShortArray();

    protected ShortArray verticalElements = new ShortArray();

    protected String vertexBufferKey = nextCacheKey();

    protected String elementBufferKey = nextCacheKey();

    protected Vec3 vertexOrigin = new Vec3();

    private int numSubsegments = 10;

    private Location loc = new Location();

    private Vec3 point = new Vec3();

    protected static final double NEAR_ZERO_THRESHOLD = 1.0e-10;

    private static long cacheKeySequence;

    private static String nextCacheKey() {
        return Path.class.getName() + ".cacheKey." + Long.toString(++cacheKeySequence);
    }

    public Path(List<Position> positions) {
        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Path", "constructor", "missingList"));
        }

        this.positions = positions;
    }

    public Path(List<Position> positions, ShapeAttributes attributes) {
        super(attributes);

        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Path", "constructor", "missingList"));
        }

        this.positions = positions;
    }

    public List<Position> getPositions() {
        return this.positions;
    }

    public void setPositions(List<Position> positions) {
        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Path", "setPositions", "missingList"));
        }

        this.positions = positions;
        this.reset();
    }

    public boolean isExtrude() {
        return this.extrude;
    }

    public void setExtrude(boolean extrude) {
        this.extrude = extrude;
        this.reset();
    }

    protected void reset() {
        this.vertexArray.clear();
    }

    @Override
    protected void makeDrawable(RenderContext rc) {
        if (this.positions.isEmpty()) {
            return; // nothing to draw
        }

        if (this.mustAssembleGeometry(rc)) {
            this.assembleGeometry(rc);
            //rc.renderResourceCache.remove(this.vertexBufferKey);
            //rc.renderResourceCache.remove(this.elementBufferKey);
            this.vertexBufferKey = nextCacheKey();
            this.elementBufferKey = nextCacheKey();
        }

        // Obtain a drawable form the render context pool.
        Pool<DrawableShape> pool = rc.getDrawablePool(DrawableShape.class);
        DrawableShape drawable = DrawableShape.obtain(pool);

        // Use the basic GLSL program to draw the shape.
        drawable.program = (BasicShaderProgram) rc.getShaderProgram(BasicShaderProgram.KEY);
        if (drawable.program == null) {
            drawable.program = (BasicShaderProgram) rc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(rc.resources));
        }

        // Assemble the drawable's OpenGL vertex buffer object.
        drawable.vertexBuffer = rc.getBufferObject(this.vertexBufferKey);
        if (drawable.vertexBuffer == null) {
            int size = this.vertexArray.size() * 4;
            FloatBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
            buffer.put(this.vertexArray.array(), 0, this.vertexArray.size());
            BufferObject bufferObject = new BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer.rewind());
            drawable.vertexBuffer = rc.putBufferObject(this.vertexBufferKey, bufferObject);
        }

        // Assemble the drawable's OpenGL element buffer object.
        drawable.elementBuffer = rc.getBufferObject(this.elementBufferKey);
        if (drawable.elementBuffer == null) {
            int size = (this.interiorElements.size() * 2) + (this.outlineElements.size() * 2) + (this.verticalElements.size() * 2);
            ShortBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();
            buffer.put(this.interiorElements.array(), 0, this.interiorElements.size());
            buffer.put(this.outlineElements.array(), 0, this.outlineElements.size());
            buffer.put(this.verticalElements.array(), 0, this.verticalElements.size());
            BufferObject bufferObject = new BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind());
            drawable.elementBuffer = rc.putBufferObject(this.elementBufferKey, bufferObject);
        }

        // Configure the drawable to display the path's outline.
        if (this.activeAttributes.drawOutline) {
            DrawableElements prim = drawable.addDrawElements(GLES20.GL_LINE_STRIP, this.outlineElements.size(),
                GLES20.GL_UNSIGNED_SHORT, this.interiorElements.size() * 2);
            prim.color.set(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
            prim.lineWidth = this.activeAttributes.outlineWidth;
        }

        // Configure the drawable to display the path's extruded verticals.
        if (this.activeAttributes.drawOutline && this.activeAttributes.drawVerticals && this.extrude) {
            DrawableElements prim = drawable.addDrawElements(GLES20.GL_LINES, this.verticalElements.size(),
                GLES20.GL_UNSIGNED_SHORT, (this.interiorElements.size() * 2) + (this.outlineElements.size() * 2));
            prim.color.set(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
            prim.lineWidth = this.activeAttributes.outlineWidth;
        }

        // Configure the drawable to display the path's extruded interior.
        if (this.activeAttributes.drawInterior && this.extrude) {
            DrawableElements prim = drawable.addDrawElements(GLES20.GL_TRIANGLE_STRIP, this.interiorElements.size(),
                GLES20.GL_UNSIGNED_SHORT, 0);
            prim.color.set(rc.pickMode ? this.pickColor : this.activeAttributes.interiorColor);
        }

        // Configure the drawable according to the shape's attributes.
        drawable.vertexOrigin.set(this.vertexOrigin);
        drawable.enableDepthTest = this.activeAttributes.depthTest;

        // Enqueue the drawable for processing on the OpenGL thread.
        double cameraDistance = this.boundingBox.distanceTo(rc.cameraPoint);
        rc.offerShapeDrawable(drawable, cameraDistance);
    }

    protected boolean mustAssembleGeometry(RenderContext rc) {
        return this.vertexArray.size() == 0;
    }

    protected void assembleGeometry(RenderContext rc) {
        // Clear the path's vertex array and element arrays. These arrays will accumulate values as the path's Cartesian
        // geometry is assembled.
        this.vertexArray.clear();
        this.interiorElements.clear();
        this.outlineElements.clear();
        this.verticalElements.clear();

        // Compute the path's local Cartesian coordinate origin and add the first vertex.
        Position begin = this.positions.get(0);
        rc.geographicToCartesian(begin.latitude, begin.longitude, begin.altitude, this.altitudeMode, this.vertexOrigin);
        this.addVertex(rc, begin.latitude, begin.longitude, begin.altitude, false /*tessellated*/);

        // Add the remaining path vertices, tessellating each segment as indicated by the path's properties.
        for (int idx = 1, len = this.positions.size(); idx < len; idx++) {
            Position end = this.positions.get(idx);
            this.addSegment(rc, begin, end);
            begin = end;
        }

        // Compute the path's Cartesian bounding box from its Cartesian coordinates.
        this.boundingBox.setToPoints(this.vertexArray.array(), this.vertexArray.size(), 3);
        this.boundingBox.translate(this.vertexOrigin.x, this.vertexOrigin.y, this.vertexOrigin.z);
    }

    protected void addSegment(RenderContext rc, Position begin, Position end) {
        double azimuth = 0;
        double length = 0;

        switch (this.pathType) {
            case WorldWind.GREAT_CIRCLE:
                azimuth = begin.greatCircleAzimuth(end);
                length = begin.greatCircleDistance(end);
                break;
            case WorldWind.LINEAR:
                azimuth = begin.linearAzimuth(end);
                length = begin.linearDistance(end);
                break;
            case WorldWind.RHUMB_LINE:
                azimuth = begin.rhumbAzimuth(end);
                length = begin.rhumbDistance(end);
                break;
        }

        if (length < NEAR_ZERO_THRESHOLD) {
            return; // suppress the next point when the segment length less than a millimeter (on Earth)
        }

        if (this.numSubsegments > 0) {
            double deltaDist = length / this.numSubsegments;
            double deltaAlt = (end.altitude - begin.altitude) / this.numSubsegments;
            double dist = deltaDist;
            double alt = begin.altitude + deltaAlt;

            for (int idx = 1; idx < this.numSubsegments; idx++) {
                switch (this.pathType) {
                    case WorldWind.GREAT_CIRCLE:
                        begin.greatCircleLocation(azimuth, dist, this.loc);
                        break;
                    case WorldWind.LINEAR:
                        begin.linearLocation(azimuth, dist, this.loc);
                        break;
                    case WorldWind.RHUMB_LINE:
                        begin.rhumbLocation(azimuth, dist, this.loc);
                        break;
                }

                this.addVertex(rc, this.loc.latitude, this.loc.longitude, alt, true /*tessellated*/);
                dist += deltaDist;
                alt += deltaAlt;
            }
        }

        // Explicitly add the endpoint to ensure alignment.
        this.addVertex(rc, end.latitude, end.longitude, end.altitude, false /*tessellated*/);
    }

    protected void addVertex(RenderContext rc, double latitude, double longitude, double altitude, boolean tessellated) {
        rc.geographicToCartesian(latitude, longitude, altitude, this.altitudeMode, this.point);

        int topVertex = this.vertexArray.size() / 3;
        this.vertexArray.add((float) (this.point.x - this.vertexOrigin.x));
        this.vertexArray.add((float) (this.point.y - this.vertexOrigin.y));
        this.vertexArray.add((float) (this.point.z - this.vertexOrigin.z));

        this.outlineElements.add((short) topVertex);

        if (this.extrude) {
            rc.geographicToCartesian(latitude, longitude, 0, WorldWind.CLAMP_TO_GROUND, this.point);

            int bottomVertex = this.vertexArray.size() / 3;
            this.vertexArray.add((float) (this.point.x - this.vertexOrigin.x));
            this.vertexArray.add((float) (this.point.y - this.vertexOrigin.y));
            this.vertexArray.add((float) (this.point.z - this.vertexOrigin.z));

            this.interiorElements.add((short) topVertex);
            this.interiorElements.add((short) bottomVertex);

            if (!tessellated) {
                this.verticalElements.add((short) topVertex);
                this.verticalElements.add((short) bottomVertex);
            }
        }
    }
}
