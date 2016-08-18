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
import java.util.Collections;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.draw.DrawShapeState;
import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.draw.DrawableShape;
import gov.nasa.worldwind.draw.DrawableSurfaceShape;
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

    protected List<Position> positions = Collections.emptyList();

    protected boolean extrude;

    protected boolean followTerrain;

    protected FloatArray vertexArray = new FloatArray();

    protected ShortArray interiorElements = new ShortArray();

    protected ShortArray outlineElements = new ShortArray();

    protected ShortArray verticalElements = new ShortArray();

    protected Object vertexBufferKey = nextCacheKey();

    protected Object elementBufferKey = nextCacheKey();

    protected Vec3 vertexOrigin = new Vec3();

    protected boolean isSurfaceShape;

    private Vec3 point = new Vec3();

    private Location loc = new Location();

    protected static Object nextCacheKey() {
        return new Object();
    }

    public Path() {
    }

    public Path(ShapeAttributes attributes) {
        super(attributes);
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

    public boolean isFollowTerrain() {
        return this.followTerrain;
    }

    public void setFollowTerrain(boolean followTerrain) {
        this.followTerrain = followTerrain;
        this.reset();
    }

    protected void reset() {
        this.vertexArray.clear();
        this.interiorElements.clear();
        this.outlineElements.clear();
        this.verticalElements.clear();
    }

    @Override
    protected void makeDrawable(RenderContext rc) {
        if (this.positions.isEmpty()) {
            return; // nothing to draw
        }

        if (this.mustAssembleGeometry(rc)) {
            this.assembleGeometry(rc);
            this.vertexBufferKey = nextCacheKey();
            this.elementBufferKey = nextCacheKey();
        }

        // Obtain a drawable form the render context pool.
        Drawable drawable;
        DrawShapeState drawState;
        if (this.isSurfaceShape) {
            Pool<DrawableSurfaceShape> pool = rc.getDrawablePool(DrawableSurfaceShape.class);
            drawable = DrawableSurfaceShape.obtain(pool);
            drawState = ((DrawableSurfaceShape) drawable).drawState;
            ((DrawableSurfaceShape) drawable).sector.set(this.boundingSector);
        } else {
            Pool<DrawableShape> pool = rc.getDrawablePool(DrawableShape.class);
            drawable = DrawableShape.obtain(pool);
            drawState = ((DrawableShape) drawable).drawState;
        }

        // Use the basic GLSL program to draw the shape.
        drawState.program = (BasicShaderProgram) rc.getShaderProgram(BasicShaderProgram.KEY);
        if (drawState.program == null) {
            drawState.program = (BasicShaderProgram) rc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(rc.resources));
        }

        // Assemble the drawable's OpenGL vertex buffer object.
        drawState.vertexBuffer = rc.getBufferObject(this.vertexBufferKey);
        if (drawState.vertexBuffer == null) {
            int size = this.vertexArray.size() * 4;
            FloatBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
            buffer.put(this.vertexArray.array(), 0, this.vertexArray.size());
            drawState.vertexBuffer = new BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer.rewind());
            rc.putBufferObject(this.vertexBufferKey, drawState.vertexBuffer);
        }

        // Assemble the drawable's OpenGL element buffer object.
        drawState.elementBuffer = rc.getBufferObject(this.elementBufferKey);
        if (drawState.elementBuffer == null) {
            int size = (this.interiorElements.size() * 2) + (this.outlineElements.size() * 2) + (this.verticalElements.size() * 2);
            ShortBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();
            buffer.put(this.interiorElements.array(), 0, this.interiorElements.size());
            buffer.put(this.outlineElements.array(), 0, this.outlineElements.size());
            buffer.put(this.verticalElements.array(), 0, this.verticalElements.size());
            drawState.elementBuffer = new BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind());
            rc.putBufferObject(this.elementBufferKey, drawState.elementBuffer);
        }

        // Configure the drawable to display the shape's outline.
        if (this.activeAttributes.drawOutline) {
            drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
            drawState.lineWidth(this.activeAttributes.outlineWidth);
            drawState.drawElements(GLES20.GL_LINE_STRIP, this.outlineElements.size(),
                GLES20.GL_UNSIGNED_SHORT, this.interiorElements.size() * 2);
        }

        // Configure the drawable to display the shape's extruded verticals.
        if (this.activeAttributes.drawOutline && this.activeAttributes.drawVerticals && this.extrude) {
            drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
            drawState.lineWidth(this.activeAttributes.outlineWidth);
            drawState.drawElements(GLES20.GL_LINES, this.verticalElements.size(),
                GLES20.GL_UNSIGNED_SHORT, (this.interiorElements.size() * 2) + (this.outlineElements.size() * 2));
        }

        // Configure the drawable to display the shape's extruded interior.
        if (this.activeAttributes.drawInterior && this.extrude) {
            drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.interiorColor);
            drawState.drawElements(GLES20.GL_TRIANGLE_STRIP, this.interiorElements.size(),
                GLES20.GL_UNSIGNED_SHORT, 0);
        }

        // Configure the drawable according to the shape's attributes.
        drawState.vertexOrigin.set(this.vertexOrigin);
        drawState.enableCullFace = false;
        drawState.enableDepthTest = this.activeAttributes.depthTest;

        // Enqueue the drawable for processing on the OpenGL thread.
        if (this.isSurfaceShape) {
            rc.offerSurfaceDrawable(drawable, 0 /*zOrder*/);
        } else {
            double cameraDistance = this.boundingBox.distanceTo(rc.cameraPoint);
            rc.offerShapeDrawable(drawable, cameraDistance);
        }
    }

    protected boolean mustAssembleGeometry(RenderContext rc) {
        return this.vertexArray.size() == 0;
    }

    protected void assembleGeometry(RenderContext rc) {
        // Determine whether the shape geometry must be assembled as Cartesian geometry or as geographic geometry.
        this.isSurfaceShape = (this.altitudeMode == WorldWind.CLAMP_TO_GROUND) && this.followTerrain;

        // Clear the shape's vertex array and element arrays. These arrays will accumulate values as the shapes's
        // geometry is assembled.
        this.vertexArray.clear();
        this.interiorElements.clear();
        this.outlineElements.clear();
        this.verticalElements.clear();

        // Add the first vertex and compute the shape's local Cartesian coordinate origin.
        Position begin = this.positions.get(0);
        rc.geographicToCartesian(begin.latitude, begin.longitude, begin.altitude, this.altitudeMode, this.vertexOrigin);
        this.addVertex(rc, begin.latitude, begin.longitude, begin.altitude, false /*tessellated*/);

        // Add the remaining vertices, tessellating each edge as indicated by the path's properties.
        for (int idx = 1, len = this.positions.size(); idx < len; idx++) {
            Position end = this.positions.get(idx);
            this.addIntermediateVertices(rc, begin, end);
            this.addVertex(rc, end.latitude, end.longitude, end.altitude, false /*tessellated*/);
            begin = end;
        }

        // Compute the shape's bounding box or bounding sector from its assembled coordinates.
        if (this.isSurfaceShape) {
            this.boundingSector.setEmpty();
            this.boundingSector.union(this.vertexArray.array(), this.vertexArray.size(), 2);
            this.boundingBox.setToUnitBox(); // Surface/geographic shape bounding box is unused
        } else {
            this.boundingBox.setToPoints(this.vertexArray.array(), this.vertexArray.size(), 3);
            this.boundingBox.translate(this.vertexOrigin.x, this.vertexOrigin.y, this.vertexOrigin.z);
            this.boundingSector.setEmpty(); // Cartesian shape bounding sector is unused
        }
    }

    protected void addIntermediateVertices(RenderContext rc, Position begin, Position end) {
        if (this.pathType == WorldWind.LINEAR) {
            return; // suppress intermediate vertices when the path type is linear
        }

        if (this.maximumIntermediatePoints <= 0) {
            return; // suppress intermediate vertices when configured to do so
        }

        double azimuth = 0;
        double length = 0;
        if (this.pathType == WorldWind.GREAT_CIRCLE) {
            azimuth = begin.greatCircleAzimuth(end);
            length = begin.greatCircleDistance(end);
        } else if (this.pathType == WorldWind.RHUMB_LINE) {
            azimuth = begin.rhumbAzimuth(end);
            length = begin.rhumbDistance(end);
        }

        if (length < NEAR_ZERO_THRESHOLD) {
            return; // suppress intermediate vertices when the edge length less than a millimeter (on Earth)
        }

        int numSubsegments = this.maximumIntermediatePoints + 1;
        double deltaDist = length / numSubsegments;
        double deltaAlt = (end.altitude - begin.altitude) / numSubsegments;
        double dist = deltaDist;
        double alt = begin.altitude + deltaAlt;

        for (int idx = 1; idx < numSubsegments; idx++) {
            if (this.pathType == WorldWind.GREAT_CIRCLE) {
                begin.greatCircleLocation(azimuth, dist, this.loc);
            } else if (this.pathType == WorldWind.RHUMB_LINE) {
                begin.rhumbLocation(azimuth, dist, this.loc);
            }

            this.addVertex(rc, this.loc.latitude, this.loc.longitude, alt, true /*tessellated*/);
            dist += deltaDist;
            alt += deltaAlt;
        }
    }

    protected void addVertex(RenderContext rc, double latitude, double longitude, double altitude, boolean intermediate) {
        if (this.isSurfaceShape) {
            this.addVertexGeographic(rc, latitude, longitude, altitude, intermediate);
        } else {
            this.addVertexCartesian(rc, latitude, longitude, altitude, intermediate);
        }
    }

    protected void addVertexGeographic(RenderContext rc, double latitude, double longitude, double altitude, boolean intermediate) {
        int vertex = this.vertexArray.size() / 2;
        this.vertexArray.add((float) longitude);
        this.vertexArray.add((float) latitude);

        this.outlineElements.add((short) vertex);
    }

    protected void addVertexCartesian(RenderContext rc, double latitude, double longitude, double altitude, boolean intermediate) {
        rc.geographicToCartesian(latitude, longitude, altitude, this.altitudeMode, this.point);
        int vertex = this.vertexArray.size() / 3;
        this.vertexArray.add((float) (this.point.x - this.vertexOrigin.x));
        this.vertexArray.add((float) (this.point.y - this.vertexOrigin.y));
        this.vertexArray.add((float) (this.point.z - this.vertexOrigin.z));

        this.outlineElements.add((short) vertex);

        if (this.extrude) {
            rc.geographicToCartesian(latitude, longitude, 0, WorldWind.CLAMP_TO_GROUND, this.point);
            this.vertexArray.add((float) (this.point.x - this.vertexOrigin.x));
            this.vertexArray.add((float) (this.point.y - this.vertexOrigin.y));
            this.vertexArray.add((float) (this.point.z - this.vertexOrigin.z));

            this.interiorElements.add((short) vertex);
            this.interiorElements.add((short) (vertex + 1));
        }

        if (this.extrude && !intermediate) {
            this.verticalElements.add((short) vertex);
            this.verticalElements.add((short) (vertex + 1));
        }
    }
}
