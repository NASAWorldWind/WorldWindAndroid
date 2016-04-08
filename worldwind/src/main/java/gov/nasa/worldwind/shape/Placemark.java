/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.graphics.Rect;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec2;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.BasicProgram;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWMath;

/**
 * Represents a Placemark shape. A placemark displays an image, a label and a leader line connecting the placemark's
 * geographic position to the ground. All three of these items are optional. By default, the leader line is not
 * pickable. See [enableLeaderLinePicking]{@link Placemark#enableLeaderLinePicking}.
 * <p/>
 * Placemarks may be drawn with either an image orderedRenderable as single-color square with a specified size. When the
 * placemark attributes indicate a valid image, the placemark's image is drawn as a rectangle in the image's original
 * dimensions, scaled by the image scale attribute. Otherwise, the placemark is drawn as a square with width and height
 * equal to the value of the image scale attribute, in pixels, and color equal to the image color attribute.
 * <p/>
 * By default, placemarks participate in decluttering with a [declutterGroupID]{@link Placemark#declutterGroup} of 2.
 * Only placemark labels are decluttered relative to other placemark labels. The placemarks themselves are optionally
 * scaled with eye distance to achieve decluttering of the placemark as a whole. See [eyeDistanceScaling]{@link
 * Placemark#eyeDistanceScaling}.
 */
public class Placemark extends AbstractRenderable {

    protected Position position;

    protected Vec3 placePoint;

    protected Vec3 groundPoint;

    /**
     * This placemark's textual label. If null, no label is drawn.
     */
    protected String label = null;

    /**
     * The placemark's attributes. If null and this placemark is not highlighted, this placemark is not drawn.
     */
    protected PlacemarkAttributes attributes = null;

    /**
     * The attributes used when this placemark's highlighted flag is true. If null and the highlighted flag is true,
     * this placemark's normal attributes are used. If they, too, are null, this placemark is not drawn.
     */
    protected PlacemarkAttributes highlightAttributes = null;

    /**
     * Indicates whether this placemark uses its highlight attributes rather than its normal attributes.
     */
    protected boolean highlighted = false;


    /**
     * Indicates whether this placemark's size is reduced at higher eye distances. If true, this placemark's size is
     * scaled inversely proportional to the eye distance if the eye distance is greater than the value of the
     * [eyeDistanceScalingThreshold]{@link Placemark#eyeDistanceScalingThreshold} property. When the eye distance is
     * below the threshold, this placemark is scaled only according to the [imageScale]{@link
     * PlacemarkAttributes#imageScale}.
     */
    protected boolean eyeDistanceScaling = false;

    /**
     * The eye distance above which to reduce the size of this placemark, in meters. If [eyeDistanceScaling]{@link
     * Placemark#eyeDistanceScaling} is true, this placemark's image, label and leader line sizes are reduced as the eye
     * distance increases beyond this threshold.
     * <p/>
     * default 1e6 (meters)
     */
    protected double eyeDistanceScalingThreshold = 1e6;

    /**
     * The eye altitude above which this placemark's label is not displayed.
     */
    protected double eyeDistanceScalingLabelThreshold = 1.5 * this.eyeDistanceScalingThreshold;


    protected int altitudeMode = WorldWind.ABSOLUTE;

    /**
     * Indicates whether this placemark has visual priority over other shapes in the scene.
     */
    boolean alwaysOnTop = false;

    /**
     * Indicates whether this placemark's leader line, if any, is pickable.
     */
    boolean enableLeaderLinePicking = false;

    /**
     * Indicates whether this placemark's image should be re-retrieved even if it has already been retrieved. Set this
     * property to true when the image has changed but has the same image path. The property is set to false when the
     * image is re-retrieved.
     */
    boolean updateImage = true;

    /**
     * Indicates the group ID of the declutter group to include this Text shape. If non-zero, this shape is decluttered
     * relative to all other shapes within its group.
     */
    int declutterGroup = 2;

