/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.graphics.Rect;
import android.graphics.Typeface;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.draw.DrawablePlacemark;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec2;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.Texture;
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
public class Placemark extends AbstractRenderable implements OrderedRenderable {

    /**
     * The default eye distance above which to reduce the size of this placemark, in meters. If {@link
     * Placemark#setEyeDistanceScaling(boolean)} is true, this placemark's image, label and leader line sizes are
     * reduced as the eye distance increases beyond this threshold.
     */
    protected static final double DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD = 1e6;

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
     * The Drawable implementation for this placemark.
     */
    protected DrawablePlacemark drawablePlacemark = null;

    private double eyeDistance = 0;

    private Vec3 placePoint = new Vec3(0, 0, 0);

    private Vec3 groundPoint = null;  // will be created if a leader line must be drawn

    private static final double DEFAULT_DEPTH_OFFSET = -0.003;

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
        this.highlightAttributes = null;
        this.highlighted = false;
        this.eyeDistanceScaling = eyeDistanceScaling;
        this.eyeDistanceScalingThreshold = DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD;
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
     * Returns the placemark attributes that should be used in the next rendering pass.
     *
     * @param dc The current DrawContext.
     */
    public PlacemarkAttributes getActiveAttributes(DrawContext dc) {
        return this.attributes; // TODO interpret highlighted state
    }

    /**
     * Performs the rendering; called by the public render method.
     *
     * @param dc The current DrawContext.
     */
    @Override
    protected void doRender(DrawContext dc) {

        PlacemarkAttributes activeAttributes = this.getActiveAttributes(dc);

        // Compute the placemark's model point and corresponding distance to the eye point. If the placemark's
        // position is terrain-dependent but off the terrain, then compute it ABSOLUTE so that we have a point for
        // the placemark and are thus able to draw it. Otherwise its image and label portion that are potentially
        // over the terrain won't get drawn, and would disappear as soon as there is no terrain at the placemark's
        // position. This can occur at the window edges.
        this.placePoint = dc.globe.geographicToCartesian(
            this.position.latitude, this.position.longitude, this.position.altitude, this.placePoint);
        // TODO: dc.surfacePointForMode(this.position.latitude, this.position.longitude, this.position.altitude, this.altitudeMode, this.placePoint);

        // Compute the eye distance to the place point, the value which is used for sorting/ordering the OrderedRenderables
        this.eyeDistance = dc.eyePoint.distanceTo(this.placePoint);

        // If a leader line is desired for placemarks off of the terrain surface, we'll need a ground model point for
        // one end of the leader line.  The placePoint is the other end.
        if (activeAttributes.drawLeaderLine) {
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
            // Rendering is deferred for ordered renderables -- these renderables will be sorted
            // by eye distance and rendered after the layers are rendered. Simply add this placemark
            // to the collection of ordered renderables for rendering later via Placemark.renderOrdered().
            dc.offerOrderedRenderable(this, this.eyeDistance);
        }
    }

    /**
     * Renders the ordered placemark.
     */
    @Override
    public void renderOrdered(DrawContext dc) {
        this.drawablePlacemark.draw(dc);
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

        // Get a reference to the attributes to use in the next drawing pass.
        PlacemarkAttributes activeAttributes = this.getActiveAttributes(dc);

        // Set the (optional) label. May be null.
        drawable.label = this.getLabel();

        // Precompute the image rotation and tilt.
        drawable.actualRotation = this.imageRotationReference == WorldWind.RELATIVE_TO_GLOBE ?
            dc.heading - this.imageRotation : -this.imageRotation;
        drawable.actualTilt = this.imageTiltReference == WorldWind.RELATIVE_TO_GLOBE ?
            dc.tilt + this.imageTilt : this.imageTilt;

        ////////////////////////
        // Prepare the image
        ////////////////////////

        // Set the color used for the image
        drawable.imageColor.set(activeAttributes.imageColor);

        // Set the active texture to use, if applicable, creating it if necessary from the imageSource object.
        if (activeAttributes.imageSource != null) {
            drawable.activeTexture = dc.getTexture(activeAttributes.imageSource);
            if (drawable.activeTexture == null) {
                drawable.activeTexture = dc.retrieveTexture(activeAttributes.imageSource); // puts retrieved textures in the cache
            }
        } else {
            // When there is no imageSource we draw a simple colored square
            drawable.activeTexture = null;
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
            double longestSide = drawable.activeTexture != null ?
                Math.max(drawable.activeTexture.getImageWidth(), drawable.activeTexture.getImageHeight()) : 1;
            double metersPerPixel = dc.pixelSizeAtDistance(this.eyeDistance);
            depthOffset = longestSide * activeAttributes.imageScale * metersPerPixel * -1;
        }

        if (!dc.projectWithDepth(this.placePoint, depthOffset, drawable.screenPlacePoint)) {
            // Probably outside the clipping planes
            return false;
        }

        // Use World Wind's basic GLSL program.
        drawable.program = (BasicShaderProgram) dc.getShaderProgram(BasicShaderProgram.KEY);
        if (drawable.program == null) {
            drawable.program = (BasicShaderProgram) dc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(dc.resources));
        }

        // Compute an eye-position proximity scaling factor, so that distant placemarks can be scaled smaller than
        // nearer placemarks.
        double visibilityScale = this.isEyeDistanceScaling() ?
            Math.max(activeAttributes.minimumImageScale, Math.min(1, this.getEyeDistanceScalingThreshold() / this.eyeDistance)) : 1;

