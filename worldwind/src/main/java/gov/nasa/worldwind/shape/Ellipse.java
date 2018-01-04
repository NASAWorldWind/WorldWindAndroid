/*
 * Copyright (c) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import gov.nasa.worldwind.draw.DrawShapeState;
import gov.nasa.worldwind.draw.DrawableSurfaceShape;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.FloatArray;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.ShortArray;

public class Ellipse extends AbstractShape {

    protected static final int VERTEX_STRIDE = 6;

    protected Location center;

    protected double minorRadius;

    protected double majorRadius;

    protected double headingDegrees;

    protected int intervals = 64;

    protected FloatArray vertexArray = new FloatArray();

    protected ShortArray interiorElements = new ShortArray();

    protected ShortArray outlineElements = new ShortArray();

    protected Object vertexBufferKey = nextCacheKey();

    protected Object elementBufferKey = nextCacheKey();

    protected Vec3 vertexOrigin = new Vec3();

    private static final Location SCRATCH = new Location();

    protected static Object nextCacheKey() {
        return new Object();
    }

    public Ellipse() {
    }

    public Ellipse(ShapeAttributes attributes) {
        super(attributes);
    }

    public Ellipse(double latitude, double longitude, double majorRadius, double minorRadius, double headingDegrees) {
        this(latitude, longitude, majorRadius, minorRadius, headingDegrees, null);
    }

    public Ellipse(double latitude, double longitude, double majorRadius, double minorRadius, double headingDegrees, ShapeAttributes attributes) {
        super(attributes);
        if (latitude > 90 || latitude < -90) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "constructor", "invalid latitude"));
        }

        if (longitude > 180 || longitude < -180) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "constructor", "invalid longitude"));
        }

        if (minorRadius > majorRadius) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "constructor", "minor radius must be smaller than major radius"));
        }

        this.center = new Location(latitude, longitude);
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
        this.headingDegrees = headingDegrees;
    }

    public Location getCenter() {
        return center;
    }

    public void setCenter(double latitude, double longitude) {
        if (this.center == null) {
            this.center = new Location(latitude, longitude);
        } else {
            this.center.set(latitude, longitude);
        }
        this.reset();
    }

    public double getMinorRadius() {
        return minorRadius;
    }

    public void setMinorRadius(double minorRadius) {
        if (minorRadius > this.majorRadius) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "setMinorRadius", "minor radius must be smaller than major radius"));
        }
        this.minorRadius = minorRadius;
        this.reset();
    }

    public double getMajorRadius() {
        return majorRadius;
    }

    public void setMajorRadius(double majorRadius) {
        if (majorRadius < this.minorRadius) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "setMajorRadius", "minor radius must be smaller than major radius"));
        }
        this.majorRadius = majorRadius;
        this.reset();
    }

    public double getHeadingDegrees() {
        return headingDegrees;
    }

    public void setHeadingDegrees(double headingDegrees) {
        this.headingDegrees = headingDegrees;
        this.reset();
    }

    protected void reset() {
        this.vertexArray.clear();
        this.interiorElements.clear();
        this.outlineElements.clear();
    }

    @Override
    protected void makeDrawable(RenderContext rc) {
        if (this.center == null || this.minorRadius == 0 || this.majorRadius == 0) {
            return; // nothing to draw
        }

        if (this.mustAssembleGeometry(rc)) {
            this.assembleGeometry(rc);
            this.assembleElements(rc);
            this.vertexBufferKey = nextCacheKey();
            this.elementBufferKey = nextCacheKey();
        }

        // Obtain a drawable form the render context pool.
        DrawableSurfaceShape drawable;
        DrawShapeState drawState;
        Pool<DrawableSurfaceShape> pool = rc.getDrawablePool(DrawableSurfaceShape.class);
        drawable = DrawableSurfaceShape.obtain(pool);
        drawState = drawable.drawState;
        drawable.sector.set(this.boundingSector);


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
            int size = (this.interiorElements.size() * 2) + (this.outlineElements.size() * 2);
            ShortBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();
            buffer.put(this.interiorElements.array(), 0, this.interiorElements.size());
            buffer.put(this.outlineElements.array(), 0, this.outlineElements.size());
            drawState.elementBuffer = new BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind());
            rc.putBufferObject(this.elementBufferKey, drawState.elementBuffer);
        }

        this.drawInterior(rc, drawState);
        this.drawOutline(rc, drawState);

        // Configure the drawable according to the shape's attributes. Disable triangle backface culling when we're
        // displaying a polygon without extruded sides, so we want to draw the top and the bottom.
        drawState.vertexOrigin.set(this.vertexOrigin);
        drawState.vertexStride = VERTEX_STRIDE * 4; // stride in bytes
        drawState.enableCullFace = false;
        drawState.enableDepthTest = this.activeAttributes.depthTest;

        // Enqueue the drawable for processing on the OpenGL thread.
        rc.offerSurfaceDrawable(drawable, 0 /*zOrder*/);
    }

    protected void drawInterior(RenderContext rc, DrawShapeState drawState) {
        if (!this.activeAttributes.drawInterior) {
            return;
        }

        drawState.texture(null);

        // Configure the drawable to display the shape's interior.
        drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.interiorColor);
        drawState.texCoordAttrib(2 /*size*/, 12 /*offset in bytes*/);
        drawState.drawElements(GLES20.GL_TRIANGLE_STRIP, this.interiorElements.size(),
            GLES20.GL_UNSIGNED_SHORT, 0 /*offset*/);
    }

    protected void drawOutline(RenderContext rc, DrawShapeState drawState) {
        if (!this.activeAttributes.drawOutline) {
            return;
        }

        drawState.texture(null);

        // Configure the drawable to display the shape's outline.
        drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
        drawState.lineWidth(this.activeAttributes.outlineWidth);
        drawState.texCoordAttrib(1 /*size*/, 20 /*offset in bytes*/);
        drawState.drawElements(GLES20.GL_LINE_LOOP, this.outlineElements.size(),
            GLES20.GL_UNSIGNED_SHORT, this.interiorElements.size() * 2 /*offset*/);
    }

    protected boolean mustAssembleGeometry(RenderContext rc) {
        return this.vertexArray.size() == 0;
    }

    protected void assembleGeometry(RenderContext rc) {

        // Clear the shape's vertex array. This array will accumulate values as the shapes's
        // geometry is assembled.
        this.vertexArray.clear();

        // Determine the number of spine points and construct radius value holding array
        int spinePoints = this.intervals / 2 - 1; // intervals must be even
        int spineIdx = 0;
        double[] spineRadius = new double[spinePoints];

        // Vertex generation begins on the positive major axis and works ccs around the ellipse. The spine points are
        // then appended from positive major axis to negative major axis. The radians value does not align with a
        // geographical "heading"
        double deltaRadians = 2 * Math.PI / this.intervals;
        double majorArcRadians = this.majorRadius / rc.globe.getRadiusAt(this.center.latitude, this.center.longitude);
        double minorArcRadians = this.minorRadius / rc.globe.getRadiusAt(this.center.latitude, this.center.longitude);
        for (int i = 0; i < this.intervals; i++) {
            double radians = deltaRadians * i;
            double x = Math.cos(radians) * majorArcRadians;
            double y = Math.sin(radians) * minorArcRadians;
            double azimuthDegrees = Math.toDegrees(-Math.atan2(y, x));
            double arcRadius = Math.sqrt(x * x + y * y);
            // Calculate the great circle location given this intervals step (azimuthDegrees) a correction value to
            // start from an east-west aligned major axis (90.0) and the user specified user heading value
            this.center.greatCircleLocation(azimuthDegrees + 90.0 + this.headingDegrees, arcRadius, SCRATCH);
            this.addVertex(rc, SCRATCH.latitude, SCRATCH.longitude, 0);
            // Add the major arc radius for the spine points. Spine points are vertically coincident with exterior
            // points. The first and middle most point do not have corresponding spine points.
            if (i > 0 && i < this.intervals / 2) {
                spineRadius[spineIdx++] = x;
            }
        }

        // Add the interior spine point vertices
        for (int i = 0; i < spinePoints; i++) {
            this.center.greatCircleLocation(0 + 90.0 + this.headingDegrees, spineRadius[i], SCRATCH);
            this.addVertex(rc, SCRATCH.latitude, SCRATCH.longitude, 0);
        }


        // Compute the shape's bounding box or bounding sector from its assembled coordinates.
        this.boundingSector.setEmpty();
        this.boundingSector.union(this.vertexArray.array(), this.vertexArray.size(), VERTEX_STRIDE);
        this.boundingBox.setToUnitBox(); // Surface/geographic shape bounding box is unused
    }

    protected void assembleElements(RenderContext rc) {
        // Generate the interior element buffer with spine
        int interiorIdx = this.intervals;
        // Add the anchor leg
        this.interiorElements.add((short) 0);
        this.interiorElements.add((short) 1);
        // Tessellate the interior
        for (int i = 2; i < this.intervals; i++) {
            // Add the corresponding interior spine point if this isn't the vertex following the last vertex for the
            // negative major axis
            if (i != (this.intervals / 2 + 1)) {
                if (i > this.intervals / 2) {
                    this.interiorElements.add((short) --interiorIdx);
                } else {
                    this.interiorElements.add((short) interiorIdx++);
                }
            }
            // Add the degenerate triangle at the negative major axis in order to flip the triangle strip back towards
            // the positive axis
            if (i == this.intervals / 2) {
                this.interiorElements.add((short) i);
            }
            // Add the exterior vertex
            this.interiorElements.add((short) i);
        }
        // Complete the strip
        this.interiorElements.add((short) --interiorIdx);
        this.interiorElements.add((short) 0);

        // Generate the outline element buffer
        for (int i = 0; i < this.intervals; i++) {
            this.outlineElements.add((short) i);
        }
    }

    protected void addVertex(RenderContext rc, double latitude, double longitude, double altitude) {
        this.vertexArray.add((float) longitude);
        this.vertexArray.add((float) latitude);
        this.vertexArray.add((float) altitude);
        // reserved for future texture coordinate use
        this.vertexArray.add(0);
        this.vertexArray.add(0);
        this.vertexArray.add(0);
    }
}
