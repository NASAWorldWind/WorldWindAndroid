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
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.ImageOptions;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.FloatArray;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.ShortArray;
import gov.nasa.worldwind.util.glu.GLU;
import gov.nasa.worldwind.util.glu.GLUtessellator;
import gov.nasa.worldwind.util.glu.GLUtessellatorCallbackAdapter;

public class Polygon extends AbstractShape {

    protected static final int VERTEX_STRIDE = 6;

    protected static final ImageOptions defaultInteriorImageOptions = new ImageOptions();

    protected static final ImageOptions defaultOutlineImageOptions = new ImageOptions();

    protected List<List<Position>> boundaries = new ArrayList<>();

    protected boolean extrude;

    protected boolean followTerrain;

    protected FloatArray vertexArray = new FloatArray();

    protected ShortArray topElements = new ShortArray();

    protected ShortArray sideElements = new ShortArray();

    protected ShortArray outlineElements = new ShortArray();

    protected ShortArray verticalElements = new ShortArray();

    protected Object vertexBufferKey = nextCacheKey();

    protected Object elementBufferKey = nextCacheKey();

    protected Vec3 vertexOrigin = new Vec3();

    protected boolean isSurfaceShape;

    protected double cameraDistance;

    protected double texCoord1d;

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

    protected static final int VERTEX_ORIGINAL = 0;

    protected static final int VERTEX_INTERMEDIATE = 1;

    protected static final int VERTEX_COMBINED = 2;

    private Vec3 point = new Vec3();

    private Vec3 prevPoint = new Vec3();

    private Vec3 texCoord2d = new Vec3();

    private Matrix3 texCoordMatrix = new Matrix3();

    private Matrix4 modelToLocal = new Matrix4();

    private Location intermediateLocation = new Location();

    private double[] tessCoords = new double[3];

    private int[] tessVertices = new int[3];

    private boolean[] tessEdgeFlags = new boolean[3];

    private boolean tessEdgeFlag = true;

    private int tessVertexCount;

    protected static Object nextCacheKey() {
        return new Object();
    }

    static {
        defaultInteriorImageOptions.wrapMode = WorldWind.REPEAT;
        defaultOutlineImageOptions.resamplingMode = WorldWind.NEAREST_NEIGHBOR;
        defaultOutlineImageOptions.wrapMode = WorldWind.REPEAT;
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

        this.reset();

        return this.boundaries.set(index, positions);
    }

