/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.graphics.Rect;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.draw.DrawablePlacemark;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec2;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ImageSource;
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
     * The default eye distance above which to reduce the size of this placemark, in meters. If {@link
     * Placemark#setEyeDistanceScaling(boolean)} is true, this placemark's image, label and leader line sizes are
     * reduced as the eye distance increases beyond this threshold.
     */
    protected static final double DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD = 1e6;

    private static final double DEFAULT_DEPTH_OFFSET = -0.003;

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
    protected PlacemarkAttributes attributes;

    /**
     * The attributes to use when the placemark is highlighted.
     */
    protected PlacemarkAttributes highlightAttributes;

    protected PlacemarkAttributes activeAttributes;

    /**
     * The label text to draw near the placemark.
     */
    protected String label;

    /**
     * Determines whether the normal or highlighted attibutes should be used.
     */
    protected boolean highlighted;

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
     * The Drawable implementation for this placemark.
     */
    protected DrawablePlacemark drawablePlacemark;

    private double eyeDistance;

    private Vec3 placePoint = new Vec3();

    private Vec3 groundPoint;  // will be created if a leader line must be drawn

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

        this.setPosition(position);
        this.setAltitudeMode(WorldWind.ABSOLUTE);
        this.setDisplayName(displayName);
        this.setLabel(label);
        this.attributes = attributes != null ? attributes : new PlacemarkAttributes();
        this.eyeDistanceScaling = eyeDistanceScaling;
        this.eyeDistanceScalingThreshold = DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD;
        this.eyeDistanceScalingLabelThreshold = 1.5 * this.eyeDistanceScalingThreshold;
        this.imageRotationReference = WorldWind.RELATIVE_TO_SCREEN;
        this.imageTiltReference = WorldWind.RELATIVE_TO_SCREEN;
    }

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
    public static Placemark simpleImage(Position position, ImageSource imageSource) {
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
    public static Placemark simpleImageAndLabel(Position position, ImageSource imageSource, String label) {
        return new Placemark(position, PlacemarkAttributes.withImageAndLabel(imageSource), label);
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
    public Placemark setPosition(Position position) {
        this.position = position;
        return this;
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
    public Placemark setAltitudeMode(@WorldWind.AltitudeMode int altitudeMode) {
        this.altitudeMode = altitudeMode;
        return this;
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
    public Placemark setAttributes(PlacemarkAttributes attributes) {
        this.attributes = attributes;
        return this;
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
    public Placemark setHighlightAttributes(PlacemarkAttributes highlightAttributes) {
        this.highlightAttributes = highlightAttributes;
        return this;
    }

    public String getLabel() {
        if (this.label == null) {
            return this.getDisplayName();
        }
        return label;
    }

    public Placemark setLabel(String label) {
        this.label = label;
        return this;
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
    public Placemark setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        return this;
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
    public Placemark setEyeDistanceScaling(boolean eyeDistanceScaling) {
        this.eyeDistanceScaling = eyeDistanceScaling;
        return this;
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
    public Placemark setEyeDistanceScalingThreshold(double eyeDistanceScalingThreshold) {
        this.eyeDistanceScalingThreshold = eyeDistanceScalingThreshold;
        return this;
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
    public Placemark setEyeDistanceScalingLabelThreshold(double eyeDistanceScalingLabelThreshold) {
        this.eyeDistanceScalingLabelThreshold = eyeDistanceScalingLabelThreshold;
        return this;
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
    public Placemark setImageRotation(double imageRotation) {
        this.imageRotation = imageRotation;
        return this;
    }

    /**
     * Indicates whether to apply this placemark's image rotation relative to the screen orderedRenderable the globe. If
     * WorldWind.RELATIVE_TO_SCREEN, this placemark's image is rotated in the plane of the screen and its orientation
     * relative to the globe changes as the view changes. If WorldWind.RELATIVE_TO_GLOBE, this placemark's image is
     * rotated in a plane tangent to the globe at this placemark's position and retains its orientation relative to the
     * globe. See {@link gov.nasa.worldwind.WorldWind.OrientationMode}
     */
    @WorldWind.OrientationMode
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
    public Placemark setImageRotationReference(@WorldWind.OrientationMode int imageRotationReference) {
        this.imageRotationReference = imageRotationReference;
        return this;
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
    public Placemark setImageTilt(double imageTilt) {
        this.imageTilt = imageTilt;
        return this;
    }

    /**
     * Indicates whether to apply this placemark's image tilt relative to the screen orderedRenderable the globe. If
     * WorldWind.RELATIVE_TO_SCREEN, this placemark's image is tilted inwards (for positive tilts) relative to the plane
     * of the screen, and its orientation relative to the globe changes as the view changes. If
     * WorldWind.RELATIVE_TO_GLOBE, this placemark's image is tilted towards the globe's surface, and retains its
     * orientation relative to the surface. See {@link gov.nasa.worldwind.WorldWind.OrientationMode}
     */
    @WorldWind.OrientationMode
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
    public Placemark setImageTiltReference(@WorldWind.OrientationMode int imageTiltReference) {
        this.imageTiltReference = imageTiltReference;
        return this;
    }

    public boolean isEnableLeaderLinePicking() {
        return enableLeaderLinePicking;
    }

    public Placemark setEnableLeaderLinePicking(boolean enableLeaderLinePicking) {
        this.enableLeaderLinePicking = enableLeaderLinePicking;
        return this;
    }

    /**
     * Performs the rendering; called by the public render method.
     *
     * @param dc The current DrawContext.
     */
    @Override
    protected void doRender(DrawContext dc) {

        this.determineActiveAttributes(dc);
        if (this.activeAttributes == null) {
            return;
        }

        // Compute the placemark's model point and corresponding distance to the eye point. If the placemark's
        // position is terrain-dependent but off the terrain, then compute it ABSOLUTE so that we have a point for the
        // placemark and are thus able to draw it. Otherwise its image and label portion that are potentially over the
        // terrain won't get drawn, and would disappear as soon as there is no terrain at the placemark's position. This
        // can occur at the window edges.
        this.placePoint = dc.globe.geographicToCartesian(
            this.position.latitude, this.position.longitude, this.position.altitude, this.placePoint);
        // TODO: dc.surfacePointForMode(this.position.latitude, this.position.longitude, this.position.altitude, this.altitudeMode, this.placePoint);

        // Compute the eye distance to the place point, the value which is used for sorting/ordering the OrderedRenderables
        this.eyeDistance = dc.eyePoint.distanceTo(this.placePoint);

        // If a leader line is desired for placemarks off of the terrain surface, we'll need a ground model point for
        // one end of the leader line.  The placePoint is the other end.
        if (this.activeAttributes.drawLeaderLine) {
            // Perform lazy allocation of the vector
            if (this.groundPoint == null) {
                this.groundPoint = new Vec3();
            }
            // Compute the placemark's ground model point.
            this.groundPoint = dc.globe.geographicToCartesian(
                this.position.latitude, this.position.longitude, 0, this.groundPoint);
            // TODO: dc.surfacePointForMode(this.position.latitude, this.position.longitude, 0, this.altitudeMode, this.groundPoint);
        }

        // Get the drawable delegate for this placemark.
        DrawablePlacemark drawable = this.makeDrawablePlacemark(dc);

        // Prepare the drawable for portrayal of this placemark
        if (this.prepareDrawable(drawable, dc) && this.isVisible(drawable, dc)) {

            // Set up the drawable to use World Wind's basic GLSL program.
            drawable.program = (BasicShaderProgram) dc.getShaderProgram(BasicShaderProgram.KEY);
            if (drawable.program == null) {
                drawable.program = (BasicShaderProgram) dc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(dc.resources));
            }

            // Rendering is deferred for ordered renderables -- these renderables will be sorted by eye distance and
            // rendered after the layers are rendered. Simply add this placemark to the collection of ordered
            // renderables for rendering later via Placemark.renderOrdered().
            dc.offerDrawable(drawable, this.eyeDistance);
        }
    }

    /**
     * Determines the placemark attributes that should be used in the current rendering pass.
     *
     * @param dc the current draw context
     */
    protected void determineActiveAttributes(DrawContext dc) {
        if (this.highlighted && this.highlightAttributes != null) {
            this.activeAttributes = this.highlightAttributes;
        } else {
            this.activeAttributes = this.attributes;
        }
    }

    /**
     * Returns an ordered renderable for this placemark. The renderable may be a new instance or an existing instance.
     *
     * @return The DrawablePlacemark to use for rendering.
     */
    protected DrawablePlacemark makeDrawablePlacemark(DrawContext dc) {
        // Create a new instance if necessary, otherwise reuse the existing instance
        // TODO: consider pooling of DrawablePlacemarks
        if (this.drawablePlacemark == null) {
            this.drawablePlacemark = new DrawablePlacemark();
        }
        return this.drawablePlacemark;
    }

    /**
     * Returns an ordered renderable for this placemark. The renderable may be a new instance or an existing instance.
     *
     * @return The DrawablePlacemark to use for rendering; will be null if the placemark cannot or should not be
     * rendered.
     */
    protected boolean prepareDrawable(DrawablePlacemark drawable, DrawContext dc) {

        // Precompute the image rotation and tilt.
        drawable.rotation = this.imageRotationReference == WorldWind.RELATIVE_TO_GLOBE ?
            dc.heading - this.imageRotation : -this.imageRotation;
        drawable.tilt = this.imageTiltReference == WorldWind.RELATIVE_TO_GLOBE ?
            dc.tilt + this.imageTilt : this.imageTilt;

        ////////////////////////
        // Prepare the image
        ////////////////////////

        // Set the color used for the image
        drawable.imageColor.set(this.activeAttributes.imageColor);

        // Set the active texture to use, if applicable, creating it if necessary from the imageSource object.
        if (this.activeAttributes.imageSource != null) {
            drawable.iconTexture = dc.getTexture(this.activeAttributes.imageSource);
            if (drawable.iconTexture == null) {
                drawable.iconTexture = dc.retrieveTexture(this.activeAttributes.imageSource); // puts retrieved textures in the cache
            }
        } else {
            // When there is no imageSource we draw a simple colored square
            drawable.iconTexture = null;
        }

        // Compute the placemark's screen point in the OpenGL coordinate system of the WorldWindow by projecting its
        // model coordinate point onto the viewport. Apply a depth offset in order to cause the entire placemark
        // image to appear above the globe/terrain. When a placemark is displayed near the terrain or the horizon,
        // portions of its geometry are often behind the terrain, yet as a screen element the placemark is expected
        // to be visible. We adjust its depth values rather than moving the placemark itself to avoid obscuring its
        // actual position.
        double depthOffset = Placemark.DEFAULT_DEPTH_OFFSET;
        if (this.eyeDistance < dc.horizonDistance) {
            // Offset the image towards the eye such that whatever the orientation of the image, with respect to the
            // globe, the entire image is guaranteed to be in front of the globe/terrain.
            double longestSide = drawable.iconTexture != null ?
                Math.max(drawable.iconTexture.getImageWidth(), drawable.iconTexture.getImageHeight()) : 1;
            double metersPerPixel = dc.pixelSizeAtDistance(this.eyeDistance);
            depthOffset = longestSide * this.activeAttributes.imageScale * metersPerPixel * -1;
        }

        if (!dc.projectWithDepth(this.placePoint, depthOffset, drawable.screenPlacePoint)) {
            // Probably outside the clipping planes
            return false;
        }

        // Compute an eye-position proximity scaling factor, so that distant placemarks can be scaled smaller than
        // nearer placemarks.
        double visibilityScale = this.isEyeDistanceScaling() ?
            Math.max(this.activeAttributes.minimumImageScale, Math.min(1, this.getEyeDistanceScalingThreshold() / this.eyeDistance)) : 1;

        // Compute the placemark's transform matrix and texture coordinate matrix according to its screen point, image size,
        // image offset and image scale. The image offset is defined with its origin at the image's bottom-left corner and
        // axes that extend up and to the right from the origin point. When the placemark has no active texture the image
        // scale defines the image size and no other scaling is applied.
        if (drawable.iconTexture != null) {
            int w = drawable.iconTexture.getImageWidth();
            int h = drawable.iconTexture.getImageHeight();
            double s = this.activeAttributes.imageScale * visibilityScale;
            Vec2 offset = this.activeAttributes.imageOffset.offsetForSize(w, h);

            drawable.imageTransform.setTranslation(
                drawable.screenPlacePoint.x - offset.x * s,
                drawable.screenPlacePoint.y - offset.y * s,
                drawable.screenPlacePoint.z);

            drawable.imageTransform.setScale(w * s, h * s, 1);

        } else {
            double size = this.activeAttributes.imageScale * visibilityScale;
            Vec2 offset = this.activeAttributes.imageOffset.offsetForSize(size, size);

            drawable.imageTransform.setTranslation(
                drawable.screenPlacePoint.x - offset.x,
                drawable.screenPlacePoint.y - offset.y,
                drawable.screenPlacePoint.z);

            drawable.imageTransform.setScale(size, size, 1);
        }

        /////////////////////////////////////
        // Prepare the optional leader line
        ////////////////////////////////////

        drawable.drawLeader = this.mustDrawLeaderLine(dc);
        if (drawable.drawLeader) {
            if (drawable.leaderColor == null) {
                drawable.leaderColor = new Color(this.activeAttributes.leaderLineAttributes.outlineColor);
            } else {
                drawable.leaderColor.set(this.activeAttributes.leaderLineAttributes.outlineColor);
            }

            drawable.leaderWidth = this.activeAttributes.leaderLineAttributes.outlineWidth;
            drawable.enableLeaderPicking = this.isEnableLeaderLinePicking();

            // Perform lazy allocation of vector resources
            if (drawable.screenGroundPoint == null) {
                drawable.screenGroundPoint = new Vec3();
            }

            // Compute the ground point's screen point, using the the same depthOffset as used for the placePoint
            // to ensure the proper depth with relation to other placemarks.
            if (!dc.projectWithDepth(this.groundPoint, depthOffset, drawable.screenGroundPoint)) {
                // Probably outside the clipping planes, don't draw the leader, but continue
                // drawing the other placemark elements.
                drawable.drawLeader = false;
            }
        }

        return true;
    }

    /**
     * Computes the bounding boxes and determines if the placemark's image or label are visible.
     *
     * @return True if the image, label and/or leader-line intercept the viewport.
     */
    protected boolean isVisible(DrawablePlacemark drawable, DrawContext dc) {
        // Compute the bounding boxes in screen coordinates
        Rect imageBounds = (drawable.imageTransform) == null ? null : WWMath.boundingRectForUnitQuad(drawable.imageTransform);

        return (imageBounds != null && Rect.intersects(imageBounds, dc.viewport))
            || (this.mustDrawLeaderLine(dc) && dc.frustum.intersectsSegment(this.groundPoint, this.placePoint));
    }

    /**
     * Determines if a label should and can be drawn.
     *
     * @return True if there is a valid label and label attributes.
     */

    protected boolean mustDrawLabel(DrawContext dc) {
        return this.label != null && !this.label.isEmpty() && this.activeAttributes.labelAttributes != null;
    }

    /**
     * Determines if a leader-line should and can be drawn.
     *
     * @return True if leader-line directive is enabled and there are valid leader-line attributes.
     */
    protected boolean mustDrawLeaderLine(DrawContext dc) {
        return this.activeAttributes.drawLeaderLine && this.activeAttributes.leaderLineAttributes != null
            && (!dc.pickingMode || this.enableLeaderLinePicking);
    }
}