    /**
     * The amount of rotation to apply to the image, measured in degrees clockwise and relative to this placemark's
     * [imageRotationReference]{@link Placemark#imageRotationReference}.
     */
    double imageRotation;

    /**
     * The amount of tilt to apply to the image, measured in degrees away from the eye point and relative to this
     * placemark's [imageTiltReference]{@link Placemark#imageTiltReference}. While any positive orderedRenderable
     * negative number may be specified, values outside the range [0. 90] cause some orderedRenderable all of the image
     * to be clipped.
     */
    double imageTilt;

    /**
     * Indicates whether to apply this placemark's image rotation relative to the screen orderedRenderable the globe. If
     * WorldWind.RELATIVE_TO_SCREEN, this placemark's image is rotated in the plane of the screen and its orientation
     * relative to the globe changes as the view changes. If WorldWind.RELATIVE_TO_GLOBE, this placemark's image is
     * rotated in a plane tangent to the globe at this placemark's position and retains its orientation relative to the
     * globe.
     */
    @WorldWind.OrientationMode
    int imageRotationReference;

    /**
     * Indicates whether to apply this placemark's image tilt relative to the screen orderedRenderable the globe. If
     * WorldWind.RELATIVE_TO_SCREEN, this placemark's image is tilted inwards (for positive tilts) relative to the plane
     * of the screen, and its orientation relative to the globe changes as the view changes. If
     * WorldWind.RELATIVE_TO_GLOBE, this placemark's image is tilted towards the globe's surface, and retains its
     * orientation relative to the surface.
     */
    @WorldWind.OrientationMode
    int imageTiltReference;

    // Internal use only. Intentionally not documented.
    private double depthOffset = -0.003;

    /**
     * The OrderedRenderable implementation for this placemark.
     */
    protected OrderedPlacemark orderedPlacemark = null;


    public Placemark(Position position) {
        this(position, new PlacemarkAttributes());
    }

    public Placemark(Position position, PlacemarkAttributes attributes) {
        this(position, attributes, false);
    }

