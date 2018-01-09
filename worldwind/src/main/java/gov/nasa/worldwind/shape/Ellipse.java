/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
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
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.FloatArray;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.ShortArray;

/**
 * Ellipse shape defined by a geographic center position and radii for the semi-major and semi-minor axes.
 * <p>
 * <h3>Axes and Heading</h3>
 * <p>
 * Ellipse axes, by default, are oriented such that the semi-major axis points East and the semi-minor axis points
 * North. Ellipse provides an optional heading, which when set to anything other than 0.0 rotates the semi-major and
 * semi-minor axes about the center position, while retaining the axes relative relationship to one another. Heading is
 * defined in degrees clockwise from North. Configuring ellipse with a heading of 45.0 results in the semi-major axis
 * pointing Southeast and the semi-minor axis pointing Northeast.
 * <p>
 * <h3>Altitude Mode and Terrain Following</h3>
 * <p>
 * Ellipse geometry displays at a constant altitude determined by the geographic center position and altitude mode. For
 * example, an ellipse with a center position altitude of 1km and altitude mode of ABSOLUTE displays at 1km above mean
 * sea level. The same ellipse with an altitude mode of RELATIVE_TO_GROUND displays at 1km above ground level, relative
 * to the ellipse's center location.
 * <p>
 * Surface ellipse geometry, where an ellipse appears draped across the terrain, may be achieved by enabling ellipse's
 * terrain following state and setting its altitude mode to CLAMP_TO_GROUND. See {@link #setFollowTerrain(boolean)} and
 * {@link #setAltitudeMode(int)}.
 * <p>
 * <h3>Display Granularity</h3>
 * <p>
 * Ellipse's appearance on screen is composed of discrete segments which approximate the ellipse's geometry. This
 * approximation is chosen such that the display appears to be a continuous smooth ellipse. Applications can control the
 * maximum number of angular intervals used in this representation with {@link #setMaximumIntervals(int)}.
 */
public class Ellipse extends AbstractShape {

    protected static final int VERTEX_STRIDE = 6;

    protected static final int MIN_INTERVALS = 32;

    /**
     * The ellipse's geographic center position.
     */
    protected Position center;

    /**
     * The ellipse's radius perpendicular to it's heading, in meters.
     */
    protected double majorRadius;

    /**
     * The ellipse's radius parallel to it's heading, in meters.
     */
    protected double minorRadius;

    /**
     * The ellipse's heading in degrees clockwise from North.
     */
    protected double heading;

    /**
     * Determines whether this ellipse's geometry follows the terrain surface or is fixed at a constant altitude.
     */
    protected boolean followTerrain;

    /**
     * The maximum number of angular intervals that may be used to assemble the ellipse's geometry for rendering.
     */
    protected int maximumIntervals = 256;

    /**
     * The number of intervals used for generating geometry. Clamped between MIN_INTERVALS and maximumIntervals and
     * based on the circumference of the ellipse in planar geometry. Will always be even.
     */
    protected int intervals;

    protected FloatArray vertexArray = new FloatArray();

    protected ShortArray interiorElements = new ShortArray();

    protected ShortArray outlineElements = new ShortArray();

    protected Object vertexBufferKey = nextCacheKey();

    protected Object elementBufferKey = nextCacheKey();

    private static final Position SCRATCH = new Position();

    protected static Object nextCacheKey() {
        return new Object();
    }

    /**
     * Constructs an ellipse with a null center position, and with major- and minor-radius both 0.0. This ellipse does
     * not display until the center position is defined and the radii are both greater than 0.0.
     */
    public Ellipse() {
    }

    /**
     * Constructs an ellipse with a null center position, and with major- and minor-radius both 0.0. This ellipse does
     * not display until the center position is defined and the radii are both greater than 0.0.
     *
     * @param attributes the shape attributes applied to the ellipse
     */
    public Ellipse(ShapeAttributes attributes) {
        super(attributes);
    }

