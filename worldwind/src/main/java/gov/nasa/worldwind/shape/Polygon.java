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
import java.util.ArrayList;
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
import gov.nasa.worldwind.util.glu.GLU;
import gov.nasa.worldwind.util.glu.GLUtessellator;
import gov.nasa.worldwind.util.glu.GLUtessellatorCallbackAdapter;

public class Polygon extends AbstractShape {

    protected List<List<Position>> boundaries = new ArrayList<>();

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

    protected GLUtessellatorCallbackAdapter tessCallback = new GLUtessellatorCallbackAdapter() {
        @Override
        public void combineData(double[] coords, Object[] data, float[] weight, Object[] outData, Object polygonData) {
            tessCombine((RenderContext) polygonData, coords, data, weight, outData);
        }

        @Override
        public void vertexData(Object vertexData, Object polygonData) {
            tessVertex((RenderContext) polygonData, vertexData);
        }

        @Override
        public void edgeFlagData(boolean boundaryEdge, Object polygonData) {
            tessEdgeFlag((RenderContext) polygonData, boundaryEdge);
        }

        @Override
        public void errorData(int errnum, Object polygonData) {
            tessError((RenderContext) polygonData, errnum);
        }
    };

    protected static final double NEAR_ZERO_THRESHOLD = 1.0e-10;

    protected static final int VERTEX_USER = 0;

    protected static final int VERTEX_TESSELATED = 1;

    protected static final int VERTEX_COMBINED = 2;

    private int numSubsegments = 10;

    private Vec3 point = new Vec3();

    private Location loc = new Location();

    private double[] tessCoords = new double[3];

    private int[] tessVertices = new int[3];

    private boolean[] tessEdgeFlags = new boolean[3];

    private boolean tessEdgeFlag = true;

    private int tessVertexCount;

    protected static Object nextCacheKey() {
        return new Object();
    }

    public Polygon() {
    }

    public Polygon(ShapeAttributes attributes) {
        super(attributes);
    }

