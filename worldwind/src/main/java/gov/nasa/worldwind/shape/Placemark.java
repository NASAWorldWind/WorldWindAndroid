/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.graphics.Rect;
import android.graphics.Typeface;
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
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GpuTexture;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWMath;

/**
 * Represents a Placemark shape. A placemark displays an image, a label and a leader line connecting the placemark's
 * geographic position to the ground. All three of these items are optional. By default, the leader line is not
 * pickable. See {@link Placemark#setEnableLeaderLinePicking(boolean)}.
 * <p/>
 * Placemarks may be drawn with either an image or as single-color square with a specified size. When the placemark
 * attributes indicate a valid image, the placemark's image is drawn as a rectangle in the image's original dimensions,
 * scaled by the image scale attribute. Otherwise, the placemark is drawn as a square with width and height equal to the
 * value of the image scale attribute, in pixels, and color equal to the image color attribute.
 */
public class Placemark extends AbstractRenderable {

    /**
     * The default eye distance above which to reduce the size of this placemark, in meters. If
     * [eyeDistanceScaling]{@link Placemark#setEyeDistanceScaling(boolean)} is true, this placemark's image, label and
     * leader line sizes are reduced as the eye distance increases beyond this threshold.
     */
    public static double defaultEyeDistanceScalingThreshold = 1e6;

    /**
     * The placemark's geographic position.
     */
    protected Position position;

    /**
     * The placemark's altitude mode. See {@link gov.nasa.worldwind.WorldWind.AltitudeMode}
     */
    @WorldWind.AltitudeMode
    protected int altitudeMode;

    /**
     * The placemark's normal attributes.
     */
    protected PlacemarkAttributes attributes = null;

    /**
     * The attributes to use when the placemark is highlighted.
     */
    protected PlacemarkAttributes highlightAttributes = null;

    /**
     * The label text to draw near the placemark.
     */
    protected String label = null;

    /**
     * Determines whether the normal or highlighted attibutes should be used.
     */
    protected boolean highlighted = false;

    /**
     * Indicates whether this placemark's size is reduced at higher eye distances.
     */
    protected boolean eyeDistanceScaling;

    /**
     * The eye distance above which to reduce the size of this placemark, in meters.
     */
    protected double eyeDistanceScalingThreshold;

    /**
     * The eye altitude above which this placemark's label is not displayed.
     */
    protected double eyeDistanceScalingLabelThreshold;

    /**
     * Indicates whether this placemark's leader line, if any, is pickable.
     */
    protected boolean enableLeaderLinePicking;

    /**
     * The amount of rotation to apply to the image, measured in degrees clockwise and relative to this placemark's
     * {@link Placemark#getImageRotationReference}.
     */
    protected double imageRotation;

    /**
     * Indicates whether to apply this placemark's image rotation relative to the screen orderedRenderable the globe.
     * See {@link gov.nasa.worldwind.WorldWind.OrientationMode}
     */
    @WorldWind.OrientationMode
    protected int imageRotationReference;

    /**
     * The amount of tilt to apply to the image, measured in degrees away from the eye point and relative to this
     * placemark's {@link Placemark#getImageTiltReference()}.
     */
    protected double imageTilt;

    /**
     * Indicates whether to apply this placemark's image tilt relative to the screen orderedRenderable the globe. See
     * {@link gov.nasa.worldwind.WorldWind.OrientationMode}
     */
    @WorldWind.OrientationMode
    protected int imageTiltReference;


    /**
     * The depth offset is used to the placemark to appear above nearby terrain. When a placemark is displayed near the
     * terrain portions of its geometry are often behind the terrain, yet as a screen element the placemark is expected
     * to be visible.
     */
    static double defaultDepthOffset = -0.003;

    /**
     * The OrderedRenderable implementation for this placemark.
     */
    OrderedPlacemark orderedPlacemark = null;

    /**
     * Factory method
     *
     * @param position
     * @param color
     * @param pixelSize
     *
     * @return
     */
    public static Placemark simple(Position position, Color color, int pixelSize) {
        return new Placemark(position, PlacemarkAttributes.defaults().setImageColor(color).setImageScale(pixelSize));
    }