    /**
     * Constructs an ellipse with a specified center position and radii. The ellipse displays in the default shape
     * attributes, which may be specified using {@link #setAttributes(ShapeAttributes)}. The ellipse does not display if
     * the center position is null, or both radii are 0.0.
     *
     * @param center      geographic position at the ellipse's center. May be null.
     * @param majorRadius radius of the semi-major axis, in meters.
     * @param minorRadius radius of the semi-minor axis, in meters.
     *
     * @throws IllegalArgumentException If either radius is negative
     */
    public Ellipse(Position center, double majorRadius, double minorRadius) {
        if (majorRadius < 0 || minorRadius < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "constructor", "invalidRadius"));
        }

        if (center != null) {
            this.center = new Position(center);
        }

        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    /**
     * Constructs an ellipse with a specified center position, radii, and shape attributes. The ellipse displays in the
     * specified shape attributes, which may be modifies using {@link #setAttributes(ShapeAttributes)}. The ellipse does
     * not display if the center position is null, or both radii are 0.0.
     *
     * @param center      geographic position at the ellipse's center; may be null
     * @param majorRadius radius of the semi-major axis, in meters.
     * @param minorRadius radius of the semi-minor axis, in meters.
     * @param attributes  the shape attributes applied to the ellipse
     *
     * @throws IllegalArgumentException If either radius is negative
     */
    public Ellipse(Position center, double majorRadius, double minorRadius, ShapeAttributes attributes) {
        super(attributes);

        if (majorRadius < 0 || minorRadius < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "constructor", "invalidRadius"));
        }

        if (center != null) {
            this.center = new Position(center);
        }

        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    /**
     * Indicates the geographic position of this ellipse's center. The position may be null, in which case the ellipse
     * does not display.
     *
     * @return this ellipse's geographic center; may be null
     */
    public Position getCenter() {
        return this.center;
    }

    /**
     * Sets the geographic position of this ellipse's center. The position may be null, in which case the ellipse does
     * not display.
     *
     * @param position the new center position; may be null
     *
     * @return this ellipse with its center position set to the specified position
     */
    public Ellipse setCenter(Position position) {
        if (position == null) {
            this.center = null;
        } else if (this.center == null) {
            this.center = new Position(position);
        } else {
            this.center.set(position);
        }
        this.reset();

        return this;
    }

    /**
     * Indicates the radius of this globe's semi-major axis. When the ellipse's heading is 0.0, the semi-major axis
     * points East.
     *
     * @return the radius, in meters
     */
    public double getMajorRadius() {
        return this.majorRadius;
    }

    /**
     * Sets the radius of this globe's semi-major axis. When the ellipse's heading is 0.0, the semi-major axis points
     * East.
     *
     * @param radius the new radius, in meters
     *
     * @return this ellipse with the radius of its semi-major axis set to the specified value
     *
     * @throws IllegalArgumentException If the radius is negative
     */
    public Ellipse setMajorRadius(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "setMajorRadius", "invalidRadius"));
        }

        this.majorRadius = radius;
        this.reset();
        return this;
    }

    /**
     * Indicates the radius of this globe's semi-minor axis. When the ellipse's heading is 0.0, the semi-minor axis
     * points North.
     *
     * @return the radius, in meters
     */
    public double getMinorRadius() {
        return this.minorRadius;
    }

    /**
     * Sets the radius of this globe's semi-minor axis. When the ellipse's heading is 0.0, the semi-minor axis points
     * North.
     *
     * @param radius the new radius, in meters
     *
     * @return this ellipse with the radius of its semi-minor axis set to the specified value
     *
     * @throws IllegalArgumentException If the radius is negative
     */
    public Ellipse setMinorRadius(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "setMinorRadius", "invalidRadius"));
        }

        this.minorRadius = radius;
        this.reset();
        return this;
    }

    /**
     * Indicates this ellipse's heading. When ellipse's heading is 0.0, the
     * semi-major axis points East and the semi-minor axis points North. Headings other than 0.0 rotate the axes about
     * the ellipse's center position, while retaining the axes relative relationship to one another.
     *
     * @return this ellipse's heading, in degrees clockwise from North
     */
    public double getHeading() {
        return heading;
    }

    /**
     * Sets this ellipse's heading in degrees clockwise from North. When ellipse's heading is 0.0, the
     * semi-major axis points East and the semi-minor axis points North. Headings other than 0.0 rotate the axes about
     * the ellipse's center position, while retaining the axes relative relationship to one another.
     *
     * @param degrees the new heading, in degrees clockwise from North
     *
     * @return this ellipse, with its heading set to the specified value
     */
    public Ellipse setHeading(double degrees) {
        this.heading = degrees;
        this.reset();
        return this;
    }

    /**
     * Indicates whether this ellipse's geometry follows the terrain surface or is fixed at a constant altitude.
     *
     * @return true if ellipse geometry follows the terrain surface, and false otherwise
     */
    public boolean isFollowTerrain() {
        return this.followTerrain;
    }

    /**
     * Sets the terrain following state of this ellipse, which indicates whether this ellipse's geometry follows the
     * terrain surface or is fixed at a constant altitude. By default the terrain following state is false, and ellipse
     * geometry follows the constant altitude of its center position.
     *
     * @param followTerrain true to follow the terrain surface, and false otherwise
     *
     * @return this ellipse, with its terrain following state set to the specified value
     */
    public Ellipse setFollowTerrain(boolean followTerrain) {
        this.followTerrain = followTerrain;
        this.reset();
        return this;
    }

    /**
     * Indicates the maximum number of angular intervals that may be used to approximate this ellipse's geometry on
     * screen.
     *
     * @return the number of angular intervals
     */
    public int getMaximumIntervals() {
        return this.maximumIntervals;
    }

    /**
     * Sets the maximum number of angular intervals that may be used to approximate this ellipse's on screen.
     * <p>
     * Ellipse may use a minimum number of intervals to ensure that its appearance on screen at least roughly
     * approximates the ellipse's shape. When the specified number of intervals is too small, it is clamped to an
     * implementation-defined minimum number of intervals.
     * <p>
     * Ellipse may require that the number of intervals is an even multiple of some integer. When the specified number
     * of intervals does not meet this criteria, the next smallest integer that meets ellipse's criteria is used
     * instead.
     *
     * @param numIntervals the number of angular intervals
     *
     * @return this ellipse with its number of angular intervals set to the specified value
     *
     * @throws IllegalArgumentException If the number of intervals is negative
     */
    public Ellipse setMaximumIntervals(int numIntervals) {
        if (numIntervals < 0) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Ellipse", "setMaximumIntervals", "invalidNumIntervals"));
        }

        this.maximumIntervals = numIntervals;
        this.reset();
        return this;
    }

    @Override
    protected void makeDrawable(RenderContext rc) {
        if (this.center == null) {
            return; // nothing to draw
        }

        if (this.majorRadius == 0 && this.minorRadius == 0) {
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
        // Determine the number of intervals to use based on the circumference of the ellipse
        this.calculateIntervals();
        if (this.intervals % 2 != 0) {
            this.intervals--;
        }

        // Determine the number of spine points and construct radius value holding array
        int spinePoints = this.intervals / 2 - 1; // intervals must be even
        int spineIdx = 0;
        double[] spineRadius = new double[spinePoints];

        // Check if minor radius is less than major in which case we need to flip the definitions and change the phase
        boolean isStandardAxisOrientation = this.majorRadius > this.minorRadius;
        double headingAdjustment = isStandardAxisOrientation ? 90 : 0;

        // Vertex generation begins on the positive major axis and works ccs around the ellipse. The spine points are
        // then appended from positive major axis to negative major axis.
        double deltaRadians = 2 * Math.PI / this.intervals;
        double majorArcRadians, minorArcRadians;
        if (isStandardAxisOrientation) {
            majorArcRadians = this.majorRadius / rc.globe.getRadiusAt(this.center.latitude, this.center.longitude);
            minorArcRadians = this.minorRadius / rc.globe.getRadiusAt(this.center.latitude, this.center.longitude);
        } else {
            majorArcRadians = this.minorRadius / rc.globe.getRadiusAt(this.center.latitude, this.center.longitude);
            minorArcRadians = this.majorRadius / rc.globe.getRadiusAt(this.center.latitude, this.center.longitude);
        }

        for (int i = 0; i < this.intervals; i++) {
            double radians = deltaRadians * i;
            double x = Math.cos(radians) * majorArcRadians;
            double y = Math.sin(radians) * minorArcRadians;
            double azimuthDegrees = Math.toDegrees(-Math.atan2(y, x));
            double arcRadius = Math.sqrt(x * x + y * y);
            // Calculate the great circle location given this intervals step (azimuthDegrees) a correction value to
            // start from an east-west aligned major axis (90.0) and the user specified user heading value
            this.center.greatCircleLocation(azimuthDegrees + headingAdjustment + this.heading, arcRadius, SCRATCH);
            this.addVertex(rc, SCRATCH.latitude, SCRATCH.longitude, 0);
            // Add the major arc radius for the spine points. Spine points are vertically coincident with exterior
            // points. The first and middle most point do not have corresponding spine points.
            if (i > 0 && i < this.intervals / 2) {
                spineRadius[spineIdx++] = x;
            }
        }

        // Add the interior spine point vertices
        for (int i = 0; i < spinePoints; i++) {
            this.center.greatCircleLocation(0 + headingAdjustment + this.heading, spineRadius[i], SCRATCH);
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

    protected void calculateIntervals() {
        double circumference = this.calculateCircumference();
        int intervals = (int) (circumference / 700.0); // In a circle, this would generate an interval every 700m
        if (intervals < MIN_INTERVALS) {
            this.intervals = MIN_INTERVALS;
        } else if (intervals < this.maximumIntervals) {
            this.intervals = intervals;
        } else {
            this.intervals = this.maximumIntervals;
        }
    }

    private double calculateCircumference() {
        double a = this.majorRadius;
        double b = this.minorRadius;
        return Math.PI * (3 * (a + b) - Math.sqrt((3 * a + b) * (a + 3 * b)));
    }

    @Override
    protected void reset() {
        this.vertexArray.clear();
        this.interiorElements.clear();
        this.outlineElements.clear();
    }
}
