/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import android.graphics.Rect;

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
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWMath;

/**
 * Represents a Placemark shape. A placemark displays an image, a label and a leader line connecting the placemark's
 * geographic position to the ground. All three of these items are optional. By default, the leader line is not
 * pickable. See {@link Placemark#setEnableLeaderLinePicking(boolean)}.
 * <p>
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

    private static final double DEFAULT_DEPTH_OFFSET = -0.1;

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

    /**
     * The attributes identified for use during the current render pass.
     */
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

    private Vec3 screenPlacePoint = new Vec3();

    private Vec3 groundPoint = null; // will be allocated if a leader line must be drawn

    /**
     * This factory method creates a Placemark and an associated PlacemarkAttributes bundle that draws a simple square
     * centered on the supplied position with the given size and color.
     *
     * @param position  The geographic position where the placemark is drawn.
     * @param color     The color of the placemark.
     * @param pixelSize The width and height of the placemark.
     *
     * @return A new Placemark with a PlacemarkAttributes bundle.
     */
    public static Placemark simple(Position position, Color color, int pixelSize) {
        return new Placemark(position, PlacemarkAttributes.defaults().setImageColor(color).setImageScale(pixelSize));
    }


    /**
     * This factory method creates a Placemark and an associated PlacemarkAttributes bundle that draws the given image
     * centered on the supplied position.
     *
     * @param position    The geographic position with the placemark is drawn.
     * @param imageSource The object containing the image that is drawn.
     *
     * @return A new Placemark with a PlacemarkAttributes bundle.
     */
    public static Placemark simpleImage(Position position, ImageSource imageSource) {
        return new Placemark(position, PlacemarkAttributes.withImage(imageSource));
    }

    /**
     * This factory method creates a Placemark and an associated PlacemarkAttributes bundle (with TextAttributes) that
     * draws the given image centered on the supplied position with a nearby label.
     *
     * @param position    The geographic position with the placemark is drawn.
     * @param imageSource The object containing the image that is drawn.
     * @param label       The text that is drawn near the image. This parameter becomes the placemark's displayName
     *                    property.
     *
     * @return A new Placemark with a PlacemarkAttributes bundle containing TextAttributes.
     */
    public static Placemark simpleImageAndLabel(Position position, ImageSource imageSource, String label) {
        return new Placemark(position, PlacemarkAttributes.withImageAndLabel(imageSource), label);
    }


    /**
     * Constructs a Placemark that draws its representation at the supplied position using the given PlacemarkAttributes
     * bundle. The displayName and label properties are empty.
     *
     * @param position   The geographic position with the placemark is drawn.
     * @param attributes The attributes bundle reference that defines how the placemark is drawn.
     */
    public Placemark(Position position, PlacemarkAttributes attributes) {
        this(position, attributes, null, null);
    }

    /**
     * Constructs a Placemark with a label that draws its representation at the supplied position using the given
     * PlacemarkAttributes bundle. The displayName property is set to the supplied label string, which is drawn
     * according to the (optional) TextAttributes within the PlacemarkAttributes.
     *
     * @param position   The geographic position with the placemark is drawn.
     * @param attributes The attributes bundle reference that defines how the placemark and label are drawn.
     * @param label      The text assigned to the displayName property that is optionally drawn near the image.
     */
    public Placemark(Position position, PlacemarkAttributes attributes, String label) {
        this(position, attributes, label, null);
    }

    /**
     * Constructs a placemark.
     *
     * @param position    The placemark's geographic position.
     * @param attributes  The attributes to associate with this placemark. May be null, but if null the placemark will
     *                    not be drawn.
     * @param displayName The display name associated with this placemark. May be null. If a name is specified, but a
     *                    label is not, then the name will be used for the label.
     * @param label       The label associated with this placemark. May be null. If specified, the label will be drawn
     *                    with the active {@link PlacemarkAttributes#labelAttributes}. If these attributes are null, the
     *                    label will not be drawn.
     */
    public Placemark(Position position, PlacemarkAttributes attributes, String displayName, String label) {
        this.setPosition(position);
        this.setAltitudeMode(WorldWind.ABSOLUTE);
        this.setDisplayName(displayName);
        this.setLabel(label);
        this.attributes = attributes;
        this.eyeDistanceScaling = false;
        this.eyeDistanceScalingThreshold = DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD;
        this.eyeDistanceScalingLabelThreshold = 1.5 * this.eyeDistanceScalingThreshold;
        this.imageRotationReference = WorldWind.RELATIVE_TO_SCREEN;
        this.imageTiltReference = WorldWind.RELATIVE_TO_SCREEN;
    }

    /**
     * Gets this placemark's geographic position.
     *
     * @return The geographic position where this placemark is drawn.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets this placemark's geographic position to the values in the supplied position.
     *
     * @param position The new position where this placemark will be drawn.
     *
     * @return This placemark.
     */
    public Placemark setPosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Placemark", "setPosition", "missingPosition"));
        }
        if (this.position == null) {
            this.position = new Position(position);
        } else {
            this.position.set(position);
        }
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
     * Sets this placemark's altitude mode.
     *
     * @param altitudeMode The new altitude mode. See {@link gov.nasa.worldwind.WorldWind.AltitudeMode} for acceptable
     *                     values
     *
     * @return This placemark.
     */
    public Placemark setAltitudeMode(@WorldWind.AltitudeMode int altitudeMode) {
        this.altitudeMode = altitudeMode;
        return this;
    }

    /**
     * Gets the placemark's "normal" attributes, that is the attributes used when the placemark's highlighted flag is
     * false. If null and this placemark is not highlighted, this placemark is not drawn.
     *
     * @return A reference to this placemark's attributes bundle.
     */
    public PlacemarkAttributes getAttributes() {
        return attributes;
    }

    /**
     * Sets the placemark's attributes to the supplied attributes bundle. If null and this placemark is not highlighted,
     * this placemark is not drawn.
     * <p>
     * It is permissible to share attribute bundles between placemarks.
     *
     * @param attributes A reference to an attributes bundle used by this placemark when not highlighted.
     *
     * @return This placemark.
     */
    public Placemark setAttributes(PlacemarkAttributes attributes) {
        this.attributes = attributes;
        return this;
    }

    /**
     * Gets the attributes used when this placemark's highlighted flag is true. If null and the highlighted flag is
     * true, this placemark's normal attributes are used. If they, too, are null, this placemark is not drawn.
     *
     * @return A reference to this placemark's highlight attributes bundle.
     */
    public PlacemarkAttributes getHighlightAttributes() {
        return highlightAttributes;
    }

    /**
     * Sets the attributes used when this placemark's highlighted flag is true. If null and the highlighted flag is
     * true, this placemark's normal attributes are used. If they, too, are null, this placemark is not drawn.
     * <p>
     * It is permissible to share attribute bundles between placemarks.
     *
     * @param highlightAttributes A reference to the attributes bundle used by this placemark when highlighted.
     *
     * @return This placemark.
     */
    public Placemark setHighlightAttributes(PlacemarkAttributes highlightAttributes) {
        this.highlightAttributes = highlightAttributes;
        return this;
    }

    /**
     * Gets the text used to label this placemark on the globe. If null, then the {@link Placemark#displayName} property
     * is used for the label.
     *
     * @return The text used to label a placemark on the globe when labels are enabled.
     */
    public String getLabel() {
        if (this.label == null) {
            return this.getDisplayName();
        }
        return label;
    }

    /**
     * Sets the text used for this placemark's label on the globe. If non-null, then this property will be used for the
     * label in lieu of the displayName. If null, then the {@link Placemark#displayName} property is used for the label
     * text.
     * <p>
     * A typical use case is to use the displayName property in lists of placemarks and use the label property for the
     * placemark labels on the globe, allowing for short or abbreviated names to be used on a cluttered globe.
     *
     * @param label The new label text. If set, this value supersedes the use of {@link Placemark#displayName} property
     *              for the label. An empty string is permissible.
     *
     * @return This placemark.
     */
    public Placemark setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Indicates whether this placemark uses its highlight attributes rather than its normal attributes.
     *
     * @return True if this placemark should be highlighted.
     */
    public boolean isHighlighted() {
        return highlighted;
    }

    /**
     * Sets the highlighted state of this placemark, which indicates whether this placemark uses its highlight
     * attributes rather than its normal attributes.
     *
     * @param highlighted The highlighted state applied to this placemark.
     *
     * @return This placemark.
     */
    public Placemark setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        return this;
    }

    /**
     * Indicates whether this placemark's size is reduced at higher eye distances. If true, this placemark's size is
     * scaled inversely proportional to the eye distance if the eye distance is greater than the value of the {@link
     * Placemark#getEyeDistanceScalingThreshold()} property. When the eye distance is below the threshold, this
     * placemark is scaled only according to the {@link PlacemarkAttributes#getImageScale()}.
     *
     * @return True if eye distance scaling should be applied.
     */
    public boolean isEyeDistanceScaling() {
        return eyeDistanceScaling;
    }

    /**
     * Enables or disables the eye distance scaling feature for this placemark. When enabled, the placemark's size is
     * reduced at higher eye distances. If true, this placemark's size is scaled inversely proportional to the eye
     * distance if the eye distance is greater than the value of the {@link Placemark#getEyeDistanceScalingThreshold()}
     * property. When the eye distance is below the threshold, this placemark is scaled only according to the {@link
     * PlacemarkAttributes#getImageScale()}.
     *
     * @param eyeDistanceScaling The new state for the eye distance scaling feature.
     *
     * @return This placemark.
     */
    public Placemark setEyeDistanceScaling(boolean eyeDistanceScaling) {
        this.eyeDistanceScaling = eyeDistanceScaling;
        return this;
    }

    /**
     * Gets the eye distance above which to reduce the size of this placemark, in meters. If {@link
     * Placemark#isEyeDistanceScaling()} is true, this placemark's image, label and leader line sizes are reduced as the
     * eye distance increases beyond this threshold.
     *
     * @return The current threshold value, in meters.
     */
    public double getEyeDistanceScalingThreshold() {
        return eyeDistanceScalingThreshold;
    }

    /**
     * Sets the eye distance above which to reduce the size of this placemark, in meters. If {@link
     * Placemark#isEyeDistanceScaling()} is true, this placemark's image, label and leader line sizes are reduced as the
     * eye distance increases beyond this threshold.
     *
     * @param eyeDistanceScalingThreshold The new threshold value, in meters, used to determine if eye distance scaling
     *                                    should be applied.
     *
     * @return This placemark.
     */
    public Placemark setEyeDistanceScalingThreshold(double eyeDistanceScalingThreshold) {
        this.eyeDistanceScalingThreshold = eyeDistanceScalingThreshold;
        return this;
    }

    /**
     * Gets the eye altitude, in meters, above which this placemark's label is not displayed.
     *
     * @return The current label scaling threshold.
     */
    public double getEyeDistanceScalingLabelThreshold() {
        return eyeDistanceScalingLabelThreshold;
    }

    /**
     * Sets the eye altitude, in meters, above which this placemark's label is not displayed.
     *
     * @param eyeDistanceScalingLabelThreshold The new threshold value, in meters, used to determine if eye distance
     *                                         label scaling should be applied.
     *
     * @return This placemark.
     */
    public Placemark setEyeDistanceScalingLabelThreshold(double eyeDistanceScalingLabelThreshold) {
        this.eyeDistanceScalingLabelThreshold = eyeDistanceScalingLabelThreshold;
        return this;
    }

    /**
     * Gets the amount of rotation to apply to the image, measured in degrees clockwise and relative to this placemark's
     * {@link Placemark#getImageRotationReference()}.
     *
     * @return The current image rotation value in degrees.
     */
    public double getImageRotation() {
        return imageRotation;
    }

    /**
     * Sets the amount of rotation to apply to the image, measured in degrees clockwise and relative to this placemark's
     * {@link Placemark#getImageRotationReference()}.
     *
     * @param imageRotation The amount in degrees to rotate the image. Zero is no rotation.
     *
     * @return This placemark.
     */
    public Placemark setImageRotation(double imageRotation) {
        this.imageRotation = imageRotation;
        return this;
    }

    /**
     * Gets the type of rotation to apply if the {@link Placemark#getImageRotation()} is not zero. This value indicates
     * whether to apply this placemark's image rotation relative to the screen or the globe.
     * <p>
     * If {@link WorldWind#RELATIVE_TO_SCREEN}, this placemark's image is rotated in the plane of the screen and its
     * orientation relative to the globe changes as the view changes. If {@link WorldWind#RELATIVE_TO_GLOBE}, this
     * placemark's image is rotated in a plane tangent to the globe at this placemark's position and retains its
     * orientation relative to the globe.
     *
     * @return The {@link gov.nasa.worldwind.WorldWind.OrientationMode} to use when image rotation applied.
     */
    @WorldWind.OrientationMode
    public int getImageRotationReference() {
        return imageRotationReference;
    }

    /**
     * Sets the type of rotation to apply if the {@link Placemark#getImageRotation()} is not zero. This value indicates
     * whether to apply this placemark's image rotation relative to the screen or the globe.
     * <p>
     * If {@link WorldWind#RELATIVE_TO_SCREEN}, this placemark's image is rotated in the plane of the screen and its
     * orientation relative to the globe changes as the view changes. If {@link WorldWind#RELATIVE_TO_GLOBE}, this
     * placemark's image is rotated in a plane tangent to the globe at this placemark's position and retains its
     * orientation relative to the globe.
     *
     * @param imageRotationReference The {@link gov.nasa.worldwind.WorldWind.OrientationMode} to use when image rotation
     *                               applied.
     *
     * @return This placemark.
     */
    public Placemark setImageRotationReference(@WorldWind.OrientationMode int imageRotationReference) {
        this.imageRotationReference = imageRotationReference;
        return this;
    }

    /**
     * Gets the amount of tilt to apply to the image, measured in degrees away from the eye point and relative to this
     * placemark's {@link Placemark#getImageTiltReference()}. While any positive or negative number may be specified,
     * values outside the range [0. 90] cause some or all of the image to be clipped.
     *
     * @return The amount, in degrees, to tilt the image.
     */
    public double getImageTilt() {
        return imageTilt;
    }

    /**
     * Sets the amount of tilt to apply to the image, measured in degrees away from the eye point and relative to this
     * placemark's {@link Placemark#getImageTiltReference()}. While any positive or negative number may be specified,
     * values outside the range [0. 90] cause some or all of the image to be clipped.
     *
     * @param imageTilt The amount, in degrees, to tilt the image.
     *
     * @return This placemark.
     */
    public Placemark setImageTilt(double imageTilt) {
        this.imageTilt = imageTilt;
        return this;
    }

    /**
     * Gets the type tilt to apply when {@link Placemark#getImageTilt()} is non-zero. This value indicates whether to
     * apply this placemark's image tilt relative to the screen or the globe.
     * <p>
     * If {@link WorldWind#RELATIVE_TO_SCREEN}, this placemark's image is tilted inwards (for positive tilts) relative
     * to the plane of the screen, and its orientation relative to the globe changes as the view changes. If {@link
     * WorldWind#RELATIVE_TO_GLOBE}, this placemark's image is tilted towards the globe's surface, and retains its
     * orientation relative to the surface.
     *
     * @return The {@link gov.nasa.worldwind.WorldWind.OrientationMode} to use when the image is tilted.
     */
    @WorldWind.OrientationMode
    public int getImageTiltReference() {
        return imageTiltReference;
    }

    /**
     * Sets the type tilt to apply when {@link Placemark#getImageTilt()} is non-zero. This value indicates whether to
     * apply this placemark's image tilt relative to the screen or the globe.
     * <p>
     * If {@link WorldWind#RELATIVE_TO_SCREEN}, this placemark's image is tilted inwards (for positive tilts) relative
     * to the plane of the screen, and its orientation relative to the globe changes as the view changes. If {@link
     * WorldWind#RELATIVE_TO_GLOBE}, this placemark's image is tilted towards the globe's surface, and retains its
     * orientation relative to the surface.
     *
     * @param imageTiltReference The {@link gov.nasa.worldwind.WorldWind.OrientationMode} to use when the image is
     *                           tilted.
     *
     * @return This placemark.
     */
    public Placemark setImageTiltReference(@WorldWind.OrientationMode int imageTiltReference) {
        this.imageTiltReference = imageTiltReference;
        return this;
    }

    /**
     * Indicates if picking is allowed on this placemark's (optional) leader line.
     *
     * @return True if leader line picking is enable.
     */
    public boolean isEnableLeaderLinePicking() {
        return enableLeaderLinePicking;
    }

    /**
     * Sets whether picking is allowed on this placemark's (optional) leader line.
     *
     * @param enableLeaderLinePicking True if leader line picking should be enabled.
     *
     * @return This placemark.
     */
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

        // Compute the placemark's Cartesian model point.
        // TODO: dc.surfacePointForMode
        dc.globe.geographicToCartesian(
            this.position.latitude, this.position.longitude, this.position.altitude, this.placePoint);

        // Compute the eye distance to the place point, the value which is used for ordering the placemark drawable and
        // determining the amount of depth offset to apply.
        this.eyeDistance = dc.eyePoint.distanceTo(this.placePoint);

        double depthOffset = 0;
        if (this.eyeDistance < dc.horizonDistance) {
            depthOffset = DEFAULT_DEPTH_OFFSET;
        }

        // Project the placemark's model point to screen coordinates. Use a screen depth offset appropriate for the
        // current viewing distance.
        if (!dc.projectWithDepth(this.placePoint, depthOffset, this.screenPlacePoint)) {
            return; // clipped by the near plane or the far plane
        }

        // Obtain a drawable delegate for this placemark.
        DrawablePlacemark drawable = this.makeDrawablePlacemark(dc);

        // Prepare the drawable icon properties.
        this.prepareDrawableIcon(dc, drawable);

        // If a leader line is desired for placemarks off of the terrain surface, prepare the drawable leader properties.
        drawable.drawLeader = this.mustDrawLeaderLine(dc);
        if (drawable.drawLeader) {
            // Compute the ground model point for one end of the leader line. The placePoint is the other end.
            // TODO: use dc.surfacePointForMode
            this.groundPoint = dc.globe.geographicToCartesian(this.position.latitude, this.position.longitude, 0,
                (this.groundPoint != null) ? this.groundPoint : new Vec3());

            // Prepare the drawable leader line properties.
            this.prepareDrawableLeader(dc, drawable);
        }

        if (this.isVisible(dc, drawable)) {
            // Set up the drawable to use World Wind's basic GLSL program.
            drawable.program = (BasicShaderProgram) dc.getShaderProgram(BasicShaderProgram.KEY);
            if (drawable.program == null) {
                drawable.program = (BasicShaderProgram) dc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(dc.resources));
            }

            // Enqueue the drawable placemark for processing on the OpenGL thread.
            dc.offerDrawable(drawable, WorldWind.SHAPE_DRAWABLE, this.eyeDistance);
        } else {
            // The drawable will not be used; recycle it.
            drawable.recycle();
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
     * Prepares this placemark's icon or symbol to for drawing in a subsequent drawing pass.
     * <p>
     * Implementations must be careful not to leak resources from Placemark into the Drawable.
     *
     * @param dc       The current DrawContext.
     * @param drawable The Drawable to be prepared.
     */
    protected void prepareDrawableIcon(DrawContext dc, DrawablePlacemark drawable) {
        // Set the color used for the icon.
        drawable.iconColor.set(this.activeAttributes.imageColor);

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

        // Compute an eye-position proximity scaling factor, so that distant placemarks can be scaled smaller than
        // nearer placemarks.
        double visibilityScale = this.isEyeDistanceScaling() ?
            Math.max(this.activeAttributes.minimumImageScale, Math.min(1, this.getEyeDistanceScalingThreshold() / this.eyeDistance)) : 1;

        // Compute the icon's modelview-projection matrix, beginning with the draw context's screen projection.
        drawable.iconMvpMatrix.set(dc.screenProjection);

        // Apply the icon's translation and scale according to the image size, image offset and image scale. The image
        // offset is defined with its origin at the image's bottom-left corner and axes that extend up and to the right
        // from the origin point. When the placemark has no active texture the image scale defines the image size and no
        // other scaling is applied.
        if (drawable.iconTexture != null) {
            int w = drawable.iconTexture.getImageWidth();
            int h = drawable.iconTexture.getImageHeight();
            double s = this.activeAttributes.imageScale * visibilityScale;
            Vec2 offset = this.activeAttributes.imageOffset.offsetForSize(w, h);

            drawable.iconMvpMatrix.multiplyByTranslation(
                this.screenPlacePoint.x - offset.x * s,
                this.screenPlacePoint.y - offset.y * s,
                this.screenPlacePoint.z);

            drawable.iconMvpMatrix.multiplyByScale(w * s, h * s, 1);

            drawable.iconTexCoordMatrix.set(drawable.iconTexture.getTexCoordTransform());

        } else {
            double size = this.activeAttributes.imageScale * visibilityScale;
            Vec2 offset = this.activeAttributes.imageOffset.offsetForSize(size, size);

            drawable.iconMvpMatrix.multiplyByTranslation(
                this.screenPlacePoint.x - offset.x,
                this.screenPlacePoint.y - offset.y,
                this.screenPlacePoint.z);

            drawable.iconMvpMatrix.multiplyByScale(size, size, 1);
        }

        // ... perform image rotation
        if (this.imageRotation != 0) {
            double rotation = this.imageRotationReference == WorldWind.RELATIVE_TO_GLOBE ?
                dc.heading - this.imageRotation : -this.imageRotation;
            drawable.iconMvpMatrix.multiplyByTranslation(0.5, 0.5, 0);
            drawable.iconMvpMatrix.multiplyByRotation(0, 0, 1, rotation);
            drawable.iconMvpMatrix.multiplyByTranslation(-0.5, -0.5, 0);
        }

        // ... and perform the tilt so that the image tilts back from its base into the view volume.
        if (this.imageTilt != 0) {
            double tilt = this.imageTiltReference == WorldWind.RELATIVE_TO_GLOBE ?
                dc.tilt + this.imageTilt : this.imageTilt;
            drawable.iconMvpMatrix.multiplyByRotation(-1, 0, 0, tilt);
        }
    }

    /**
     * Prepares this placemark's leader line to for drawing in a subsequent drawing pass.
     * <p>
     * Implementations must be careful not to leak resources from Placemark into the Drawable.
     *
     * @param dc       The current DrawContext.
     * @param drawable The Drawable to be prepared.
     */
    protected void prepareDrawableLeader(DrawContext dc, DrawablePlacemark drawable) {
        // Allocate drawable leader properties that are null unless a leader is to be drawn.
        if (drawable.leaderColor == null) {
            drawable.leaderColor = new Color();
        }

        if (drawable.leaderMvpMatrix == null) {
            drawable.leaderMvpMatrix = new Matrix4();
        }

        if (drawable.leaderVertexPoint == null) {
            drawable.leaderVertexPoint = new float[6];
        }

        // Set the leader's line width and color.
        drawable.leaderWidth = this.activeAttributes.leaderLineAttributes.outlineWidth;
        drawable.leaderColor.set(this.activeAttributes.leaderLineAttributes.outlineColor);

        // Set the leader's depth testing and picking enabled states.
        drawable.enableLeaderDepthTest = this.activeAttributes.leaderLineAttributes.depthTest;
        drawable.enableLeaderPicking = this.enableLeaderLinePicking;

        // Compute the leader's modelview-projection matrix, relative to the placemark's ground point.
        drawable.leaderMvpMatrix.set(dc.modelviewProjection);
        drawable.leaderMvpMatrix.multiplyByTranslation(this.groundPoint.x, this.groundPoint.y, this.groundPoint.z);

        // Compute the leader's vertex point coordinates, relative to the placemark's ground point.
        drawable.leaderVertexPoint[0] = 0; // groundPoint.x - groundPoint.x
        drawable.leaderVertexPoint[1] = 0; // groundPoint.y - groundPoint.y
        drawable.leaderVertexPoint[2] = 0; // groundPoint.z - groundPoint.z
        drawable.leaderVertexPoint[3] = (float) (this.placePoint.x - this.groundPoint.x);
        drawable.leaderVertexPoint[4] = (float) (this.placePoint.y - this.groundPoint.y);
        drawable.leaderVertexPoint[5] = (float) (this.placePoint.z - this.groundPoint.z);
    }

    /**
     * Computes the bounding boxes and determines if the placemark's image or label are visible.
     *
     * @return True if the image, label and/or leader-line intercept the viewport.
     */
    protected boolean isVisible(DrawContext dc, DrawablePlacemark drawable) {
        // Compute the bounding boxes in screen coordinates
        Rect imageBounds = WWMath.boundingRectForUnitQuad(drawable.iconMvpMatrix); // TODO allocation

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