    /**
     * Constructs a placemark.
     *
     * @param position           The placemark's geographic position.
     * @param attributes         The attributes to associate with this placemark.
     * @param eyeDistanceScaling Indicates whether the size of this placemark scales with eye distance. See
     *                           [eyeDistanceScalingThreshold]{@link Placemark#eyeDistanceScalingThreshold} and
     *                           [eyeDistanceScalingLabelThreshold]{@link Placemark#eyeDistanceScalingLabelThreshold}.
     *
     * @throws IllegalArgumentException If the specified position is null orderedRenderable undefined.
     */
    public Placemark(Position position, PlacemarkAttributes attributes, boolean eyeDistanceScaling) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Placemark", "constructor", "missingPosition"));
        }
        setPosition(position);
        setAltitudeMode(WorldWind.ABSOLUTE);

        this.attributes = attributes != null ? attributes : new PlacemarkAttributes();
        this.highlightAttributes = null;
        this.highlighted = false;
        this.eyeDistanceScaling = eyeDistanceScaling;
        this.eyeDistanceScalingThreshold = 1e6;
        this.eyeDistanceScalingLabelThreshold = 1.5 * this.eyeDistanceScalingThreshold;
        this.label = null;
        this.alwaysOnTop = false;
        this.enableLeaderLinePicking = false;
        //this.updateImage = true;
        //this.declutterGroup = 2;
        //this.targetVisibility = 1;
        //this.currentVisibility = 1;
        this.imageRotation = 0;
        this.imageTilt = 0;
        this.imageRotationReference = WorldWind.RELATIVE_TO_GLOBE;
        this.imageTiltReference = WorldWind.RELATIVE_TO_SCREEN;
    }

    /**
     * Returns this placemark's geographic position.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets this placemark's geographic position.
     *
     * @param position The new position for the placemark.
     */
    public void setPosition(Position position) {
        this.position = position;
        this.placePoint = null; // must recompute after change in position
        this.groundPoint = null; // must recompute after change in position
    }

    /**
     * Returns the placemark's altitude mode.
     */
    public
    @WorldWind.AltitudeMode
    int getAltitudeMode() {
        return altitudeMode;
    }

    /**
     * Sets this placemark's altitude mode. May be one of <pre>
     *  <ul>
     *  <li>[WorldWind.ABSOLUTE]{@link WorldWind#ABSOLUTE}</li>
     *  <li>[WorldWind.RELATIVE_TO_GROUND]{@link WorldWind#RELATIVE_TO_GROUND}</li>
     *  <li>[WorldWind.CLAMP_TO_GROUND]{@link WorldWind#CLAMP_TO_GROUND}</li>
     *  </ul>
     * </pre>default WorldWind.ABSOLUTE
     */
    public void setAltitudeMode(@WorldWind.AltitudeMode int altitudeMode) {
        this.altitudeMode = altitudeMode;
        this.placePoint = null; // must recompute after change in mode
        this.groundPoint = null; // must recompute after change in position
    }

    /**
     * Returns the placemark attributes that should be used in the next rendering pass.
     *
     * @param dc The current DrawContext.
     */
    public PlacemarkAttributes determineActiveAttributes(final DrawContext dc) {
        return this.attributes;
    }

    /**
     * Performs the rendering; called by the public render method.
     *
     * @param dc The current DrawContext.
     */
    @Override
    protected void doRender(DrawContext dc) {
        // Get an OrderedRenderable delegate for this placemark. The delegates will be sorted
        // by eye distance and rendered after the layers are rendered.
        OrderedPlacemark op = this.makeOrderedPlacemark(dc);
        if (op == null || !op.isVisible(dc)) {
            return;
        }
        this.orderedPlacemark = op;

        // Rendering is deferred for ordered renderables; simply add the placemark to the collection of ordered renderables
        // for rendering later via OrderedPlacemark.renderOrdered().
        dc.offerOrderedRenderable(op, op.eyeDistance);
    }


    /**
     * Returns an ordered renderable for this placemark. The renderable may be a new instance or an existing instance.
     *
     * @param dc The current DrawContext.
     */
    protected OrderedPlacemark makeOrderedPlacemark(final DrawContext dc) {
        // Create a new instance if necessary, otherwise reuse the existing instance
        OrderedPlacemark op = (this.orderedPlacemark == null ? new OrderedPlacemark() : this.orderedPlacemark);

        if (!op.prepareForRendering(dc)) {
            return null;
        }
        return op;
    }

    /**
     * OrderedPlacemark is an inner class that acts as a delegate responsible for rendering a Placemark shape with eye
     * distance ordering.
     */
    protected class OrderedPlacemark implements OrderedRenderable {

        public double eyeDistance = 0;

        public PlacemarkAttributes attributes = null;

        private Vec3 screenPoint = new Vec3(0, 0, 0);

        private Object activeTexture = null;

        private Object labelTexture = null;

        private Matrix4 imageTransform = new Matrix4();

        private Matrix4 labelTransform = null;

        private Matrix3 texCoordMatrix = new Matrix3();

        private Matrix4 mvpMatrix = new Matrix4();

        private Rect imageBounds = null;

        private Rect labelBounds = null;

        /**
         * This shape's target visibility, a value between 0 and 1. During ordered rendering this shape modifies its
         * [current visibility]{@link Text#currentVisibility} towards its target visibility at the rate specified by the
         * draw context's [fade time]{@link DrawContext#fadeTime} property. The target visibility and current visibility
         * are used to control the fading in and out of this shape.
         */
        private double targetVisibility = 1d;

        /**
         * This shape's current visibility, a value between 0 and 1. This property scales the shape's effective opacity.
         * It is incremented orderedRenderable decremented each frame according to the draw context's [fade time]{@link
         * DrawContext#fadeTime} property in order to achieve this shape's current [target visibility]{@link
         * Text#targetVisibility}. This current visibility and target visibility are used to control the fading in and
         * out of this shape.
         */
        private double currentVisibility = 1d;

        /**
         * Prepares this OrderedPlacemark for visibility tests and subsequent rendering.
         *
         * @return True if this ordered renderable is in a state ready for rendering.
         */
        public boolean prepareForRendering(final DrawContext dc) {

            // Update the attributes used for this rendering pass
            this.attributes = Placemark.this.determineActiveAttributes(dc);
            if (this.attributes == null) {
                return false;
            }

            // Compute the placemark's model point and corresponding distance to the eye point. If the placemark's
            // position is terrain-dependent but off the terrain, then compute it ABSOLUTE so that we have a point for
            // the placemark and are thus able to draw it. Otherwise its image and label portion that are potentially
            // over the terrain won't get drawn, and would disappear as soon as there is no terrain at the placemark's
            // position. This can occur at the window edges.
            if (Placemark.this.placePoint == null) {
                Placemark.this.placePoint = dc.globe.geographicToCartesian(
                    Placemark.this.position.latitude, Placemark.this.position.longitude, Placemark.this.position.altitude, new Vec3());
//            dc.surfacePointForMode(this.position.latitude, this.position.longitude, this.position.altitude, this.altitudeMode, this.placePoint);
            }

            // Compute the eye distance to the place point, the value which is used for sorting/ordering.
            this.eyeDistance = Placemark.this.alwaysOnTop ? 0 : dc.eyePoint.distanceTo(Placemark.this.placePoint);

            if (this.mustDrawLeaderLine(dc)) {
                if (Placemark.this.groundPoint == null) {
                    Placemark.this.groundPoint = dc.globe.geographicToCartesian(
                        Placemark.this.position.latitude, Placemark.this.position.longitude, 0, new Vec3());
//                    dc.surfacePointForMode(this.position.latitude, this.position.longitude, 0, this.altitudeMode, this.groundPoint);
                }
            }

            // Compute the placemark's screen point in the OpenGL coordinate system of the WorldWindow by projecting its model
            // coordinate point onto the viewport. Apply a depth offset in order to cause the placemark to appear above nearby
            // terrain. When a placemark is displayed near the terrain portions of its geometry are often behind the terrain,
            // yet as a screen element the placemark is expected to be visible. We adjust its depth values rather than moving
            // the placemark itself to avoid obscuring its actual position.
            if (!dc.projectWithDepth(Placemark.this.placePoint, Placemark.this.depthOffset, this.screenPoint)) {
                return false;
            }

            double visibilityScale = Placemark.this.eyeDistanceScaling ?
                Math.max(0.0, Math.min(1, Placemark.this.eyeDistanceScalingThreshold / this.eyeDistance)) : 1;

            // Compute the placemark's transform matrix and texture coordinate matrix according to its screen point, image size,
            // image offset and image scale. The image offset is defined with its origin at the image's bottom-left corner and
            // axes that extend up and to the right from the origin point. When the placemark has no active texture the image
            // scale defines the image size and no other scaling is applied.
            if (this.activeTexture != null) {
//            w = this.activeTexture.originalImageWidth;
//            h = this.activeTexture.originalImageHeight;
//            s = this.attributes.imageScale * visibilityScale;
//            offset = this.attributes.imageOffset.offsetForSize(w, h);
//
//            this.imageTransform.setTranslation(
//                Placemark.screenPoint[0] - offset[0] * s,
//                Placemark.screenPoint[1] - offset[1] * s,
//                Placemark.screenPoint[2]);
//
//            this.imageTransform.setScale(w * s, h * s, 1);
            } else {
                double size = this.attributes.imageScale * visibilityScale;
                Vec2 offset = this.attributes.imageOffset.offsetForSize(size, size);

                this.imageTransform.setTranslation(
                    this.screenPoint.x - offset.x,
                    this.screenPoint.y - offset.y,
                    this.screenPoint.z);

                this.imageTransform.setScale(size, size, 1);
            }

            // Compute the image bounding box in screen coordinates for visibility testing
            this.imageBounds = WWMath.boundingRectForUnitQuad(this.imageTransform);


            // If there's a label, perform these same operations for the label texture, creating that texture if it
            // doesn't already exist.
            if (this.mustDrawLabel()) {
//            var labelFont = this.attributes.labelAttributes.font,
//                labelKey = this.label + labelFont.toString();
//
//            this.labelTexture = dc.gpuResourceCache.resourceForKey(labelKey);
//            if (!this.labelTexture) {
//                this.labelTexture = dc.textSupport.createTexture(dc, this.label, labelFont, true);
//                dc.gpuResourceCache.putResource(labelKey, this.labelTexture, this.labelTexture.size);
//            }
                if (this.labelTransform == null) {
                    this.labelTransform = new Matrix4();
                }
//            w = this.labelTexture.imageWidth;
//            h = this.labelTexture.imageHeight;
//            s = this.attributes.labelAttributes.scale * visibilityScale;
//            offset = this.attributes.labelAttributes.offset.offsetForSize(w, h);
//
//            this.labelTransform.setTranslation(
//                Placemark.screenPoint[0] - offset[0] * s,
//                Placemark.screenPoint[1] - offset[1] * s,
//                Placemark.screenPoint[2]);
//
//            this.labelTransform.setScale(w * s, h * s, 1);
//
                this.labelBounds = WWMath.boundingRectForUnitQuad(this.labelTransform);
            }

            return true;
        }

        /**
         * Renders the placemark.
         */
        @Override
        public void renderOrdered(DrawContext dc) {
            this.doDrawOrderedPlacemark(dc);
        }

        /**
         * Performs the actual rendering of the Placemark.
         */
        void doDrawOrderedPlacemark(final DrawContext dc) {
            BasicProgram program = (BasicProgram) dc.gpuObjectCache.retrieveProgram(dc, BasicProgram.class);
            if (program == null) {
                return; // program is not in the GPU object cache yet
            }
            // Use World Wind's basic GLSL program.
            dc.useProgram(program);

            if (Placemark.this.eyeDistanceScaling && (this.eyeDistance > Placemark.this.eyeDistanceScalingLabelThreshold)) {
                // Target visibility is set to 0 to cause the label to be faded in or out. Nothing else
                // here uses target visibility.
                this.targetVisibility = 0;
            }

            // Compute the effective visibility. Use the current value if picking.
            if (!dc.pickingMode && this.mustDrawLabel()) {
                if (this.currentVisibility != this.targetVisibility) {
//                var visibilityDelta = (dc.timestamp - dc.previousRedrawTimestamp) / dc.fadeTime;
//                if (this.currentVisibility < this.targetVisibility) {
//                    this.currentVisibility = Math.min(1, this.currentVisibility + visibilityDelta);
//                } else {
//                    this.currentVisibility = Math.max(0, this.currentVisibility - visibilityDelta);
//                }
//                dc.redrawRequested = true;
                }
            }

            ///////////////////////////////////
            // Draw the optional leader-line
            ///////////////////////////////////

//        program.loadOpacity(gl, dc.pickingMode ? 1 : this.layer.opacity);

            // Draw the leader line first so that the image and label have visual priority.
            if (this.mustDrawLeaderLine(dc)) {
//            if (!this.leaderLinePoints) {
//                this.leaderLinePoints = new Float32Array(6);
//            }
//
//            this.leaderLinePoints[0] = this.groundPoint[0]; // computed during makeOrderedRenderable
//            this.leaderLinePoints[1] = this.groundPoint[1];
//            this.leaderLinePoints[2] = this.groundPoint[2];
//            this.leaderLinePoints[3] = this.placePoint[0]; // computed during makeOrderedRenderable
//            this.leaderLinePoints[4] = this.placePoint[1];
//            this.leaderLinePoints[5] = this.placePoint[2];
//
//            if (!this.leaderLineCacheKey) {
//                this.leaderLineCacheKey = dc.gpuResourceCache.generateCacheKey();
//            }
//
//            var leaderLineVboId = dc.gpuResourceCache.resourceForKey(this.leaderLineCacheKey);
//            if (!leaderLineVboId) {
//                leaderLineVboId = gl.createBuffer();
//                dc.gpuResourceCache.putResource(this.leaderLineCacheKey, leaderLineVboId,
//                    this.leaderLinePoints.length * 4);
//            }
//
//            program.loadTextureEnabled(gl, false);
//            program.loadColor(gl, dc.pickingMode ? this.pickColor :
//                this.attributes.leaderLineAttributes.outlineColor);
//
//            Placemark.matrix.copy(dc.navigatorState.modelviewProjection);
//            program.loadModelviewProjection(gl, Placemark.matrix);
//
//            if (!this.attributes.leaderLineAttributes.depthTest) {
//                gl.disable(gl.DEPTH_TEST);
//            }
//
//            gl.lineWidth(this.attributes.leaderLineAttributes.outlineWidth);
//
//            gl.bindBuffer(gl.ARRAY_BUFFER, leaderLineVboId);
//            gl.bufferData(gl.ARRAY_BUFFER, this.leaderLinePoints, gl.STATIC_DRAW);
//            dc.frameStatistics.incrementVboLoadCount(1);
//            gl.vertexAttribPointer(program.vertexPointLocation, 3, gl.FLOAT, false, 0, 0);
//            gl.drawArrays(gl.LINES, 0, 2);
            }

            // Turn off depth testing for the placemark image if requested. The placemark label and leader line have
            // their own depth-test controls.
            boolean depthTest = true;
            if (!this.attributes.depthTest) {
                depthTest = false;
                // Suppress writes to the OpenGL depth buffer.
                GLES20.glDepthMask(false);
            }

            ///////////////////////////////////
            // Draw the image
            ///////////////////////////////////

            program.loadColor(this.attributes.imageColor);

            this.texCoordMatrix.setToIdentity();
            if (this.activeTexture != null) {
//            this.texCoordMatrix.multiplyByTextureTransform(this.activeTexture);
            }
            program.loadTexCoordMatrix(this.texCoordMatrix);

            // Allocate a unit-quad buffer for the image coordinates
            float[] points = new float[]{
                0, 1, 0,    // upper left corner
                0, 0, 0,    // lower left corner
                1, 1, 0,    // upper right corner
                1, 0, 0};   // lower right corner
            FloatBuffer buffer = ByteBuffer.allocateDirect(points.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            buffer.put(points).rewind();
            GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, buffer);
            GLES20.glEnableVertexAttribArray(0);

            // Compute and specify the MVP matrix.
            this.mvpMatrix.set(dc.screenProjection);
            this.mvpMatrix.multiplyByMatrix(this.imageTransform);

            double actualRotation = Placemark.this.imageRotationReference == WorldWind.RELATIVE_TO_GLOBE ?
                dc.heading - Placemark.this.imageRotation : -Placemark.this.imageRotation;
            this.mvpMatrix.multiplyByTranslation(0.5, 0.5, 0);
            this.mvpMatrix.multiplyByRotation(0, 0, 1, actualRotation);
            this.mvpMatrix.multiplyByTranslation(-0.5, -0.5, 0);
            // Perform the tilt before applying the rotation so that the image tilts back from its base into the view volume.
            double actualTilt = Placemark.this.imageTiltReference == WorldWind.RELATIVE_TO_GLOBE ?
                dc.tilt + Placemark.this.imageTilt : Placemark.this.imageTilt;
            this.mvpMatrix.multiplyByRotation(-1, 0, 0, actualTilt);

            program.loadModelviewProjection(this.mvpMatrix);

            // Enable texture for both normal display and for picking. If picking is enabled in the shader (set in
            // beginDrawing() above) then the texture's alpha component is still needed in order to modulate the
            // pick color to mask off transparent pixels.
            program.enableTexture(true);
//
            if (dc.pickingMode) {
//            program.loadColor(gl, this.pickColor);
            } else {
                program.loadColor(this.attributes.imageColor);
            }

            this.texCoordMatrix.setToIdentity();
            if (this.activeTexture != null) {
//            this.texCoordMatrix.multiplyByTextureTransform(this.activeTexture);
            }
            program.loadTexCoordMatrix(this.texCoordMatrix);

            if (this.activeTexture != null) {
//            textureBound = this.activeTexture.bind(dc); // returns false if active texture is null or cannot be bound
//            program.loadTextureEnabled(gl, textureBound);
            } else {
                program.enableTexture(false);
            }

            // Draw the placemark's image quad.
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            ///////////////////////////////////
            // Draw the label
            ///////////////////////////////////

            if (this.mustDrawLabel() && this.currentVisibility > 0) {
//            program.loadOpacity(gl, dc.pickingMode ? 1 : this.layer.opacity * this.currentVisibility);
//
//            Placemark.matrix.copy(dc.screenProjection);
//            Placemark.matrix.multiplyMatrix(this.labelTransform);
//            program.loadModelviewProjection(gl, Placemark.matrix);
//
//            if (!dc.pickingMode && this.labelTexture) {
//                this.texCoordMatrix.setToIdentity();
//                this.texCoordMatrix.multiplyByTextureTransform(this.labelTexture);
//
//                program.loadTextureMatrix(gl, this.texCoordMatrix);
//                program.loadColor(gl, this.attributes.labelAttributes.color);
//
//                textureBound = this.labelTexture.bind(dc);
//                program.loadTextureEnabled(gl, textureBound);
//            } else {
//                program.loadTextureEnabled(gl, false);
//                program.loadColor(gl, this.pickColor);
//            }
//
//            if (this.attributes.labelAttributes.depthTest) {
//                if (!depthTest) {
//                    depthTest = true;
//                    gl.enable(gl.DEPTH_TEST);
//                }
//            } else {
//                depthTest = false;
//                gl.disable(gl.DEPTH_TEST);
//            }
//
//            gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
            }

            if (!depthTest) {
                GLES20.glDepthMask(true);
            }

        }

        /**
         * Determines if the placemark's image or label are visible.
         *
         * @return True if the image, label and/or leader-line intercept the viewport.
         */
        public boolean isVisible(DrawContext dc) {
            if (dc.pickingMode) {
                return this.imageBounds.intersect(dc.viewport);
//                return dc.pickRectangle && (this.imageBounds.intersects(dc.pickRectangle)
//                    || (this.mustDrawLabel() && this.labelBounds.intersects(dc.pickRectangle))
//                    || (this.mustDrawLeaderLine(dc)
//                    && dc.pickFrustum.intersectsSegment(this.groundPoint, this.placePoint)));
            } else {
                return this.imageBounds.intersect(dc.viewport)
                    || (this.mustDrawLabel() && this.labelBounds.intersect(dc.viewport))
                    || (this.mustDrawLeaderLine(dc)
                    && dc.frustum.intersectsSegment(Placemark.this.groundPoint, Placemark.this.placePoint));
            }
        }

        /**
         * Determines if a label should and can be drawn.
         *
         * @return True if there is a valid label and label attributes.
         */
        boolean mustDrawLabel() {
            return Placemark.this.label != null && !Placemark.this.label.isEmpty() && this.attributes.labelAttributes != null;
        }

        /**
         * Determines if a leader-line should and can be drawn.
         *
         * @return True if leader-line directive is enabled and there are valid leader-line attributes.
         */
        boolean mustDrawLeaderLine(DrawContext dc) {
            return this.attributes.drawLeaderLine && this.attributes.leaderLineAttributes != null
                && (!dc.pickingMode || Placemark.this.enableLeaderLinePicking);
        }

    }
}
