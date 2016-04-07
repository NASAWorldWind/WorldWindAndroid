/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

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
     * This placemark's geographic position.
     */
    protected Position position;

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
     *
     * default 1e6 (meters)
     */
    protected double eyeDistanceScalingThreshold = 1e6;

    /**
     * The eye altitude above which this placemark's label is not displayed.
     */
    protected double eyeDistanceScalingLabelThreshold = 1.5 * this.eyeDistanceScalingThreshold;

    /**
     * This placemark's textual label. If null, no label is drawn.
     */
    protected String label = null;

    /**
     * This placemark's altitude mode. May be one of <pre>
     *  <ul>
     *  <li>[WorldWind.ABSOLUTE]{@link WorldWind#ABSOLUTE}</li>
     *  <li>[WorldWind.RELATIVE_TO_GROUND]{@link WorldWind#RELATIVE_TO_GROUND}</li>
     *  <li>[WorldWind.CLAMP_TO_GROUND]{@link WorldWind#CLAMP_TO_GROUND}</li>
     *  </ul>
     * </pre>default WorldWind.ABSOLUTE
     */
    protected
    @WorldWind.AltitudeMode
    int altitudeMode = WorldWind.ABSOLUTE;

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
     * This shape's target visibility, a value between 0 and 1. During ordered rendering this shape modifies its
     * [current visibility]{@link Text#currentVisibility} towards its target visibility at the rate specified by the
     * draw context's [fade time]{@link DrawContext#fadeTime} property. The target visibility and current visibility are
     * used to control the fading in and out of this shape.
     */
    double targetVisibility = 1d;

    /**
     * This shape's current visibility, a value between 0 and 1. This property scales the shape's effective opacity. It
     * is incremented orderedRenderable decremented each frame according to the draw context's [fade time]{@link
     * DrawContext#fadeTime} property in order to achieve this shape's current [target visibility]{@link
     * Text#targetVisibility}. This current visibility and target visibility are used to control the fading in and out
     * of this shape.
     *
     * readonly
     */
    double currentVisibility = 1;

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
    protected OrderedPlacemark op = null;


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

        this.attributes = attributes != null ? attributes : new PlacemarkAttributes();
        this.highlightAttributes = null;
        this.highlighted = false;
        this.position = position;
        this.eyeDistanceScaling = eyeDistanceScaling;
        this.eyeDistanceScalingThreshold = 1e6;
        this.eyeDistanceScalingLabelThreshold = 1.5 * this.eyeDistanceScalingThreshold;
        this.label = null;
        this.altitudeMode = WorldWind.ABSOLUTE;
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
     * @param dc
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
        OrderedPlacemark orderedPlacemark = this.makeOrderedPlacemark(dc);
        if (orderedPlacemark == null) {
            return;
        }

        // Rendering is deferred for ordered renderables; simply add the placemark to the collection of ordered renderables
        // for rendering later via OrderedPlacemark.renderOrdered().
        dc.offerOrderedRenderable(orderedPlacemark, orderedPlacemark.eyeDistance);
    }


    /**
     * Returns an ordered renderable for this placemark. The renderable may be a new instance or an existing instance.
     *
     * @param dc
     */
    protected OrderedPlacemark makeOrderedPlacemark(final DrawContext dc) {
        // Create a new instance if necessary, otherwise reuse the existing instance
        if (this.op == null) {
            this.op = new OrderedPlacemark(this);
        }

        // Update the attributes used for this rendering pass
        this.op.attributes = this.determineActiveAttributes(dc);
        if (this.op.attributes == null) {
            return null;
        }

        // Compute the placemark's model point and corresponding distance to the eye point. If the placemark's
        // position is terrain-dependent but off the terrain, then compute it ABSOLUTE so that we have a point for
        // the placemark and are thus able to draw it. Otherwise its image and label portion that are potentially
        // over the terrain won't get drawn, and would disappear as soon as there is no terrain at the placemark's
        // position. This can occur at the window edges.
//        dc.surfacePointForMode(this.position.latitude, this.position.longitude, this.position.altitude,
//            this.altitudeMode, this.placePoint);
        dc.globe.geographicToCartesian(
            this.position.latitude, this.position.longitude, this.position.altitude,
            this.op.placePoint);

        // Compute the eye distance to the place point, the value which is used for sorting/ordering.
        this.op.eyeDistance = this.alwaysOnTop ? 0 : dc.eyePoint.distanceTo(this.op.placePoint);

//        if (this.mustDrawLeaderLine(dc)) {
//            dc.surfacePointForMode(this.position.latitude, this.position.longitude, 0,
//                this.altitudeMode, this.groundPoint);
//        }

        // Compute the placemark's screen point in the OpenGL coordinate system of the WorldWindow by projecting its model
        // coordinate point onto the viewport. Apply a depth offset in order to cause the placemark to appear above nearby
        // terrain. When a placemark is displayed near the terrain portions of its geometry are often behind the terrain,
        // yet as a screen element the placemark is expected to be visible. We adjust its depth values rather than moving
        // the placemark itself to avoid obscuring its actual position.
        if (!dc.projectWithDepth(this.op.placePoint, this.depthOffset, this.op.screenPoint)) {
            return null;
        }
        System.out.println("screenPoint 1: " + this.op.screenPoint);

        double visibilityScale = this.eyeDistanceScaling ?
            Math.max(0.0, Math.min(1, this.eyeDistanceScalingThreshold / this.op.eyeDistance)) : 1;

        // Compute the placemark's transform matrix and texture coordinate matrix according to its screen point, image size,
        // image offset and image scale. The image offset is defined with its origin at the image's bottom-left corner and
        // axes that extend up and to the right from the origin point. When the placemark has no active texture the image
        // scale defines the image size and no other scaling is applied.
//        if (this.activeTexture) {
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
//        } else {
        double size = this.op.attributes.imageScale * visibilityScale;
        Vec2 offset = this.op.attributes.imageOffset.offsetForSize(size, size);

        this.op.imageTransform.setTranslation(
            this.op.screenPoint.x - offset.x,
            this.op.screenPoint.y - offset.y,
            this.op.screenPoint.z);

        this.op.imageTransform.setScale(size, size, 1);

        System.out.println("imageTransform 1: " + this.op.imageTransform);
//        }

        // If there's a label, perform these same operations for the label texture, creating that texture if it
        // doesn't already exist.
//        if (this.mustDrawLabel()) {
//            var labelFont = this.attributes.labelAttributes.font,
//                labelKey = this.label + labelFont.toString();
//
//            this.labelTexture = dc.gpuResourceCache.resourceForKey(labelKey);
//            if (!this.labelTexture) {
//                this.labelTexture = dc.textSupport.createTexture(dc, this.label, labelFont, true);
//                dc.gpuResourceCache.putResource(labelKey, this.labelTexture, this.labelTexture.size);
//            }
//
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
//            this.labelBounds = WWMath.boundingRectForUnitQuad(this.labelTransform);
//        }

        return this.op;
    }

    /**
     * OrderedPlacemark is a delegate responsible for rendering a Placemark shape with eye distance ordering.
     */
    public class OrderedPlacemark implements OrderedRenderable {

        public Placemark placemark;

        /**
         * The distance from the eye position to the placemark, used for sorting the ordered renderables.
         */
        public double eyeDistance;

        /**
         * The attributes to use during the current rendering pass.
         */
        public PlacemarkAttributes attributes = null;

        /**
         * Cartesian point corresponding to this placemark's geographic position
         */
        private Vec3 placePoint = new Vec3(0, 0, 0);

        /**
         * Cartesian point corresponding to ground position below this placemark
         */
        private Vec3 groundPoint = new Vec3(0, 0, 0);

        /**
         * Cartesian point corresponding to the placePoint in screen space
         */
        private Vec3 screenPoint = new Vec3(0, 0, 0);

        // Internal use only. Intentionally not documented.
        private Object activeTexture = null;

        // Internal use only. Intentionally not documented.
        private Object labelTexture = null;

        // Internal use only. Intentionally not documented.
        private Matrix4 imageTransform = new Matrix4();

        // Internal use only. Intentionally not documented.
        private Matrix4 labelTransform = new Matrix4();

        // Internal use only. Intentionally not documented.
        private Matrix3 texCoordMatrix = new Matrix3();

        // Internal use only. Intentionally not documented.
        private Matrix4 mvpMatrix = new Matrix4();

        public OrderedPlacemark(Placemark placemark) {
            this.placemark = placemark;
            this.eyeDistance = 0;
        }

        /**
         * @param dc
         */
        @Override
        public void renderOrdered(DrawContext dc) {
            this.doDrawOrderedPlacemark(dc);
        }


        /**
         * Performs the actual rendering of the Placemark.
         *
         * @param dc
         */
        protected void doDrawOrderedPlacemark(final DrawContext dc) {
            BasicProgram program = (BasicProgram) dc.gpuObjectCache.retrieveProgram(dc, BasicProgram.class);
            if (program == null) {
                return; // program is not in the GPU object cache yet
            }
            // Use World Wind's basic GLSL program.
            dc.useProgram(program);

            if (this.placemark.eyeDistanceScaling && (this.eyeDistance > this.placemark.eyeDistanceScalingLabelThreshold)) {
                // Target visibility is set to 0 to cause the label to be faded in or out. Nothing else
                // here uses target visibility.
                //this.targetVisibility = 0;
            }


//        // Compute the effective visibility. Use the current value if picking.
//        if (!dc.pickingMode && this.mustDrawLabel()) {
//            if (this.currentVisibility != this.targetVisibility) {
//                var visibilityDelta = (dc.timestamp - dc.previousRedrawTimestamp) / dc.fadeTime;
//                if (this.currentVisibility < this.targetVisibility) {
//                    this.currentVisibility = Math.min(1, this.currentVisibility + visibilityDelta);
//                } else {
//                    this.currentVisibility = Math.max(0, this.currentVisibility - visibilityDelta);
//                }
//                dc.redrawRequested = true;
//            }
//        }
//
//        program.loadOpacity(gl, dc.pickingMode ? 1 : this.layer.opacity);
//
//        // Draw the leader line first so that the image and label have visual priority.
//        if (this.mustDrawLeaderLine(dc)) {
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
//        }
//
            // Turn off depth testing for the placemark image if requested. The placemark label and leader line have
            // their own depth-test controls.
            boolean depthTest = true;
            if (!this.attributes.depthTest) {
                depthTest = false;
                // Suppress writes to the OpenGL depth buffer.
                GLES20.glDepthMask(false);
            }

            program.loadColor(this.attributes.imageColor);

//        this.texCoordMatrix.setToIdentity();
//        if (this.activeTexture != null) {
//            //this.texCoordMatrix.multiplyByTextureTransform(this.activeTexture);
//        }
//        program.loadTexCoordMatrix(this.texCoordMatrix);


            //////////////////////////////
            // THIS WORKS!
            /////////////////////////////
//        float[] point = this.screenPoint.toArray(new float[3], 0);
//        FloatBuffer buffer = ByteBuffer.allocateDirect(point.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
//        buffer.put(point).rewind();
//        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, buffer);
//        this.mvpMatrix.set(dc.getProjection());
//        this.mvpMatrix.multiplyByMatrix(dc.getModelview());
//        this.mvpMatrix.multiplyByTranslation(placePoint.x, placePoint.y, placePoint.z);

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

            double actualRotation = this.placemark.imageRotationReference == WorldWind.RELATIVE_TO_GLOBE ?
                dc.heading - this.placemark.imageRotation : -this.placemark.imageRotation;
            this.mvpMatrix.multiplyByTranslation(0.5, 0.5, 0);
            this.mvpMatrix.multiplyByRotation(0, 0, 1, actualRotation);
            this.mvpMatrix.multiplyByTranslation(-0.5, -0.5, 0);
            // Perform the tilt before applying the rotation so that the image tilts back from its base into
            // the view volume.
            double actualTilt = this.placemark.imageTiltReference == WorldWind.RELATIVE_TO_GLOBE ?
                dc.tilt + this.placemark.imageTilt : this.placemark.imageTilt;
            this.mvpMatrix.multiplyByRotation(-1, 0, 0, actualTilt);

            program.loadModelviewProjection(this.mvpMatrix);

            // Enable texture for both normal display and for picking. If picking is enabled in the shader (set in
            // beginDrawing() above) then the texture's alpha component is still needed in order to modulate the
            // pick color to mask off transparent pixels.
//        program.enableTexture(true);
//
//        if (dc.pickingMode) {
//            program.loadColor(gl, this.pickColor);
//        } else {
            program.loadColor(this.attributes.imageColor);
//        }
//
//        this.texCoordMatrix.setToIdentity();
//        if (this.activeTexture) {
//            this.texCoordMatrix.multiplyByTextureTransform(this.activeTexture);
//        }
//        program.loadTexCoordMatrix(this.texCoordMatrix);
//
//        if (this.activeTexture) {
//            textureBound = this.activeTexture.bind(dc); // returns false if active texture is null or cannot be bound
//            program.loadTextureEnabled(gl, textureBound);
//        } else {
//        program.enableTexture(false);
//        }

            // Draw the placemark's image quad.
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            //GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);


//        if (this.mustDrawLabel() && this.currentVisibility > 0) {
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
//        }
//
            if (!depthTest) {
                GLES20.glDepthMask(true);
            }
//
        }


    }
}