    public Polygon(List<Position> positions) {
        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "constructor", "missingList"));
        }

        this.boundaries.add(positions);
    }

    public Polygon(List<Position> positions, ShapeAttributes attributes) {
        super(attributes);

        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "constructor", "missingList"));
        }

        this.boundaries.add(positions);
    }

    public int getBoundaryCount() {
        return this.boundaries.size();
    }

    public List<Position> getBoundary(int index) {
        if (index < 0 || index >= this.boundaries.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "getBoundary", "invalidIndex"));
        }

        return this.boundaries.get(index);
    }

    public List<Position> setBoundary(int index, List<Position> positions) {
        if (index < 0 || index >= this.boundaries.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "setBoundary", "invalidIndex"));
        }

        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "setBoundary", "missingList"));
        }

        return this.boundaries.set(index, positions);
    }

    public void addBoundary(List<Position> positions) {
        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "addBoundary", "missingList"));
        }

        this.boundaries.add(positions);
    }

    public void addBoundary(int index, List<Position> positions) {
        if (index < 0 || index > this.boundaries.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "addBoundary", "invalidIndex"));
        }

        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "addBoundary", "missingList"));
        }

        this.boundaries.add(index, positions);
    }

    public List<Position> removeBoundary(int index) {
        if (index < 0 || index >= this.boundaries.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "removeBoundary", "invalidIndex"));
        }

        return this.boundaries.remove(index);
    }

    public void clearBoundaries() {
        this.boundaries.clear();
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
        if (this.boundaries.isEmpty()) {
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
            drawState.drawElements(GLES20.GL_LINES, this.outlineElements.size(),
                GLES20.GL_UNSIGNED_SHORT, this.interiorElements.size() * 2);
        }

        // Configure the drawable to display the shape's extruded verticals.
        if (this.activeAttributes.drawOutline && this.activeAttributes.drawVerticals && this.extrude) {
            drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
            drawState.lineWidth(this.activeAttributes.outlineWidth);
            drawState.drawElements(GLES20.GL_LINES, this.verticalElements.size(),
                GLES20.GL_UNSIGNED_SHORT, (this.interiorElements.size() * 2) + (this.outlineElements.size() * 2));
        }

        // Configure the drawable to display the shape's interior (and its optional extruded interior).
        if (this.activeAttributes.drawInterior) {
            drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.interiorColor);
            drawState.drawElements(GLES20.GL_TRIANGLES, this.interiorElements.size(),
                GLES20.GL_UNSIGNED_SHORT, 0);
        }

        // Configure the drawable according to the shape's attributes.
        drawState.vertexOrigin.set(this.vertexOrigin);
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
        // Prepare the vertex origin to be re-computed.
        this.vertexOrigin.set(Double.NaN, Double.NaN, Double.NaN);

        // Determine whether the shape geometry must be assembled as Cartesian geometry or as geographic geometry.
        this.isSurfaceShape = (this.altitudeMode == WorldWind.CLAMP_TO_GROUND) && this.followTerrain;

        // Clear the shape's vertex array and element arrays. These arrays will accumulate values as the shapes's
        // geometry is assembled.
        this.vertexArray.clear();
        this.interiorElements.clear();
        this.outlineElements.clear();
        this.verticalElements.clear();

        GLUtessellator tess = rc.getTessellator();
        GLU.gluTessNormal(tess, 0, 0, 1);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE_DATA, this.tessCallback);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX_DATA, this.tessCallback);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG_DATA, this.tessCallback);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_ERROR_DATA, this.tessCallback);
        GLU.gluTessBeginPolygon(tess, rc);

        for (int boundaryIdx = 0, boundaryCount = this.boundaries.size(); boundaryIdx < boundaryCount; boundaryIdx++) {

            List<Position> positions = this.boundaries.get(boundaryIdx);
            if (positions.isEmpty()) {
                continue; // no boundary to tessellate
            }

            GLU.gluTessBeginContour(tess);

            // Assemble the vertices for each edge between boundary positions.
            Position begin = positions.get(0);
            for (int idx = 1, len = positions.size(); idx < len; idx++) {
                Position end = positions.get(idx);
                this.addEdge(rc, begin, end);
                begin = end;
            }

            // If the the boundary is not closed, add the vertices the edge between the last and first positions.
            if (!begin.equals(positions.get(0))) {
                this.addEdge(rc, begin, positions.get(0));
            }

            GLU.gluTessEndContour(tess);
        }

        GLU.gluTessEndPolygon(tess);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_COMBINE_DATA, null);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX_DATA, null);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_EDGE_FLAG_DATA, null);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_ERROR_DATA, null);

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

    protected void addEdge(RenderContext rc, Position begin, Position end) {
        // Compute the segment's constant properties - the segment azimuth and the segment length.
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

        // Compute the shape's local Cartesian coordinate origin.
        if (Double.isNaN(this.vertexOrigin.x)) {
            rc.geographicToCartesian(begin.latitude, begin.longitude, begin.altitude, this.altitudeMode, this.vertexOrigin);
        }

        // Explicitly add the first vertex to ensure alignment with adjacent segments.
        this.addVertex(rc, begin.latitude, begin.longitude, begin.altitude, VERTEX_USER /*type*/);

        // Add additional segment vertices when specified, and the segment length is nonzero.
        if (this.numSubsegments > 0 && length > NEAR_ZERO_THRESHOLD) {
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

                this.addVertex(rc, this.loc.latitude, this.loc.longitude, alt, VERTEX_TESSELATED /*type*/);

                dist += deltaDist;
                alt += deltaAlt;
            }
        }

        // Explicitly add the last vertex to ensure alignment with adjacent segments.
        this.addVertex(rc, end.latitude, end.longitude, end.altitude, VERTEX_USER /*type*/);
    }

    protected int addVertex(RenderContext rc, double latitude, double longitude, double altitude, int type) {
        if (this.isSurfaceShape) {
            return this.addVertexGeographic(rc, latitude, longitude, altitude, type);
        } else {
            return this.addVertexCartesian(rc, latitude, longitude, altitude, type);
        }
    }

    protected int addVertexGeographic(RenderContext rc, double latitude, double longitude, double altitude, int type) {
        int vertex = this.vertexArray.size() / 2;
        this.vertexArray.add((float) longitude);
        this.vertexArray.add((float) latitude);

        if (type != VERTEX_COMBINED) {
            this.tessCoords[0] = (float) longitude;
            this.tessCoords[1] = (float) latitude;
            this.tessCoords[2] = (float) altitude;
            GLU.gluTessVertex(rc.getTessellator(), this.tessCoords, 0 /*coords_offset*/, vertex);
        }

        return vertex;
    }

    protected int addVertexCartesian(RenderContext rc, double latitude, double longitude, double altitude, int type) {
        rc.geographicToCartesian(latitude, longitude, altitude, this.altitudeMode, this.point);
        int vertex = this.vertexArray.size() / 3;
        this.vertexArray.add((float) (this.point.x - this.vertexOrigin.x));
        this.vertexArray.add((float) (this.point.y - this.vertexOrigin.y));
        this.vertexArray.add((float) (this.point.z - this.vertexOrigin.z));

        if (this.extrude) {
            rc.geographicToCartesian(latitude, longitude, 0, WorldWind.CLAMP_TO_GROUND, this.point);
            this.vertexArray.add((float) (this.point.x - this.vertexOrigin.x));
            this.vertexArray.add((float) (this.point.y - this.vertexOrigin.y));
            this.vertexArray.add((float) (this.point.z - this.vertexOrigin.z));
        }

        if (this.extrude && type == VERTEX_USER) {
            this.verticalElements.add((short) vertex);
            this.verticalElements.add((short) (vertex + 1));
        }

        if (type != VERTEX_COMBINED) {
            this.tessCoords[0] = (float) longitude;
            this.tessCoords[1] = (float) latitude;
            this.tessCoords[2] = (float) altitude;
            GLU.gluTessVertex(rc.getTessellator(), this.tessCoords, 0 /*coords_offset*/, vertex);
        }

        return vertex;
    }

    protected void tessCombine(RenderContext rc, double[] coords, Object[] data, float[] weight, Object[] outData) {
        int vertex = this.addVertex(rc, coords[1] /*lat*/, coords[0] /*lon*/, coords[2] /*alt*/, VERTEX_COMBINED /*type*/);
        outData[0] = vertex;
    }

    protected void tessVertex(RenderContext rc, Object vertexData) {
        this.tessVertices[this.tessVertexCount] = (int) vertexData;
        this.tessEdgeFlags[this.tessVertexCount] = this.tessEdgeFlag;

        if (this.tessVertexCount < 2) {
            this.tessVertexCount++; // increment the vertex count and wait for more vertices
            return;
        } else {
            this.tessVertexCount = 0; // reset the vertex count and process one triangle
        }

        int v0 = this.tessVertices[0];
        int v1 = this.tessVertices[1];
        int v2 = this.tessVertices[2];

        this.interiorElements.add((short) v0).add((short) v1).add((short) v2);

        if (this.tessEdgeFlags[0] && this.extrude && !this.isSurfaceShape) {
            this.interiorElements.add((short) v0).add((short) (v0 + 1)).add((short) v1);
            this.interiorElements.add((short) v1).add((short) (v0 + 1)).add((short) (v1 + 1));
        }
        if (this.tessEdgeFlags[1] && this.extrude && !this.isSurfaceShape) {
            this.interiorElements.add((short) v1).add((short) (v1 + 1)).add((short) v2);
            this.interiorElements.add((short) v2).add((short) (v1 + 1)).add((short) (v2 + 1));
        }
        if (this.tessEdgeFlags[2] && this.extrude && !this.isSurfaceShape) {
            this.interiorElements.add((short) v2).add((short) (v2 + 1)).add((short) v0);
            this.interiorElements.add((short) v0).add((short) (v2 + 1)).add((short) (v0 + 1));
        }

        if (this.tessEdgeFlags[0]) {
            this.outlineElements.add((short) v0);
            this.outlineElements.add((short) v1);
        }
        if (this.tessEdgeFlags[1]) {
            this.outlineElements.add((short) v1);
            this.outlineElements.add((short) v2);
        }
        if (this.tessEdgeFlags[2]) {
            this.outlineElements.add((short) v2);
            this.outlineElements.add((short) v0);
        }
    }

    protected void tessEdgeFlag(RenderContext rc, boolean boundaryEdge) {
        this.tessEdgeFlag = boundaryEdge;
    }

    protected void tessError(RenderContext rc, int errnum) {
        String errstr = GLU.gluErrorString(errnum);
        Logger.logMessage(Logger.WARN, "Polygon", "assembleGeometry", "Error attempting to tessellate polygon \'" + errstr + "\'");
    }
}
