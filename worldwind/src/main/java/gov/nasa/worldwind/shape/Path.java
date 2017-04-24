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
import gov.nasa.worldwind.geom.Matrix3;
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

public class Path extends AbstractShape {

    protected static final int VERTEX_STRIDE = 4;

    protected static final ImageOptions defaultOutlineImageOptions = new ImageOptions();

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

    protected double texCoord1d;

    private Vec3 point = new Vec3();

    private Vec3 prevPoint = new Vec3();

    private Matrix3 texCoordMatrix = new Matrix3();

    private Location intermediateLocation = new Location();

    protected static Object nextCacheKey() {
        return new Object();
    }

    static {
        defaultOutlineImageOptions.resamplingMode = WorldWind.NEAREST_NEIGHBOR;
        defaultOutlineImageOptions.wrapMode = WorldWind.REPEAT;
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

        // Obtain a drawable form the render context pool, and compute distance to the render camera.
        Drawable drawable;
        DrawShapeState drawState;
        double cameraDistance;
        if (this.isSurfaceShape) {
            Pool<DrawableSurfaceShape> pool = rc.getDrawablePool(DrawableSurfaceShape.class);
            drawable = DrawableSurfaceShape.obtain(pool);
            drawState = ((DrawableSurfaceShape) drawable).drawState;
            cameraDistance = this.cameraDistanceGeographic(rc, this.boundingSector);
            ((DrawableSurfaceShape) drawable).sector.set(this.boundingSector);
        } else {
            Pool<DrawableShape> pool = rc.getDrawablePool(DrawableShape.class);
            drawable = DrawableShape.obtain(pool);
            drawState = ((DrawableShape) drawable).drawState;
            cameraDistance = this.cameraDistanceCartesian(rc, this.vertexArray.array(), this.vertexArray.size(), VERTEX_STRIDE, this.vertexOrigin);
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

        // Configure the drawable's vertex texture coordinate attribute.
        drawState.texCoordAttrib(1 /*size*/, 12 /*stride in bytes*/);

        // Configure the drawable to use the outline texture when drawing the outline.
        if (this.activeAttributes.drawOutline && this.activeAttributes.outlineImageSource != null) {
            Texture texture = rc.getTexture(this.activeAttributes.outlineImageSource);
            if (texture == null) {
                texture = rc.retrieveTexture(this.activeAttributes.outlineImageSource, defaultOutlineImageOptions);
            }
            if (texture != null) {
                double metersPerPixel = rc.pixelSizeAtDistance(cameraDistance);
                Matrix3 texCoordMatrix = this.texCoordMatrix.setToIdentity();
                texCoordMatrix.setScale(1.0 / (texture.getWidth() * metersPerPixel), 1.0);
                texCoordMatrix.multiplyByMatrix(texture.getTexCoordTransform());
                drawState.texture(texture);
                drawState.texCoordMatrix(texCoordMatrix);
            }
        }

        // Configure the drawable to display the shape's outline. Increase surface shape line widths by 1/2 pixel. Lines
        // drawn indirectly offscreen framebuffer appear thinner when sampled as a texture.
        if (this.activeAttributes.drawOutline) {
            drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
            drawState.lineWidth(this.isSurfaceShape ? this.activeAttributes.outlineWidth + 0.5f : this.activeAttributes.outlineWidth);
            drawState.drawElements(GLES20.GL_LINE_STRIP, this.outlineElements.size(),
                GLES20.GL_UNSIGNED_SHORT, this.interiorElements.size() * 2);
        }

        // Disable texturing for the remaining drawable primitives.
        drawState.texture(null);

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
        drawState.vertexStride = VERTEX_STRIDE * 4; // stride in bytes
        drawState.enableCullFace = false;
        drawState.enableDepthTest = this.activeAttributes.depthTest;

        // Enqueue the drawable for processing on the OpenGL thread.
        if (this.isSurfaceShape) {
            rc.offerSurfaceDrawable(drawable, 0 /*zOrder*/);
        } else {
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

        // Add the first vertex.
        Position begin = this.positions.get(0);
        this.addVertex(rc, begin.latitude, begin.longitude, begin.altitude, false /*intermediate*/);

        // Add the remaining vertices, inserting vertices along each edge as indicated by the path's properties.
        for (int idx = 1, len = this.positions.size(); idx < len; idx++) {
            Position end = this.positions.get(idx);
            this.addIntermediateVertices(rc, begin, end);
            this.addVertex(rc, end.latitude, end.longitude, end.altitude, false /*intermediate*/);
            begin = end;
        }

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

            this.addVertex(rc, loc.latitude, loc.longitude, alt, true /*intermediate*/);
            dist += deltaDist;
            alt += deltaAlt;
        }
    }

    protected void addVertex(RenderContext rc, double latitude, double longitude, double altitude, boolean intermediate) {
        int vertex = this.vertexArray.size() / VERTEX_STRIDE;
        Vec3 point = rc.geographicToCartesian(latitude, longitude, altitude, this.altitudeMode, this.point);

        if (this.vertexArray.size() == 0) {
            this.vertexOrigin.set(point);
            this.prevPoint.set(point);
            this.texCoord1d = 0;
        } else {
            this.texCoord1d += point.distanceTo(this.prevPoint);
            this.prevPoint.set(point);
        }

        if (this.isSurfaceShape) {
            this.vertexArray.add((float) longitude);
            this.vertexArray.add((float) latitude);
            this.vertexArray.add((float) altitude);
            this.vertexArray.add((float) this.texCoord1d);
            this.outlineElements.add((short) vertex);
        } else {
            this.vertexArray.add((float) (point.x - this.vertexOrigin.x));
            this.vertexArray.add((float) (point.y - this.vertexOrigin.y));
            this.vertexArray.add((float) (point.z - this.vertexOrigin.z));
            this.vertexArray.add((float) this.texCoord1d);
            this.outlineElements.add((short) vertex);

            if (this.extrude) {
                point = rc.geographicToCartesian(latitude, longitude, 0, this.altitudeMode, this.point);

                this.vertexArray.add((float) (point.x - this.vertexOrigin.x));
                this.vertexArray.add((float) (point.y - this.vertexOrigin.y));
                this.vertexArray.add((float) (point.z - this.vertexOrigin.z));
                this.vertexArray.add((float) 0 /*unused*/);
                this.interiorElements.add((short) vertex);
                this.interiorElements.add((short) (vertex + 1));
            }

            if (this.extrude && !intermediate) {
                this.verticalElements.add((short) vertex);
                this.verticalElements.add((short) (vertex + 1));
            }
        }
    }
}