    public void addBoundary(List<Position> positions) {
        if (positions == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "addBoundary", "missingList"));
        }

        this.boundaries.add(positions);
        this.reset();
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
        this.reset();
    }

    public List<Position> removeBoundary(int index) {
        if (index < 0 || index >= this.boundaries.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Polygon", "removeBoundary", "invalidIndex"));
        }

        this.reset();

        return this.boundaries.remove(index);
    }

    public void clearBoundaries() {
        this.boundaries.clear();
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
        this.topElements.clear();
        this.sideElements.clear();
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
            this.cameraDistance = this.cameraDistanceGeographic(rc, this.boundingSector);
            ((DrawableSurfaceShape) drawable).sector.set(this.boundingSector);
        } else {
            Pool<DrawableShape> pool = rc.getDrawablePool(DrawableShape.class);
            drawable = DrawableShape.obtain(pool);
            drawState = ((DrawableShape) drawable).drawState;
            this.cameraDistance = this.cameraDistanceCartesian(rc, this.vertexArray.array(), this.vertexArray.size(), VERTEX_STRIDE, this.vertexOrigin);
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
            int size = (this.topElements.size() * 2) + (this.sideElements.size() * 2) + (this.outlineElements.size() * 2) + (this.verticalElements.size() * 2);
            ShortBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();
            buffer.put(this.topElements.array(), 0, this.topElements.size());
            buffer.put(this.sideElements.array(), 0, this.sideElements.size());
            buffer.put(this.outlineElements.array(), 0, this.outlineElements.size());
            buffer.put(this.verticalElements.array(), 0, this.verticalElements.size());
            drawState.elementBuffer = new BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind());
            rc.putBufferObject(this.elementBufferKey, drawState.elementBuffer);
        }

        if (this.isSurfaceShape || this.activeAttributes.interiorColor.alpha >= 1.0) {
            this.drawInterior(rc, drawState);
            this.drawOutline(rc, drawState);
        } else {
            this.drawOutline(rc, drawState);
            this.drawInterior(rc, drawState);
        }

        // Configure the drawable according to the shape's attributes. Disable triangle backface culling when we're
        // displaying a polygon without extruded sides, so we want to draw the top and the bottom.
        drawState.vertexOrigin.set(this.vertexOrigin);
        drawState.vertexStride = VERTEX_STRIDE * 4; // stride in bytes
        drawState.enableCullFace = this.extrude;
        drawState.enableDepthTest = this.activeAttributes.depthTest;

        // Enqueue the drawable for processing on the OpenGL thread.
        if (this.isSurfaceShape) {
            rc.offerSurfaceDrawable(drawable, 0 /*zOrder*/);
        } else {
            rc.offerShapeDrawable(drawable, this.cameraDistance);
        }
    }

    protected void drawInterior(RenderContext rc, DrawShapeState drawState) {
        if (!this.activeAttributes.drawInterior) {
            return;
        }

        // Configure the drawable to use the interior texture when drawing the interior.
        if (this.activeAttributes.interiorImageSource != null) {
            Texture texture = rc.getTexture(this.activeAttributes.interiorImageSource);
            if (texture == null) {
                texture = rc.retrieveTexture(this.activeAttributes.interiorImageSource, defaultInteriorImageOptions);
            }
            if (texture != null) {
                double metersPerPixel = rc.pixelSizeAtDistance(this.cameraDistance);
                Matrix3 texCoordMatrix = this.texCoordMatrix.setToIdentity();
                texCoordMatrix.setScale(
                    1.0 / (texture.getWidth() * metersPerPixel),
                    1.0 / (texture.getHeight() * metersPerPixel));
                texCoordMatrix.multiplyByMatrix(texture.getTexCoordTransform());
                drawState.texture(texture);
                drawState.texCoordMatrix(texCoordMatrix);
            }
        } else {
            drawState.texture(null);
        }

        // Configure the drawable to display the shape's interior top.
        drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.interiorColor);
        drawState.texCoordAttrib(2 /*size*/, 12 /*offset in bytes*/);
        drawState.drawElements(GLES20.GL_TRIANGLES, this.topElements.size(),
            GLES20.GL_UNSIGNED_SHORT, 0 /*offset*/);

        // Configure the drawable to display the shape's interior sides.
        if (this.extrude) {
            drawState.texture(null);
            drawState.drawElements(GLES20.GL_TRIANGLES, this.sideElements.size(),
                GLES20.GL_UNSIGNED_SHORT, this.topElements.size() * 2 /*offset*/);
        }
    }

    protected void drawOutline(RenderContext rc, DrawShapeState drawState) {
        if (!this.activeAttributes.drawOutline) {
            return;
        }

        // Configure the drawable to use the outline texture when drawing the outline.
        if (this.activeAttributes.outlineImageSource != null) {
            Texture texture = rc.getTexture(this.activeAttributes.outlineImageSource);
            if (texture == null) {
                texture = rc.retrieveTexture(this.activeAttributes.outlineImageSource, defaultOutlineImageOptions);
            }
            if (texture != null) {
                double metersPerPixel = rc.pixelSizeAtDistance(this.cameraDistance);
                Matrix3 texCoordMatrix = this.texCoordMatrix.setToIdentity();
                texCoordMatrix.setScale(1.0 / (texture.getWidth() * metersPerPixel), 1.0);
                texCoordMatrix.multiplyByMatrix(texture.getTexCoordTransform());
                drawState.texture(texture);
                drawState.texCoordMatrix(texCoordMatrix);
            }
        } else {
            drawState.texture(null);
        }

        // Configure the drawable to display the shape's outline.
        drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
        drawState.lineWidth(this.activeAttributes.outlineWidth);
        drawState.texCoordAttrib(1 /*size*/, 20 /*offset in bytes*/);
        drawState.drawElements(GLES20.GL_LINES, this.outlineElements.size(),
            GLES20.GL_UNSIGNED_SHORT, (this.topElements.size() * 2) + (this.sideElements.size() * 2) /*offset*/);

        // Configure the drawable to display the shape's extruded verticals.
        if (this.activeAttributes.drawVerticals && this.extrude) {
            drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
            drawState.lineWidth(this.activeAttributes.outlineWidth);
            drawState.texture(null);
            drawState.drawElements(GLES20.GL_LINES, this.verticalElements.size(),
                GLES20.GL_UNSIGNED_SHORT, (this.topElements.size() * 2) + (this.sideElements.size() * 2) + (this.outlineElements.size() * 2) /*offset*/);
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
        this.topElements.clear();
        this.sideElements.clear();
        this.outlineElements.clear();
        this.verticalElements.clear();

        this.determineShapeOrigin(rc);

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
                continue; // no boundary positions to assemble
            }

            GLU.gluTessBeginContour(tess);

            // Add the boundary's first vertex.
            Position begin = positions.get(0);
            rc.geographicToCartesian(begin.latitude, begin.longitude, begin.altitude, this.altitudeMode, this.prevPoint);
            this.texCoord1d = 0;
            this.addVertex(rc, begin.latitude, begin.longitude, begin.altitude, VERTEX_ORIGINAL /*type*/);

            // Add the remaining boundary vertices, tessellating each edge as indicated by the polygon's properties.
            for (int idx = 1, len = positions.size(); idx < len; idx++) {
                Position end = positions.get(idx);
                this.addIntermediateVertices(rc, begin, end);
                this.addVertex(rc, end.latitude, end.longitude, end.altitude, VERTEX_ORIGINAL /*type*/);
                begin = end;
            }

            // Tessellate the implicit closing edge if the boundary is not already closed.
            if (!begin.equals(positions.get(0))) {
                this.addIntermediateVertices(rc, begin, positions.get(0));
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
            this.boundingSector.union(this.vertexArray.array(), this.vertexArray.size(), VERTEX_STRIDE);
            this.boundingBox.setToUnitBox(); // Surface/geographic shape bounding box is unused
        } else {
            this.boundingBox.setToPoints(this.vertexArray.array(), this.vertexArray.size(), VERTEX_STRIDE);
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
            Location loc = this.intermediateLocation;

            if (this.pathType == WorldWind.GREAT_CIRCLE) {
                begin.greatCircleLocation(azimuth, dist, loc);
            } else if (this.pathType == WorldWind.RHUMB_LINE) {
                begin.rhumbLocation(azimuth, dist, loc);
            }

            this.addVertex(rc, loc.latitude, loc.longitude, alt, VERTEX_INTERMEDIATE /*type*/);
            dist += deltaDist;
            alt += deltaAlt;
        }
    }

    protected int addVertex(RenderContext rc, double latitude, double longitude, double altitude, int type) {
        int vertex = this.vertexArray.size() / VERTEX_STRIDE;
        Vec3 point = rc.geographicToCartesian(latitude, longitude, altitude, this.altitudeMode, this.point);
        Vec3 texCoord2d = this.texCoord2d.set(point).multiplyByMatrix(this.modelToLocal);

        if (type != VERTEX_COMBINED) {
            this.tessCoords[0] = (float) longitude;
            this.tessCoords[1] = (float) latitude;
            this.tessCoords[2] = (float) altitude;
            GLU.gluTessVertex(rc.getTessellator(), this.tessCoords, 0 /*coords_offset*/, vertex);
        }

        this.texCoord1d += point.distanceTo(this.prevPoint);
        this.prevPoint.set(point);

        if (this.isSurfaceShape) {
            this.vertexArray.add((float) longitude);
            this.vertexArray.add((float) latitude);
            this.vertexArray.add((float) altitude);
            this.vertexArray.add((float) texCoord2d.x);
            this.vertexArray.add((float) texCoord2d.y);
            this.vertexArray.add((float) this.texCoord1d);
        } else {
            point = rc.geographicToCartesian(latitude, longitude, altitude, this.altitudeMode, this.point);
            this.vertexArray.add((float) (point.x - this.vertexOrigin.x));
            this.vertexArray.add((float) (point.y - this.vertexOrigin.y));
            this.vertexArray.add((float) (point.z - this.vertexOrigin.z));
            this.vertexArray.add((float) texCoord2d.x);
            this.vertexArray.add((float) texCoord2d.y);
            this.vertexArray.add((float) this.texCoord1d);

            if (this.extrude) {
                point = rc.geographicToCartesian(latitude, longitude, 0, WorldWind.CLAMP_TO_GROUND, this.point);
                this.vertexArray.add((float) (point.x - this.vertexOrigin.x));
                this.vertexArray.add((float) (point.y - this.vertexOrigin.y));
                this.vertexArray.add((float) (point.z - this.vertexOrigin.z));
                this.vertexArray.add((float) 0 /*unused*/);
                this.vertexArray.add((float) 0 /*unused*/);
                this.vertexArray.add((float) 0 /*unused*/);

                if (type == VERTEX_ORIGINAL) {
                    this.verticalElements.add((short) vertex);
                    this.verticalElements.add((short) (vertex + 1));
                }
            }
        }

        return vertex;
    }

    protected void determineShapeOrigin(RenderContext rc) {
        double mx = 0, my = 0, mz = 0;
        double numPoints = 0;

        for (int boundaryIdx = 0, boundaryCount = this.boundaries.size(); boundaryIdx < boundaryCount; boundaryIdx++) {

            List<Position> positions = this.boundaries.get(boundaryIdx);
            if (positions.isEmpty()) {
                continue; // no boundary positions
            }

            for (int idx = 0, len = positions.size(); idx < len; idx++) {
                Position pos = positions.get(idx);
                Vec3 point = rc.geographicToCartesian(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE, this.point);
                mx += point.x;
                my += point.y;
                mz += point.z;
                numPoints++;
            }
        }

        mx /= numPoints;
        my /= numPoints;
        mz /= numPoints;

        this.vertexOrigin.set(mx, my, mz);
        this.modelToLocal = rc.globe.cartesianToLocalTransform(mx, my, mz, this.modelToLocal).invertOrthonormal();
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

        this.topElements.add((short) v0).add((short) v1).add((short) v2);

        if (this.tessEdgeFlags[0] && this.extrude && !this.isSurfaceShape) {
            this.sideElements.add((short) v0).add((short) (v0 + 1)).add((short) v1);
            this.sideElements.add((short) v1).add((short) (v0 + 1)).add((short) (v1 + 1));
        }
        if (this.tessEdgeFlags[1] && this.extrude && !this.isSurfaceShape) {
            this.sideElements.add((short) v1).add((short) (v1 + 1)).add((short) v2);
            this.sideElements.add((short) v2).add((short) (v1 + 1)).add((short) (v2 + 1));
        }
        if (this.tessEdgeFlags[2] && this.extrude && !this.isSurfaceShape) {
            this.sideElements.add((short) v2).add((short) (v2 + 1)).add((short) v0);
            this.sideElements.add((short) v0).add((short) (v2 + 1)).add((short) (v0 + 1));
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