    /**
     * Factory method
     *
     * @param position
     * @param imageSource
     *
     * @return
     */
    public static Placemark simpleImage(Position position, Object imageSource) {
        return new Placemark(position, PlacemarkAttributes.withImage(imageSource));
    }

    /**
     * Factory method
     *
     * @param position
     * @param imageSource
     * @param label
     *
     * @return
     */
    public static Placemark simpleImageAndLabel(Position position, Object imageSource, String label) {
        return new Placemark(position, PlacemarkAttributes.withImageAndLabel(imageSource), label);
    }


    public Placemark(Position position, PlacemarkAttributes attributes) {
        this(position, attributes, null, null, false);
    }

    public Placemark(Position position, PlacemarkAttributes attributes, String label) {
        this(position, attributes, label, null, false);
    }

    /**
     * Constructs a placemark.
     *
     * @param position           The placemark's geographic position.
     * @param attributes         The attributes to associate with this placemark. May be null, but if null the placemark
     *                           will not be drawn.
     * @param displayName        The display name associated with this placemark. May be null. If a name is specified,
     *                           but a label is not, then the name will be used for the label.
     * @param label              The label associated with this placemark. May be null. If specified, the label will be
     *                           drawn with the active {@link PlacemarkAttributes#labelAttributes}. If these attributes
     *                           are null, the label will not be drawn.
     * @param eyeDistanceScaling Indicates whether the size of this placemark scales with eye distance. See
     *                           [eyeDistanceScalingThreshold]{@link Placemark#eyeDistanceScalingThreshold} and
     *                           [eyeDistanceScalingLabelThreshold]{@link Placemark#eyeDistanceScalingLabelThreshold}.
     *
     * @throws IllegalArgumentException If the specified position is null orderedRenderable undefined.
     */
    public Placemark(Position position, PlacemarkAttributes attributes, String displayName, String label, boolean eyeDistanceScaling) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Placemark", "constructor", "missingPosition"));
        }
        setPosition(position);
        setAltitudeMode(WorldWind.ABSOLUTE);
        setDisplayName(displayName);  // TODO: perhaps label and displayName should be distinct
        // TODO: e.g., labels could be coordinates while display name is unique identifier

        this.attributes = attributes != null ? attributes : new PlacemarkAttributes();
        this.highlightAttributes = null;
        this.highlighted = false;
        this.eyeDistanceScaling = eyeDistanceScaling;
        this.eyeDistanceScalingThreshold = defaultEyeDistanceScalingThreshold;
        this.eyeDistanceScalingLabelThreshold = 1.5 * this.eyeDistanceScalingThreshold;
        this.enableLeaderLinePicking = false;
        this.imageRotation = 0;
        this.imageTilt = 0;
        this.imageRotationReference = WorldWind.RELATIVE_TO_SCREEN;
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
    }

    /**
     * Returns the placemark's altitude mode.
     */
    @WorldWind.AltitudeMode
    public int getAltitudeMode() {
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
    }

    /**
     * The placemark's attributes. If null and this placemark is not highlighted, this placemark is not drawn.
     */
    public PlacemarkAttributes getAttributes() {
        return attributes;
    }

    /**
     * The placemark's attributes. If null and this placemark is not highlighted, this placemark is not drawn.
     */
    public void setAttributes(PlacemarkAttributes attributes) {
        this.attributes = attributes;
    }

    /**
     * The attributes used when this placemark's highlighted flag is true. If null and the highlighted flag is true,
     * this placemark's normal attributes are used. If they, too, are null, this placemark is not drawn.
     */
    public PlacemarkAttributes getHighlightAttributes() {
        return highlightAttributes;
    }

    /**
     * The attributes used when this placemark's highlighted flag is true. If null and the highlighted flag is true,
     * this placemark's normal attributes are used. If they, too, are null, this placemark is not drawn.
     */
    public void setHighlightAttributes(PlacemarkAttributes highlightAttributes) {
        this.highlightAttributes = highlightAttributes;
    }

    public String getLabel() {
        if (this.label == null) {
            return this.getDisplayName();
        }
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Indicates whether this placemark uses its highlight attributes rather than its normal attributes.
     */
    public boolean isHighlighted() {
        return highlighted;
    }

    /**
     * Indicates whether this placemark uses its highlight attributes rather than its normal attributes.
     */
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    /**
     * Indicates whether this placemark's size is reduced at higher eye distances. If true, this placemark's size is
     * scaled inversely proportional to the eye distance if the eye distance is greater than the value of the
     * [eyeDistanceScalingThreshold]{@link Placemark#eyeDistanceScalingThreshold} property. When the eye distance is
     * below the threshold, this placemark is scaled only according to the [imageScale]{@link
     * PlacemarkAttributes#imageScale}.
     */
    public boolean isEyeDistanceScaling() {
        return eyeDistanceScaling;
    }

    /**
     * Indicates whether this placemark's size is reduced at higher eye distances. If true, this placemark's size is
     * scaled inversely proportional to the eye distance if the eye distance is greater than the value of the
     * [eyeDistanceScalingThreshold]{@link Placemark#eyeDistanceScalingThreshold} property. When the eye distance is
     * below the threshold, this placemark is scaled only according to the [imageScale]{@link
     * PlacemarkAttributes#imageScale}.
     */
    public void setEyeDistanceScaling(boolean eyeDistanceScaling) {
        this.eyeDistanceScaling = eyeDistanceScaling;
    }

    /**
     * The eye distance above which to reduce the size of this placemark, in meters. If [eyeDistanceScaling]{@link
     * Placemark#eyeDistanceScaling} is true, this placemark's image, label and leader line sizes are reduced as the eye
     * distance increases beyond this threshold.
     */
    public double getEyeDistanceScalingThreshold() {
        return eyeDistanceScalingThreshold;
    }

    /**
     * The eye distance above which to reduce the size of this placemark, in meters. If [eyeDistanceScaling]{@link
     * Placemark#eyeDistanceScaling} is true, this placemark's image, label and leader line sizes are reduced as the eye
     * distance increases beyond this threshold.
     */
    public void setEyeDistanceScalingThreshold(double eyeDistanceScalingThreshold) {
        this.eyeDistanceScalingThreshold = eyeDistanceScalingThreshold;
    }

    /**
     * The eye altitude above which this placemark's label is not displayed.
     */
    public double getEyeDistanceScalingLabelThreshold() {
        return eyeDistanceScalingLabelThreshold;
    }

    /**
     * The eye altitude above which this placemark's label is not displayed.
     */
    public void setEyeDistanceScalingLabelThreshold(double eyeDistanceScalingLabelThreshold) {
        this.eyeDistanceScalingLabelThreshold = eyeDistanceScalingLabelThreshold;
    }

    /**
     * The amount of rotation to apply to the image, measured in degrees clockwise and relative to this placemark's
     * [imageRotationReference]{@link Placemark#imageRotationReference}.
     */
    public double getImageRotation() {
        return imageRotation;
    }

    /**
     * The amount of rotation to apply to the image, measured in degrees clockwise and relative to this placemark's
     * [imageRotationReference]{@link Placemark#imageRotationReference}.
     */
    public void setImageRotation(double imageRotation) {
        this.imageRotation = imageRotation;
    }

    /**
     * Indicates whether to apply this placemark's image rotation relative to the screen orderedRenderable the globe. If
     * WorldWind.RELATIVE_TO_SCREEN, this placemark's image is rotated in the plane of the screen and its orientation
     * relative to the globe changes as the view changes. If WorldWind.RELATIVE_TO_GLOBE, this placemark's image is
     * rotated in a plane tangent to the globe at this placemark's position and retains its orientation relative to the
     * globe. See {@link gov.nasa.worldwind.WorldWind.OrientationMode}
     */
    public int getImageRotationReference() {
        return imageRotationReference;
    }

    /**
     * Indicates whether to apply this placemark's image rotation relative to the screen orderedRenderable the globe. If
     * WorldWind.RELATIVE_TO_SCREEN, this placemark's image is rotated in the plane of the screen and its orientation
     * relative to the globe changes as the view changes. If WorldWind.RELATIVE_TO_GLOBE, this placemark's image is
     * rotated in a plane tangent to the globe at this placemark's position and retains its orientation relative to the
     * globe. See {@link gov.nasa.worldwind.WorldWind.OrientationMode}
     */
    public void setImageRotationReference(int imageRotationReference) {
        this.imageRotationReference = imageRotationReference;
    }

    /**
     * The amount of tilt to apply to the image, measured in degrees away from the eye point and relative to this
     * placemark's [imageTiltReference]{@link Placemark#imageTiltReference}. While any positive orderedRenderable
     * negative number may be specified, values outside the range [0. 90] cause some orderedRenderable all of the image
     * to be clipped.
     */
    public double getImageTilt() {
        return imageTilt;
    }

    /**
     * The amount of tilt to apply to the image, measured in degrees away from the eye point and relative to this
     * placemark's [imageTiltReference]{@link Placemark#imageTiltReference}. While any positive orderedRenderable
     * negative number may be specified, values outside the range [0. 90] cause some orderedRenderable all of the image
     * to be clipped.
     */
    public void setImageTilt(double imageTilt) {
        this.imageTilt = imageTilt;
    }

    /**
     * Indicates whether to apply this placemark's image tilt relative to the screen orderedRenderable the globe. If
     * WorldWind.RELATIVE_TO_SCREEN, this placemark's image is tilted inwards (for positive tilts) relative to the plane
     * of the screen, and its orientation relative to the globe changes as the view changes. If
     * WorldWind.RELATIVE_TO_GLOBE, this placemark's image is tilted towards the globe's surface, and retains its
     * orientation relative to the surface. See {@link gov.nasa.worldwind.WorldWind.OrientationMode}
     */
    public int getImageTiltReference() {
        return imageTiltReference;
    }

    /**
     * Indicates whether to apply this placemark's image tilt relative to the screen orderedRenderable the globe. If
     * WorldWind.RELATIVE_TO_SCREEN, this placemark's image is tilted inwards (for positive tilts) relative to the plane
     * of the screen, and its orientation relative to the globe changes as the view changes. If
     * WorldWind.RELATIVE_TO_GLOBE, this placemark's image is tilted towards the globe's surface, and retains its
     * orientation relative to the surface. See {@link gov.nasa.worldwind.WorldWind.OrientationMode}
     */
    public void setImageTiltReference(int imageTiltReference) {
        this.imageTiltReference = imageTiltReference;
    }

    public boolean isEnableLeaderLinePicking() {
        return enableLeaderLinePicking;
    }

    public void setEnableLeaderLinePicking(boolean enableLeaderLinePicking) {
        this.enableLeaderLinePicking = enableLeaderLinePicking;
    }

    /**
     * Returns the placemark attributes that should be used in the next rendering pass.
     *
     * @param dc The current DrawContext.
     */
    public PlacemarkAttributes getActiveAttributes(final DrawContext dc) {
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
     *
     * @return The OrderedPlacemark to use for rendering; will be null if the placemark cannot or should not be
     * rendered.
     */
    protected OrderedPlacemark makeOrderedPlacemark(final DrawContext dc) {

        // Create a new instance if necessary, otherwise reuse the existing instance
        OrderedPlacemark op = (this.orderedPlacemark == null ? new OrderedPlacemark() : this.orderedPlacemark);
        if (!op.prepareForRendering(dc, this)) {
            return null;
        }
        return op;
    }

    ///////////////////////////////////////
    //
    // Ordered Placemark Inner Static Class
    //
    ///////////////////////////////////////

    /**
     * OrderedPlacemark is a delegate responsible for rendering a Placemark with eye distance ordering.
     */
    static protected class OrderedPlacemark implements OrderedRenderable {

        protected Position position = new Position();

        protected PlacemarkAttributes attributes = new PlacemarkAttributes();

        protected String label = null;

        protected Vec3 placePoint = new Vec3(0, 0, 0);

        protected Vec3 groundPoint = new Vec3(0, 0, 0);

        protected Vec3 screenPoint = new Vec3(0, 0, 0);

        protected double actualRotation = 0;

        protected double actualTilt = 0;

        protected double eyeDistance = 0;

        protected Matrix4 imageTransform = new Matrix4();

        protected Matrix4 labelTransform = null;

        protected Matrix3 texCoordMatrix = new Matrix3();

        protected Matrix4 mvpMatrix = new Matrix4();

        protected GpuTexture activeTexture = null;

        protected GpuTexture labelTexture = null;

        protected Rect imageBounds = null;

        protected Rect labelBounds = null;

        protected float[] leaderLinePoints = null;

        protected boolean drawLabel = false;

        protected boolean drawLeader = false;

        protected boolean enableLeaderLinePicking = false;

        /**
         * Prepares this OrderedPlacemark for visibility tests and subsequent rendering.
         *
         * @return True if this ordered renderable is in a state ready for rendering.
         */
        public boolean prepareForRendering(final DrawContext dc, final Placemark placemark) {

            // Get a copy of the attributes used for this rendering pass
            PlacemarkAttributes activeAttributes = placemark.getActiveAttributes(dc);
            if (activeAttributes == null) {
                return false;
            }
            this.attributes.set(activeAttributes);

            // Get a copy of the (optional) label. May be null.
            this.label = placemark.getLabel();

            // Precompute the image rotation. Leveraging protected access to Placemark fields.
            this.actualRotation = placemark.imageRotationReference == WorldWind.RELATIVE_TO_GLOBE ?
                dc.heading - placemark.imageRotation : -placemark.imageRotation;

            // Preompute the image tilt. Leveraging protected access to Placemark fields.
            this.actualTilt = placemark.imageTiltReference == WorldWind.RELATIVE_TO_GLOBE ?
                dc.tilt + placemark.imageTilt : placemark.imageTilt;

            // Compute the placemark's model point and corresponding distance to the eye point. If the placemark's
            // position is terrain-dependent but off the terrain, then compute it ABSOLUTE so that we have a point for
            // the placemark and are thus able to draw it. Otherwise its image and label portion that are potentially
            // over the terrain won't get drawn, and would disappear as soon as there is no terrain at the placemark's
            // position. This can occur at the window edges.
            Position currentPosition = placemark.getPosition();
            if (!this.position.equals(currentPosition)) {
                this.position.set(currentPosition);
                this.placePoint = null;
                this.groundPoint = null;
            }
            if (this.placePoint == null) {
                this.placePoint = dc.globe.geographicToCartesian(
                    this.position.latitude, this.position.longitude, this.position.altitude, new Vec3());
//            dc.surfacePointForMode(this.position.latitude, this.position.longitude, this.position.altitude, this.altitudeMode, this.placePoint);
            }

            // Compute the eye distance to the place point, the value which is used for sorting/ordering.
            this.eyeDistance = dc.eyePoint.distanceTo(this.placePoint);


            this.enableLeaderLinePicking = placemark.isEnableLeaderLinePicking();
            this.drawLeader = this.mustDrawLeaderLine(dc);
            if (this.drawLeader) {
                if (this.groundPoint == null) {
                    this.groundPoint = dc.globe.geographicToCartesian(
                        this.position.latitude, this.position.longitude, 0, new Vec3());
//                    dc.surfacePointForMode(this.position.latitude, this.position.longitude, 0, this.altitudeMode, this.groundPoint);
                }
            }

            ////////////////////////
            // Prepare the image
            ////////////////////////

            // Get the active texture, if applicable, creating it if necessary from the imageSource object.
            if (this.attributes.imageSource != null) {
                this.activeTexture = (GpuTexture) dc.gpuObjectCache.get(this.attributes.imageSource);
                if (this.activeTexture == null) {
                    this.activeTexture = new GpuTexture(dc, this.attributes.imageSource);
                }
            } else {
                // When there is no imageSource we draw a simple colored square
                this.activeTexture = null;
            }

            // Compute the placemark's screen point in the OpenGL coordinate system of the WorldWindow by projecting its
            // model coordinate point onto the viewport. Apply a depth offset in order to cause the entire placemark
            // image to appear above the globe/terrain. When a placemark is displayed near the terrain or the horizon,
            // portions of its geometry are often behind the terrain, yet as a screen element the placemark is expected
            // to be visible. We adjust its depth values rather than moving the placemark itself to avoid obscuring its
            // actual position.
            double depthOffset = Placemark.defaultDepthOffset;
            if (this.eyeDistance < dc.horizonDistance) {
                // Offset the image towards the eye such that whatever the orientation of the image, with respect to the
                // globe, the entire image is guaranteed to be in front of the globe/terrain.
                double longestSide = this.activeTexture != null ?
                    Math.max(this.activeTexture.getImageWidth(), this.activeTexture.getImageHeight()) : 1;
                double metersPerPixel = dc.pixelSizeAtDistance(this.eyeDistance);
                depthOffset = longestSide * this.attributes.imageScale * metersPerPixel * -1;
            }
            if (!dc.projectWithDepth(this.placePoint, depthOffset, this.screenPoint)) {
                // Probably outside the clipping planes
                return false;
            }

            double visibilityScale = placemark.isEyeDistanceScaling() ?
                Math.max(0.0, Math.min(1, placemark.getEyeDistanceScalingThreshold() / this.eyeDistance)) : 1;

            // Compute the placemark's transform matrix and texture coordinate matrix according to its screen point, image size,
            // image offset and image scale. The image offset is defined with its origin at the image's bottom-left corner and
            // axes that extend up and to the right from the origin point. When the placemark has no active texture the image
            // scale defines the image size and no other scaling is applied.
            if (this.activeTexture != null && this.activeTexture.bindTexture(dc, GLES20.GL_TEXTURE0)) {
                int w = this.activeTexture.getImageWidth();
                int h = this.activeTexture.getImageHeight();
                double s = this.attributes.imageScale * visibilityScale;
                Vec2 offset = this.attributes.imageOffset.offsetForSize(w, h);

                this.imageTransform.setTranslation(
                    this.screenPoint.x - offset.x * s,
                    this.screenPoint.y - offset.y * s,
                    this.screenPoint.z);

                this.imageTransform.setScale(w * s, h * s, 1);

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

            /////////////////////////
            // Prepare the label
            /////////////////////////

            // If there's a label, perform these same operations for the label texture, creating that texture if it
            // doesn't already exist.
            this.drawLabel = this.mustDrawLabel();
            if (this.drawLabel) {

                Typeface labelFont = this.attributes.labelAttributes.font;
                String labelKey = this.label + labelFont.toString();

                this.labelTexture = (GpuTexture) dc.gpuObjectCache.get(labelKey);
                if (this.labelTexture == null) {
                    // TODO: Create the label bitmap and texture
                    //this.labelTexture = dc.createFontTexture(Placemark.this.displayName, labelFont, false);
                }
                if (this.labelTexture != null && this.labelTexture.bindTexture(dc, GLES20.GL_TEXTURE0)) {

                    if (this.labelTransform == null) {
                        this.labelTransform = new Matrix4();
                    }
                    int w = this.labelTexture.getImageWidth();
                    int h = this.labelTexture.getImageHeight();
                    double s = this.attributes.labelAttributes.scale * visibilityScale;
                    Vec2 offset = this.attributes.labelAttributes.offset.offsetForSize(w, h);

                    this.labelTransform.setTranslation(
                        this.screenPoint.x - offset.x * s,
                        this.screenPoint.y - offset.y * s,
                        this.screenPoint.z);

                    this.labelTransform.setScale(w * s, h * s, 1);

                    this.labelBounds = WWMath.boundingRectForUnitQuad(this.labelTransform);
                }
            }
            return true;
        }

        /**
         * Renders the ordered placemark.
         */
        @Override
        public void renderOrdered(DrawContext dc) {
            this.drawOrderedPlacemark(dc);
        }

        /**
         * Performs the actual rendering of the Placemark.
         */
        protected void drawOrderedPlacemark(final DrawContext dc) {
            // Use World Wind's basic GLSL program.
            BasicProgram program = (BasicProgram) dc.gpuObjectCache.retrieveProgram(dc, BasicProgram.class);
            if (program == null) {
                return; // program is not in the GPU object cache yet
            }
            dc.useProgram(program);

            boolean depthTest = true;

            // Set up to use the shared tex attribute.
            // TODO: Store the texBuffer in the gpuObjectCache
            float[] texPoints = new float[]{
                0, 1,   // upper left corner
                0, 0,   // lower left corner
                1, 1,   // upper right corner
                1, 0};  // lower right corner
            FloatBuffer texBuffer = ByteBuffer.allocateDirect(texPoints.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            texBuffer.put(texPoints).rewind();
            GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 0, texBuffer);
            GLES20.glEnableVertexAttribArray(1);    // vertexTexCoord


            ///////////////////////////////////
            // Draw the optional leader-line
            ///////////////////////////////////

//        program.loadOpacity(gl, dc.pickingMode ? 1 : this.layer.opacity); // TODO: opacity

            // Draw the leader line first so that the image and label have visual priority.
            if (this.drawLeader) {
                if (this.leaderLinePoints == null) {
                    this.leaderLinePoints = new float[6];
                }
                // TODO: Store leader line  in gpuObjectCache
                this.leaderLinePoints[0] = (float) this.groundPoint.x; // computed during prepareForRendering
                this.leaderLinePoints[1] = (float) this.groundPoint.y;
                this.leaderLinePoints[2] = (float) this.groundPoint.z;
                this.leaderLinePoints[3] = (float) this.placePoint.x; // computed during prepareForRendering
                this.leaderLinePoints[4] = (float) this.placePoint.y;
                this.leaderLinePoints[5] = (float) this.placePoint.z;

                FloatBuffer leaderBuffer = ByteBuffer.allocateDirect(texPoints.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
                leaderBuffer.put(this.leaderLinePoints).rewind();
                GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, leaderBuffer);
                GLES20.glEnableVertexAttribArray(0);

//                if (!this.leaderLineCacheKey) {
//                    this.leaderLineCacheKey = dc.gpuObjectCache.generateCacheKey();
//                }
//                var leaderLineVboId = dc.gpuResourceCache.resourceForKey(this.leaderLineCacheKey);
//                if (!leaderLineVboId) {
//                    leaderLineVboId = gl.createBuffer();
//                    dc.gpuResourceCache.putResource(this.leaderLineCacheKey, leaderLineVboId,
//                        this.leaderLinePoints.length * 4);
//                }

                program.enableTexture(false);
                program.loadColor(/*dc.pickingMode ? this.pickColor : */ this.attributes.leaderLineAttributes.outlineColor); // TODO: pickColor

                this.mvpMatrix.set(dc.modelviewProjection);
                program.loadModelviewProjection(this.mvpMatrix);

                if (!this.attributes.leaderLineAttributes.depthTest && depthTest) {
                    depthTest = false;
                    GLES20.glDepthMask(false);
                }
                GLES20.glLineWidth(this.attributes.leaderLineAttributes.outlineWidth);
                GLES20.glEnableVertexAttribArray(0);    // vertexPoint
                GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
            }

            // Turn off depth testing for the placemark image if requested. The placemark label and leader line have
            // their own depth-test controls.
            if (!this.attributes.depthTest && depthTest) {
                depthTest = false;
                // Suppress writes to the OpenGL depth buffer.
                GLES20.glDepthMask(false);
            }

            ///////////////////////////////////
            // Draw the image
            ///////////////////////////////////

            // Allocate a unit-quad buffer for the image coordinates
            // TODO: Store the ptBuffer in the gpuObjectCache
            float[] points = new float[]{
                0, 1, 0,    // upper left corner
                0, 0, 0,    // lower left corner
                1, 1, 0,    // upper right corner
                1, 0, 0};   // lower right corner
            FloatBuffer ptBuffer = ByteBuffer.allocateDirect(points.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            ptBuffer.put(points).rewind();
            GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, ptBuffer);
            GLES20.glEnableVertexAttribArray(0);

            // Compute and specify the MVP matrix...
            this.mvpMatrix.set(dc.screenProjection);
            this.mvpMatrix.multiplyByMatrix(this.imageTransform);
            // ... perform image rotation
            this.mvpMatrix.multiplyByTranslation(0.5, 0.5, 0);
            this.mvpMatrix.multiplyByRotation(0, 0, 1, this.actualRotation);
            this.mvpMatrix.multiplyByTranslation(-0.5, -0.5, 0);
            // ... and perform the tilt so that the image tilts back from its base into the view volume.
            this.mvpMatrix.multiplyByRotation(-1, 0, 0, this.actualTilt);

            program.loadModelviewProjection(this.mvpMatrix);

            // Enable texture for both normal display and for picking. If picking is enabled in the shader (set in
            // beginDrawing() above) then the texture's alpha component is still needed in order to modulate the
            // pick color to mask off transparent pixels.
            program.enableTexture(true);

            if (dc.pickingMode) {
//              program.loadColor(gl, this.pickColor); // TODO: pickColor
            } else {
                program.loadColor(this.attributes.imageColor);
            }

            this.texCoordMatrix.setToIdentity();
            if (this.activeTexture != null) {
                this.activeTexture.applyTexCoordTransform(this.texCoordMatrix);
            }
            program.loadTexCoordMatrix(this.texCoordMatrix);

            if (this.activeTexture != null) {
                boolean bound = this.activeTexture.bindTexture(dc, GLES20.GL_TEXTURE0);
                program.enableTexture(bound);
            } else {
                program.enableTexture(false);
            }

            // Draw the placemark's image quad.
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            ///////////////////////////////////
            // Draw the label
            ///////////////////////////////////

            if (this.drawLabel) { // TODO: drawLabel
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
//            if (this.attributes.labelAttributes.depthTest && depthTest) {
//                    depthTest = true;
//                    gl.enable(gl.DEPTH_TEST);
//            } else {
//                depthTest = false;
//                gl.disable(gl.DEPTH_TEST);
//            }
//
//            gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);
            }

            // Restore depth testing state
            if (!depthTest) {
                GLES20.glDepthMask(true);
            }

            // Restore the default World Wind OpenGL state.
            //GLES20.glDisableVertexAttribArray(0);
            GLES20.glDisableVertexAttribArray(1);

        }

        /**
         * Determines if the placemark's image or label are visible.
         *
         * @return True if the image, label and/or leader-line intercept the viewport.
         */
        public boolean isVisible(DrawContext dc) {
            if (dc.pickingMode) {
                return this.imageBounds.intersect(dc.viewport); // TODO: pickRectangle
//                return dc.pickRectangle && (this.imageBounds.intersects(dc.pickRectangle)
//                    || (this.mustDrawLabel() && this.labelBounds.intersects(dc.pickRectangle))
//                    || (this.mustDrawLeaderLine(dc)
//                    && dc.pickFrustum.intersectsSegment(this.groundPoint, this.placePoint)));
            } else {
                return this.imageBounds.intersect(dc.viewport)
                    || (this.mustDrawLabel() && this.labelBounds.intersect(dc.viewport))
                    || (this.mustDrawLeaderLine(dc)
                    && dc.frustum.intersectsSegment(this.groundPoint, this.placePoint));
            }
        }

        /**
         * Determines if a label should and can be drawn.
         *
         * @return True if there is a valid label and label attributes.
         */
        public boolean mustDrawLabel() {
            return label != null && !label.isEmpty() && this.attributes.labelAttributes != null;
        }

        /**
         * Determines if a leader-line should and can be drawn.
         *
         * @return True if leader-line directive is enabled and there are valid leader-line attributes.
         */
        public boolean mustDrawLeaderLine(DrawContext dc) {
            return this.attributes.drawLeaderLine && this.attributes.leaderLineAttributes != null
                && (!dc.pickingMode || this.enableLeaderLinePicking);
        }

    }
}