        // Compute the placemark's transform matrix and texture coordinate matrix according to its screen point, image size,
        // image offset and image scale. The image offset is defined with its origin at the image's bottom-left corner and
        // axes that extend up and to the right from the origin point. When the placemark has no active texture the image
        // scale defines the image size and no other scaling is applied.
        if (drawable.activeTexture != null) {
            int w = drawable.activeTexture.getImageWidth();
            int h = drawable.activeTexture.getImageHeight();
            double s = activeAttributes.imageScale * visibilityScale;
            Vec2 offset = activeAttributes.imageOffset.offsetForSize(w, h);

            drawable.imageTransform.setTranslation(
                drawable.screenPlacePoint.x - offset.x * s,
                drawable.screenPlacePoint.y - offset.y * s,
                drawable.screenPlacePoint.z);

            drawable.imageTransform.setScale(w * s, h * s, 1);

        } else {
            double size = activeAttributes.imageScale * visibilityScale;
            Vec2 offset = activeAttributes.imageOffset.offsetForSize(size, size);

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
                drawable.leaderColor = new Color(activeAttributes.leaderLineAttributes.outlineColor);
            } else {
                drawable.leaderColor.set(activeAttributes.leaderLineAttributes.outlineColor);
            }
            drawable.leaderWidth = activeAttributes.leaderLineAttributes.outlineWidth;
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

        /////////////////////////
        // Prepare the label
        /////////////////////////

        // If there's a label, perform these same operations for the label texture, creating that texture if it
        // doesn't already exist.
        drawable.drawLabel = this.mustDrawLabel(dc);
        if (drawable.drawLabel) {
            if (drawable.labelColor == null) {
                drawable.labelColor = new Color(activeAttributes.labelAttributes.color);
            } else {
                drawable.labelColor.set(activeAttributes.labelAttributes.color);
            }

            Typeface labelFont = this.attributes.labelAttributes.font;
            String labelKey = this.label + labelFont.toString();

            drawable.labelTexture = (Texture) dc.renderResourceCache.get(labelKey);
            if (drawable.labelTexture == null) {
                // TODO: Create the label bitmap and texture
                //this.labelTexture = dc.createFontTexture(Placemark.this.displayName, labelFont, false);
            }
            if (drawable.labelTexture != null) {

                if (drawable.labelTransform == null) {
                    drawable.labelTransform = new Matrix4();
                }
                int w = drawable.labelTexture.getImageWidth();
                int h = drawable.labelTexture.getImageHeight();
                double s = this.attributes.labelAttributes.scale * visibilityScale;
                Vec2 offset = this.attributes.labelAttributes.offset.offsetForSize(w, h);

                drawable.labelTransform.setTranslation(
                    drawable.screenPlacePoint.x - offset.x * s,
                    drawable.screenPlacePoint.y - offset.y * s,
                    drawable.screenPlacePoint.z);

                drawable.labelTransform.setScale(w * s, h * s, 1);
            }
        }

        return true;
    }

    /**
     * Computes the bounding boxes and determines if the placemark's image or label are visible.
     *
     * @return True if the image, label and/or leader-line intercept the viewport.
     */
    public boolean isVisible(DrawablePlacemark drawable, DrawContext dc) {
        // Compute the bounding boxes in screen coordinates
        Rect imageBounds = drawable.imageTransform == null ? null : WWMath.boundingRectForUnitQuad(drawable.imageTransform);
        Rect labelBounds = drawable.labelTransform == null ? null : WWMath.boundingRectForUnitQuad(drawable.labelTransform);

        // Determine visibility by testing the bounding boxes for intersection with the viewport
        if (dc.pickingMode) {
            return imageBounds != null && imageBounds.intersect(dc.viewport); // TODO: pickRectangle
//                return dc.pickRectangle && (this.imageBounds.intersects(dc.pickRectangle)
//                    || (this.mustDrawLabel() && this.labelBounds.intersects(dc.pickRectangle))
//                    || (this.mustDrawLeaderLine(dc)
//                    && dc.pickFrustum.intersectsSegment(this.groundPoint, this.placePoint)));
        } else {
            return (imageBounds != null && imageBounds.intersect(dc.viewport))
                || (mustDrawLabel(dc) && labelBounds != null && labelBounds.intersect(dc.viewport))
                || (mustDrawLeaderLine(dc) && dc.frustum.intersectsSegment(this.groundPoint, this.placePoint));
        }
    }

    /**
     * Determines if a label should and can be drawn.
     *
     * @return True if there is a valid label and label attributes.
     */

    public boolean mustDrawLabel(DrawContext dc) {
        PlacemarkAttributes activeAttributes = this.getActiveAttributes(dc);
        return this.label != null && !this.label.isEmpty() && activeAttributes.labelAttributes != null;
    }

    /**
     * Determines if a leader-line should and can be drawn.
     *
     * @return True if leader-line directive is enabled and there are valid leader-line attributes.
     */
    public boolean mustDrawLeaderLine(DrawContext dc) {
        PlacemarkAttributes activeAttributes = this.getActiveAttributes(dc);
        return activeAttributes.drawLeaderLine && activeAttributes.leaderLineAttributes != null
            && (!dc.pickingMode || this.enableLeaderLinePicking);
    }
}
