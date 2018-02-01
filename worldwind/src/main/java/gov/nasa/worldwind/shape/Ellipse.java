/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.opengl.GLES20;
import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.draw.DrawShapeState;
import gov.nasa.worldwind.draw.Drawable;
import gov.nasa.worldwind.draw.DrawableShape;
import gov.nasa.worldwind.draw.DrawableSurfaceShape;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Range;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.BufferObject;
import gov.nasa.worldwind.render.ImageOptions;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Texture;
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

    /**
     * The minimum number of intervals that will be used for geometry generation.
     */
    protected static final int MIN_INTERVALS = 32;

    /**
     * Key for Range object in the element buffer describing the top of the Ellipse.
     */
    protected static final int TOP_RANGE = 0;

    /**
     * Key for Range object in the element buffer describing the outline of the Ellipse.
     */
    protected static final int OUTLINE_RANGE = 1;

    /**
     * Key for Range object in the element buffer describing the extruded sides of the Ellipse.
     */
    protected static final int SIDE_RANGE = 2;

    protected static final ImageOptions defaultInteriorImageOptions = new ImageOptions();

    protected static final ImageOptions defaultOutlineImageOptions = new ImageOptions();

    /**
     * Simple interval count based cache of the keys for element buffers. Element buffers are dependent only on the
     * number of intervals so the keys are cached here. The element buffer object itself is in the
     * RenderResourceCache and subject to the restrictions and behavior of that cache.
     */
    protected static SparseArray<Object> elementBufferKeys = new SparseArray<>();

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
     * Draw sides of the ellipse which extend from the defined position and altitude to the ground.
     */
    protected boolean extrude;

    /**
     * Determines whether this ellipse's geometry follows the terrain surface or is fixed at a constant altitude.
     */
    protected boolean followTerrain;

    /**
     * The maximum pixels a single edge interval will span before the number of intevals is increased. Increasing this
     * value will make ellipses appear coarser.
     */
    protected double maximumPixelsPerInterval = 50;

    /**
     * The maximum number of angular intervals that may be used to assemble the ellipse's geometry for rendering.
     */
    protected int maximumIntervals = 256;

    /**
     * The number of intervals used for generating geometry. Clamped between MIN_INTERVALS and maximumIntervals.
     * Will always be even.
     */
    protected int activeIntervals;

    protected float[] vertexArray;

    protected int vertexIndex;

    protected Object vertexBufferKey = new Object();

    protected Vec3 vertexOrigin = new Vec3();

    protected boolean isSurfaceShape;

    protected double texCoord1d;

    protected Vec3 texCoord2d = new Vec3();

    protected Matrix3 texCoordMatrix = new Matrix3();

    protected Matrix4 modelToTexCoord = new Matrix4();

    protected double cameraDistance;

    protected Vec3 prevPoint = new Vec3();

    private static Position scratchPosition = new Position();

    private static Vec3 scratchPoint = new Vec3();

    static {
        defaultInteriorImageOptions.wrapMode = WorldWind.REPEAT;
        defaultOutlineImageOptions.resamplingMode = WorldWind.NEAREST_NEIGHBOR;
        defaultOutlineImageOptions.wrapMode = WorldWind.REPEAT;
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

    public boolean isExtrude() {
        return this.extrude;
    }

    public Ellipse setExtrude(boolean extrude) {
        this.extrude = extrude;
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
            this.vertexBufferKey = new Object();
        }

        // Obtain a drawable form the render context pool.
        Drawable drawable;
        DrawShapeState drawState;
        if (this.isSurfaceShape) {
            Pool<DrawableSurfaceShape> pool = rc.getDrawablePool(DrawableSurfaceShape.class);
            drawable = DrawableSurfaceShape.obtain(pool);
            drawState = ((DrawableSurfaceShape) drawable).drawState;
            ((DrawableSurfaceShape) drawable).sector.set(this.boundingSector);
            this.cameraDistance = this.cameraDistanceGeographic(rc, this.boundingSector);
        } else {
            Pool<DrawableShape> pool = rc.getDrawablePool(DrawableShape.class);
            drawable = DrawableShape.obtain(pool);
            drawState = ((DrawableShape) drawable).drawState;
            this.cameraDistance = this.boundingBox.distanceTo(rc.cameraPoint);
        }

        // Use the basic GLSL program to draw the shape.
        drawState.program = (BasicShaderProgram) rc.getShaderProgram(BasicShaderProgram.KEY);
        if (drawState.program == null) {
            drawState.program = (BasicShaderProgram) rc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(rc.resources));
        }

        // Assemble the drawable's OpenGL vertex buffer object.
        drawState.vertexBuffer = rc.getBufferObject(this.vertexBufferKey);
        if (drawState.vertexBuffer == null) {
            int size = this.vertexArray.length * 4;
            FloatBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
            buffer.put(this.vertexArray, 0, this.vertexArray.length);
            drawState.vertexBuffer = new BufferObject(GLES20.GL_ARRAY_BUFFER, size, buffer.rewind());
            rc.putBufferObject(this.vertexBufferKey, drawState.vertexBuffer);
        }

        // Get the attributes of the element buffer
        Object elementBufferKey = elementBufferKeys.get(this.activeIntervals);
        if (elementBufferKey == null) {
            elementBufferKey = new Object();
            elementBufferKeys.put(this.activeIntervals, elementBufferKey);
        }

        drawState.elementBuffer = rc.getBufferObject(elementBufferKey);
        if (drawState.elementBuffer == null) {
            drawState.elementBuffer = assembleElements(this.activeIntervals);
            rc.putBufferObject(elementBufferKey, drawState.elementBuffer);
        }

        if (this.isSurfaceShape) {
            this.drawInterior(rc, drawState);
            this.drawOutline(rc, drawState);
        } else {
            this.drawOutline(rc, drawState);
            this.drawInterior(rc, drawState);
        }

        // Configure the drawable according to the shape's attributes.
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
                this.computeRepeatingTexCoordTransform(texture, metersPerPixel, this.texCoordMatrix);
                drawState.texture(texture);
                drawState.texCoordMatrix(this.texCoordMatrix);
            }
        } else {
            drawState.texture(null);
        }

        // Configure the drawable to display the shape's interior.
        drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.interiorColor);
        drawState.texCoordAttrib(2 /*size*/, 12 /*offset in bytes*/);
        Range top = drawState.elementBuffer.ranges.get(TOP_RANGE);
        drawState.drawElements(GLES20.GL_TRIANGLE_STRIP, top.length(),
            GLES20.GL_UNSIGNED_SHORT, top.lower * 2 /*offset*/);

        if (this.extrude) {
            Range side = drawState.elementBuffer.ranges.get(SIDE_RANGE);
            drawState.texture(null);
            drawState.drawElements(GLES20.GL_TRIANGLE_STRIP, side.length(),
                GLES20.GL_UNSIGNED_SHORT, side.lower * 2);
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
                this.computeRepeatingTexCoordTransform(texture, metersPerPixel, this.texCoordMatrix);
                drawState.texture(texture);
                drawState.texCoordMatrix(this.texCoordMatrix);
            }
        } else {
            drawState.texture(null);
        }

        // Configure the drawable to display the shape's outline.
        drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
        drawState.lineWidth(this.activeAttributes.outlineWidth);
        drawState.texCoordAttrib(1 /*size*/, 20 /*offset in bytes*/);
        Range outline = drawState.elementBuffer.ranges.get(OUTLINE_RANGE);
        drawState.drawElements(GLES20.GL_LINE_LOOP, outline.length(),
            GLES20.GL_UNSIGNED_SHORT, outline.lower * 2 /*offset*/);

        if (this.activeAttributes.drawVerticals && this.extrude) {
            Range side = drawState.elementBuffer.ranges.get(SIDE_RANGE);
            drawState.color(rc.pickMode ? this.pickColor : this.activeAttributes.outlineColor);
            drawState.lineWidth(this.activeAttributes.outlineWidth);
            drawState.texture(null);
            drawState.drawElements(GLES20.GL_LINES, side.length(),
                GLES20.GL_UNSIGNED_SHORT, side.lower * 2);
        }
    }

    protected boolean mustAssembleGeometry(RenderContext rc) {
        int calculatedIntervals = this.computeIntervals(rc);
        int sanitizedIntervals = this.sanitizeIntervals(calculatedIntervals);
        if (this.vertexArray == null || sanitizedIntervals != this.activeIntervals) {
            this.activeIntervals = sanitizedIntervals;
            return true;
        }

        return false;
    }

    protected void assembleGeometry(RenderContext rc) {
        // Determine whether the shape geometry must be assembled as Cartesian geometry or as goegraphic geometry.
        this.isSurfaceShape = (this.altitudeMode == WorldWind.CLAMP_TO_GROUND) && this.followTerrain;

        // Compute a matrix that transforms from Cartesian coordinates to shape texture coordinates.
        this.determineModelToTexCoord(rc);

        // Use the ellipse's center position as the local origin for vertex positions.
        if (this.isSurfaceShape) {
            this.vertexOrigin.set(this.center.longitude, this.center.latitude, this.center.altitude);
        } else {
            rc.geographicToCartesian(this.center.latitude, this.center.longitude, this.center.altitude, this.altitudeMode, scratchPoint);
            this.vertexOrigin.set(scratchPoint.x, scratchPoint.y, scratchPoint.z);
        }

        // Determine the number of spine points
        int spineCount = computeNumberSpinePoints(this.activeIntervals); // activeIntervals must be even

        // Clear the shape's vertex array. The array will accumulate values as the shapes's geometry is assembled.
        this.vertexIndex = 0;
        if (this.extrude && !this.isSurfaceShape) {
            this.vertexArray = new float[(this.activeIntervals * 2 + spineCount) * VERTEX_STRIDE];
        } else {
            this.vertexArray = new float[(this.activeIntervals + spineCount) * VERTEX_STRIDE];
        }

        // Check if minor radius is less than major in which case we need to flip the definitions and change the phase
        boolean isStandardAxisOrientation = this.majorRadius > this.minorRadius;
        double headingAdjustment = isStandardAxisOrientation ? 90 : 0;

        // Vertex generation begins on the positive major axis and works ccs around the ellipse. The spine points are
        // then appended from positive major axis to negative major axis.
        double deltaRadians = 2 * Math.PI / this.activeIntervals;
        double majorArcRadians, minorArcRadians;
        double globeRadius = Math.max(rc.globe.getEquatorialRadius(), rc.globe.getPolarRadius());
        if (isStandardAxisOrientation) {
            majorArcRadians = this.majorRadius / globeRadius;
            minorArcRadians = this.minorRadius / globeRadius;
        } else {
            majorArcRadians = this.minorRadius / globeRadius;
            minorArcRadians = this.majorRadius / globeRadius;
        }

        // Determine the offset from the top and extruded vertices
        int arrayOffset = computeIndexOffset(this.activeIntervals) * VERTEX_STRIDE;
        // Setup spine radius values
        int spineIdx = 0;
        double[] spineRadius = new double[spineCount];

        // Iterate around the ellipse to add vertices
        for (int i = 0; i < this.activeIntervals; i++) {
            double radians = deltaRadians * i;
            double x = Math.cos(radians) * majorArcRadians;
            double y = Math.sin(radians) * minorArcRadians;
            double azimuthDegrees = Math.toDegrees(-Math.atan2(y, x));
            double arcRadius = Math.sqrt(x * x + y * y);
            // Calculate the great circle location given this activeIntervals step (azimuthDegrees) a correction value to
            // start from an east-west aligned major axis (90.0) and the user specified user heading value
            double azimuth = azimuthDegrees + headingAdjustment + this.heading;
            Location loc = this.center.greatCircleLocation(azimuth, arcRadius, scratchPosition);
            this.addVertex(rc, loc.latitude, loc.longitude, this.center.altitude, arrayOffset, this.isExtrude());
            // Add the major arc radius for the spine points. Spine points are vertically coincident with exterior
            // points. The first and middle most point do not have corresponding spine points.
            if (i > 0 && i < this.activeIntervals / 2) {
                spineRadius[spineIdx++] = x;
            }
        }

        // Add the interior spine point vertices
        for (int i = 0; i < spineCount; i++) {
            this.center.greatCircleLocation(0 + headingAdjustment + this.heading, spineRadius[i], scratchPosition);
            this.addVertex(rc, scratchPosition.latitude, scratchPosition.longitude, this.center.altitude, arrayOffset, false);
        }

        // Compute the shape's bounding sector from its assembled coordinates.
        if (this.isSurfaceShape) {
            this.boundingSector.setEmpty();
            this.boundingSector.union(this.vertexArray, this.vertexArray.length, VERTEX_STRIDE);
            this.boundingSector.translate(this.vertexOrigin.y /*lat*/, this.vertexOrigin.x /*lon*/);
            this.boundingBox.setToUnitBox(); // Surface/geographic shape bounding box is unused
        } else {
            this.boundingBox.setToPoints(this.vertexArray, this.vertexArray.length, VERTEX_STRIDE);
            this.boundingBox.translate(this.vertexOrigin.x, this.vertexOrigin.y, this.vertexOrigin.z);
            this.boundingSector.setEmpty();
        }
    }

    protected static BufferObject assembleElements(int intervals) {
        // Create temporary storage for elements
        ShortArray elements = new ShortArray();

        // Generate the top element buffer with spine
        int interiorIdx = intervals;
        int offset = computeIndexOffset(intervals);

        // Add the anchor leg
        elements.add((short) 0);
        elements.add((short) 1);
        // Tessellate the interior
        for (int i = 2; i < intervals; i++) {
            // Add the corresponding interior spine point if this isn't the vertex following the last vertex for the
            // negative major axis
            if (i != (intervals / 2 + 1)) {
                if (i > intervals / 2) {
                    elements.add((short) --interiorIdx);
                } else {
                    elements.add((short) interiorIdx++);
                }
            }
            // Add the degenerate triangle at the negative major axis in order to flip the triangle strip back towards
            // the positive axis
            if (i == intervals / 2) {
                elements.add((short) i);
            }
            // Add the exterior vertex
            elements.add((short) i);
        }
        // Complete the strip
        elements.add((short) --interiorIdx);
        elements.add((short) 0);
        Range topRange = new Range(0, elements.size());

        // Generate the outline element buffer
        for (int i = 0; i < intervals; i++) {
            elements.add((short) i);
        }
        Range outlineRange = new Range(topRange.upper, elements.size());

        // Generate the side element buffer
        for (int i = 0; i < intervals; i++) {
            elements.add((short) i);
            elements.add((short) (i + offset));
        }
        elements.add((short) 0);
        elements.add((short) offset);
        Range sideRange = new Range(outlineRange.upper, elements.size());

        // Generate a buffer for the element
        int size = elements.size() * 2;
        ShortBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();
        buffer.put(elements.array(), 0, elements.size());
        BufferObject elementBuffer = new BufferObject(GLES20.GL_ELEMENT_ARRAY_BUFFER, size, buffer.rewind());
        elementBuffer.ranges.put(TOP_RANGE, topRange);
        elementBuffer.ranges.put(OUTLINE_RANGE, outlineRange);
        elementBuffer.ranges.put(SIDE_RANGE, sideRange);

        return elementBuffer;
    }

    protected void addVertex(RenderContext rc, double latitude, double longitude, double altitude, int offset, boolean isExtrudedSkirt) {
        int offsetVertexIndex = this.vertexIndex + offset;

        Vec3 point = rc.geographicToCartesian(latitude, longitude, altitude, this.altitudeMode, scratchPoint);
        Vec3 texCoord2d = this.texCoord2d.set(point).multiplyByMatrix(this.modelToTexCoord);

        if (this.vertexIndex == 0) {
            this.texCoord1d = 0;
            this.prevPoint.set(point);
        } else {
            this.texCoord1d += point.distanceTo(this.prevPoint);
            this.prevPoint.set(point);
        }

        if (this.isSurfaceShape) {
            this.vertexArray[this.vertexIndex++] = (float) (longitude - this.vertexOrigin.x);
            this.vertexArray[this.vertexIndex++] = (float) (latitude - this.vertexOrigin.y);
            this.vertexArray[this.vertexIndex++] = (float) (altitude - this.vertexOrigin.z);
            // reserved for future texture coordinate use
            this.vertexArray[this.vertexIndex++] = (float) texCoord2d.x;
            this.vertexArray[this.vertexIndex++] = (float) texCoord2d.y;
            this.vertexArray[this.vertexIndex++] = (float) this.texCoord1d;
        } else {
            this.vertexArray[this.vertexIndex++] = (float) (point.x - this.vertexOrigin.x);
            this.vertexArray[this.vertexIndex++] = (float) (point.y - this.vertexOrigin.y);
            this.vertexArray[this.vertexIndex++] = (float) (point.z - this.vertexOrigin.z);
            this.vertexArray[this.vertexIndex++] = (float) texCoord2d.x;
            this.vertexArray[this.vertexIndex++] = (float) texCoord2d.y;
            this.vertexArray[this.vertexIndex++] = (float) this.texCoord1d;

            if (isExtrudedSkirt) {
                point = rc.geographicToCartesian(latitude, longitude, 0, WorldWind.CLAMP_TO_GROUND, scratchPoint);
                this.vertexArray[offsetVertexIndex++] = (float) (point.x - this.vertexOrigin.x);
                this.vertexArray[offsetVertexIndex++] = (float) (point.y - this.vertexOrigin.y);
                this.vertexArray[offsetVertexIndex++] = (float) (point.z - this.vertexOrigin.z);
                this.vertexArray[offsetVertexIndex++] = 0; //unused
                this.vertexArray[offsetVertexIndex++] = 0; //unused
                this.vertexArray[offsetVertexIndex++] = 0; //unused
            }
        }
    }

    protected void determineModelToTexCoord(RenderContext rc) {
        Vec3 point = rc.geographicToCartesian(this.center.latitude, this.center.longitude, this.center.altitude, this.altitudeMode, scratchPoint);
        this.modelToTexCoord = rc.globe.cartesianToLocalTransform(point.x, point.y, point.z, this.modelToTexCoord);
        this.modelToTexCoord.invertOrthonormal();
    }

    /**
     * Calculate the number of times to split the edges of the shape for geometry assembly.
     *
     * @param rc current RenderContext
     *
     * @return an even number of intervals
     */
    protected int computeIntervals(RenderContext rc) {
        int intervals = MIN_INTERVALS;
        if (intervals >= this.maximumIntervals) {
            return intervals; // use at least the minimum number of intervals
        }

        Vec3 centerPoint = rc.geographicToCartesian(this.center.latitude, this.center.longitude, this.center.altitude, this.altitudeMode, scratchPoint);
        double maxRadius = Math.max(this.majorRadius, this.minorRadius);
        double cameraDistance = centerPoint.distanceTo(rc.cameraPoint) - maxRadius;
        if (cameraDistance <= 0) {
            return this.maximumIntervals; // use the maximum number of intervals when the camera is very close
        }

        double metersPerPixel = rc.pixelSizeAtDistance(cameraDistance);
        double circumferencePixels = this.computeCircumference() / metersPerPixel;
        double circumferenceIntervals = circumferencePixels / this.maximumPixelsPerInterval;
        double subdivisions = Math.log(circumferenceIntervals / intervals) / Math.log(2);
        int subdivisionCount = Math.max(0, (int) Math.ceil(subdivisions));
        intervals <<= subdivisionCount; // subdivide the base intervals to achieve the desired number of intervals

        return Math.min(intervals, this.maximumIntervals); // don't exceed the maximum number of intervals
    }

    protected int sanitizeIntervals(int intervals) {
        return (intervals % 2) == 0 ? intervals : intervals - 1;
    }

    protected double computeCircumference() {
        double a = this.majorRadius;
        double b = this.minorRadius;
        return Math.PI * (3 * (a + b) - Math.sqrt((3 * a + b) * (a + 3 * b)));
    }

    protected static int computeNumberSpinePoints(int intervals) {
        // intervals should be even
        return intervals / 2 - 1;
    }

    protected static int computeIndexOffset(int intervals) {
        return intervals + computeNumberSpinePoints(intervals);
    }

    @Override
    protected void reset() {
        this.vertexArray = null;
    }
}
