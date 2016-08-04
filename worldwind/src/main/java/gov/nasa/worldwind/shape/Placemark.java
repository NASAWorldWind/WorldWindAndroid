/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.draw.DrawableLines;
import gov.nasa.worldwind.draw.DrawableScreenTexture;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec2;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.geom.Viewport;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.BasicShaderProgram;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.WWMath;

/**
 * Represents a Placemark shape. A placemark displays an image, a label and a leader connecting the placemark's
 * geographic position to the ground. All three of these items are optional. By default, the leader is not pickable. See
 * {@link Placemark#setEnableLeaderPicking(boolean)}.
 * <p/>
 * Placemarks may be drawn with either an image or as single-color square with a specified size. When the placemark
 * attributes indicate a valid image, the placemark's image is drawn as a rectangle in the image's original dimensions,
 * scaled by the image scale attribute. Otherwise, the placemark is drawn as a square with width and height equal to the
 * value of the image scale attribute, in pixels, and color equal to the image color attribute.
 */
public class Placemark extends AbstractRenderable implements Highlightable, Movable {

    /**
     * Presents an interfaced for dynamically determining the PlacemarkAttributes based on the distance between the
     * placemark and the camera.
     */
    public interface LevelOfDetailSelector {

        /**
         * Gets the active attributes for the current distance to the camera and highlighted state.
         *
         * @param rc             The current render context
         * @param placemark      The placemark needing a level of detail selection
         * @param cameraDistance The distance from the placemark to the camera (meters)
         */
        void selectLevelOfDetail(RenderContext rc, Placemark placemark, double cameraDistance);
    }

    /**
     * The default eye distance above which to reduce the size of this placemark, in meters. If {@link
     * Placemark#setEyeDistanceScaling(boolean)} is true, this placemark's image, label and leader sizes are reduced as
     * the eye distance increases beyond this threshold.
     */
    protected static final double DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD = 1e6;

    protected static final double DEFAULT_DEPTH_OFFSET = -0.1;

    private static Vec3 placePoint = new Vec3();

    private static Vec3 screenPlacePoint = new Vec3();

    private static Vec3 groundPoint = new Vec3();

    private static Vec2 offset = new Vec2();

    private static Matrix4 unitSquareTransform = new Matrix4();

    private static Viewport screenBounds = new Viewport();

    /**
     * The placemark's geographic position.
     */
    protected Position position;

    /**
     * The placemark's altitude mode. See {@link gov.nasa.worldwind.WorldWind.AltitudeMode}
     */
    @WorldWind.AltitudeMode
    protected int altitudeMode = WorldWind.ABSOLUTE;

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
     * The texture associated with the active attributes, or null if the attributes specify no image.
     */
    protected Texture activeTexture;

    /**
     * The picked object ID associated with the placemark during the current render pass.
     */
    protected int pickedObjectId;

    protected Color pickColor = new Color();

    /**
     * The label text to draw near the placemark.
     */
    // TODO: implement label property
//    protected String label;

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
     * Indicates whether this placemark's leader, if any, is pickable.
     */
    protected boolean enableLeaderPicking;

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
     * The distance from the camera to the placemark in meters.
     */
    protected double cameraDistance;

    protected LevelOfDetailSelector levelOfDetailSelector;

    /**
     * Constructs a Placemark that draws its representation at the supplied position using default {@link
     * PlacemarkAttributes} bundle. The displayName and label properties are empty.
     *
     * @param position The placemark's geographic position
     */
    public Placemark(Position position) {
        this(position, new PlacemarkAttributes());
    }

    /**
     * Constructs a Placemark that draws its representation at the supplied position using the given {@link
     * PlacemarkAttributes} bundle. The displayName and label properties are empty.
     *
     * @param position   The placemark's geographic position
     * @param attributes The attributes bundle reference that defines how the placemark is drawn
     */
    public Placemark(Position position, PlacemarkAttributes attributes) {
        this(position, attributes, null);
    }

    /**
     * Constructs a Placemark with a label that draws its representation at the supplied position using the given {@link
     * PlacemarkAttributes} bundle. The displayName is set to the supplied name string.
     *
     * @param position   The placemark's geographic position
     * @param attributes The attributes to associate with this placemark. May be null, but if null the placemark will
     *                   not be drawn.
     * @param name       The text for the {@link Placemark#displayName}.
     */
    public Placemark(Position position, PlacemarkAttributes attributes, String name) {
        this.setPosition(position);
        this.setAltitudeMode(WorldWind.ABSOLUTE);
        this.setDisplayName(name == null || name.isEmpty() ? "Placemark" : name);
        // this.setLabel(name); // TODO: call setLabel(name)
        this.attributes = attributes;
        this.eyeDistanceScaling = false;
        this.eyeDistanceScalingThreshold = DEFAULT_EYE_DISTANCE_SCALING_THRESHOLD;
        this.eyeDistanceScalingLabelThreshold = 1.5 * this.eyeDistanceScalingThreshold;
        this.imageRotationReference = WorldWind.RELATIVE_TO_SCREEN;
        this.imageTiltReference = WorldWind.RELATIVE_TO_SCREEN;
    }

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
    public static Placemark createWithColorAndSize(Position position, Color color, int pixelSize) {
        return new Placemark(position, new PlacemarkAttributes().setImageColor(color).setImageScale(pixelSize));
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
    public static Placemark createWithImage(Position position, ImageSource imageSource) {
        return new Placemark(position, PlacemarkAttributes.createWithImage(imageSource));
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
    // TODO: implement createWithImageAndLabel factory method
//    public static Placemark createWithImageAndLabel(Position position, ImageSource imageSource, String label) {
//        return new Placemark(position, PlacemarkAttributes.createWithImage(imageSource), label);
//    }

    /**
     * Gets this placemark's geographic position.
     *
     * @return The geographic position where this placemark is drawn
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets this placemark's geographic position to the values in the supplied position.
     *
     * @param position The new position where this placemark will be drawn
     *
     * @return This placemark
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
     * @return This placemark
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
     * <p/>
     * It is permissible to share attribute bundles between placemarks.
     *
     * @param attributes A reference to an attributes bundle used by this placemark when not highlighted.
     *
     * @return This placemark
     */
    public Placemark setAttributes(PlacemarkAttributes attributes) {
        this.attributes = attributes;
        return this;
    }

    /**
     * Gets the attributes used when this placemark's highlighted flag is true. If null and the highlighted flag is
     * true, this placemark's normal attributes are used. If they, too, are null, this placemark is not drawn.
     *
     * @return A reference to this placemark's highlight attributes bundle
     */
    public PlacemarkAttributes getHighlightAttributes() {
        return highlightAttributes;
    }

    /**
     * Sets the attributes used when this placemark's highlighted flag is true. If null and the highlighted flag is
     * true, this placemark's normal attributes are used. If they, too, are null, this placemark is not drawn.
     * <p/>
     * It is permissible to share attribute bundles between placemarks.
     *
     * @param highlightAttributes A reference to the attributes bundle used by this placemark when highlighted
     *
     * @return This placemark
     */
    public Placemark setHighlightAttributes(PlacemarkAttributes highlightAttributes) {
        this.highlightAttributes = highlightAttributes;
        return this;
    }

    /**
     * gets the current level-of-detail selector used to inject logic for selecting PlacemarkAttributes based on the the
     * camera distance and highlighted attribute.
     *
     * @return The current level-of-detail selector; may be null
     */
    public LevelOfDetailSelector getLevelOfDetailSelector() {
        return this.levelOfDetailSelector;
    }

    /**
     * Sets the optional level-of-detail selector used to inject logic for selecting PlacemarkAttributes based on the
     * the camera distance and highlighted attribute.  If set to null, the normal and highlight attribute bundles used
     * respectfully for the normal and highlighted states.
     *
     * @param levelOfDetailSelector The new level-of-detail selected; may be null
     *
     * @return This placemark
     */
    public Placemark setLevelOfDetailSelector(LevelOfDetailSelector levelOfDetailSelector) {
        this.levelOfDetailSelector = levelOfDetailSelector;
        return this;
    }
    /**
     * Gets the text used to label this placemark on the globe.
     *
     * @return The text used to label a placemark on the globe when labels are enabled
     */
    // TODO: implement getLabel()
//    public String getLabel() {
//        return label;
//    }

    /**
     * Sets the text used for this placemark's label on the globe.
     *
     * @param label The new label text; may be null or empty
     *
     * @return This placemark
     */
    // TODO: implement setLabel()
//    public Placemark setLabel(String label) {
//        this.label = label;
//        return this;
//    }

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
     * Placemark#isEyeDistanceScaling()} is true, this placemark's image, label and leader sizes are reduced as the eye
     * distance increases beyond this threshold.
     *
     * @return The current threshold value, in meters.
     */
    public double getEyeDistanceScalingThreshold() {
        return eyeDistanceScalingThreshold;
    }

    /**
     * Sets the eye distance above which to reduce the size of this placemark, in meters. If {@link
     * Placemark#isEyeDistanceScaling()} is true, this placemark's image, label and leader sizes are reduced as the eye
     * distance increases beyond this threshold.
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
     * <p/>
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
     * <p/>
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
     * <p/>
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
     * <p/>
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
     * Indicates if picking is allowed on this placemark's (optional) leader.
     *
     * @return true if leader picking is enabled, otherwise false
     */
    public boolean isEnableLeaderPicking() {
        return this.enableLeaderPicking;
    }

    /**
     * Sets whether picking is allowed on this placemark's (optional) leader.
     *
     * @param enableLeaderPicking true if leader picking should be enabled, otherwise false
     *
     * @return this placemark
     */
    public Placemark setEnableLeaderPicking(boolean enableLeaderPicking) {
        this.enableLeaderPicking = enableLeaderPicking;
        return this;
    }

    /**
     * Indicates whether this placemark uses its highlight attributes rather than its normal attributes.
     *
     * @return True if this placemark should be highlighted.
     */
    @Override
    public boolean isHighlighted() {
        return highlighted;
    }

    /**
     * Sets the highlighted state of this placemark, which indicates whether this placemark uses its highlight
     * attributes rather than its normal attributes.
     *
     * @param highlighted The highlighted state applied to this placemark.
     */
    @Override
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    /**
     * A position associated with the object that indicates its aggregate geographic position. For a Placemark, this is
     * simply it's position property.
     *
     * @return {@link Placemark#getPosition()}
     */
    @Override
    public Position getReferencePosition() {
        return getPosition();
    }

    /**
     * Moves the shape over the globe's surface. For a Placemark, this simply calls {@link
     * Placemark#setPosition(Position)}.
     *
     * @param globe    not used.
     * @param position the new position of the shape's reference position.
     */
    @Override
    public void moveTo(Globe globe, Position position) {
        setPosition(position);
    }

    /**
     * Performs the rendering; called by the public render method.
     *
     * @param rc the current render context
     */
    @Override
    protected void doRender(RenderContext rc) {
        // Compute the placemark's Cartesian model point.
        rc.geographicToCartesian(this.position.latitude, this.position.longitude, this.position.altitude,
            this.altitudeMode, placePoint);

        // Compute the camera distance to the place point, the value which is used for ordering the placemark drawable
        // and determining the amount of depth offset to apply.
        this.cameraDistance = rc.cameraPoint.distanceTo(placePoint);

        // Compute a screen depth offset appropriate for the current viewing parameters.
        double depthOffset = 0;
        if (this.cameraDistance < rc.horizonDistance) {
            depthOffset = DEFAULT_DEPTH_OFFSET;
        }

        // Project the placemark's model point to screen coordinates, using the screen depth offset to push the screen
        // point's z component closer to the eye point.
        if (!rc.projectWithDepth(placePoint, depthOffset, screenPlacePoint)) {
            return; // clipped by the near plane or the far plane
        }

        // Allow the placemark to adjust the level of detail based on distance to the camera
        if (this.levelOfDetailSelector != null) {
            this.levelOfDetailSelector.selectLevelOfDetail(rc, this, this.cameraDistance);
        }

        // Determine the attributes to use for the current render pass.
        this.determineActiveAttributes(rc);
        if (this.activeAttributes == null) {
            return;
        }

        // Keep track of the drawable count to determine whether or not this placemark has enqueued drawables.
        int drawableCount = rc.drawableCount();
        if (rc.pickMode) {
            this.pickedObjectId = rc.nextPickedObjectId();
            this.pickColor = PickedObject.identifierToUniqueColor(this.pickedObjectId, this.pickColor);
        }

        // Prepare a drawable for the placemark's leader, if requested. Enqueue the leader drawable before the icon
        // drawable in order to give the icon visual priority over the leader.
        if (this.mustDrawLeader(rc)) {
            // Compute the placemark's Cartesian ground point.
            rc.geographicToCartesian(this.position.latitude, this.position.longitude, 0, WorldWind.CLAMP_TO_GROUND,
                groundPoint);

            // If the leader is visible, enqueue a drawable leader for processing on the OpenGL thread.
            if (rc.frustum.intersectsSegment(groundPoint, placePoint)) {
                Pool<DrawableLines> pool = rc.getDrawablePool(DrawableLines.class);
                DrawableLines drawable = DrawableLines.obtain(pool);
                this.prepareDrawableLeader(rc, drawable);
                rc.offerShapeDrawable(drawable, this.cameraDistance);
            }
        }

        // Perform point based culling for placemarks who's textures haven't been loaded yet.
        // If the texture hasn't been loaded yet, then perform point-based culling to avoid
        // loading textures for placemarks that are 'probably' outside the viewing frustum.
        // There are cases where a placemark's texture would be partially visible if it at the
        // edge of the screen were loaded. In these cases the placemark will "pop" into view when
        // the placePoint enters the view frustum.
        if (this.activeAttributes.imageSource != null) {
            this.activeTexture = rc.getTexture(this.activeAttributes.imageSource); // try to get the texture from the cache
            // If we don't have a texture, then perform point-based culling here,
            // otherwise we'll perform a "frustum intersects screenBounds" test later on.
            if (this.activeTexture == null) {
                if (!rc.frustum.containsPoint(placePoint)) {
                    return;
                }
            }
        }

        // Compute the placemark icon's active texture.
        this.determineActiveTexture(rc);

        // If the placemark's icon is visible, enqueue a drawable icon for processing on the OpenGL thread.
        WWMath.boundingRectForUnitSquare(unitSquareTransform, screenBounds);
        if (rc.frustum.intersectsViewport(screenBounds)) {
            Pool<DrawableScreenTexture> pool = rc.getDrawablePool(DrawableScreenTexture.class);
            DrawableScreenTexture drawable = DrawableScreenTexture.obtain(pool);
            this.prepareDrawableIcon(rc, drawable);
            rc.offerShapeDrawable(drawable, this.cameraDistance);
        }

        // Release references to objects stored in the render resource cache.
        this.activeTexture = null;

        // Enqueue a picked object that associates the placemark's icon and leader with its picked object ID.
        if (rc.pickMode && rc.drawableCount() != drawableCount) {
            rc.offerPickedObject(PickedObject.fromRenderable(this.pickedObjectId, this, rc.currentLayer));
        }
    }

    /**
     * Determines the placemark attributes to use for the current render pass.
     *
     * @param rc the current render context
     */
    protected void determineActiveAttributes(RenderContext rc) {
        if (this.highlighted && this.highlightAttributes != null) {
            this.activeAttributes = this.highlightAttributes;
        } else {
            this.activeAttributes = this.attributes;
        }
    }

    /**
     * Determines the image texture and unit square transform to use for the current render pass.
     *
     * @param rc the current render context
     */
    protected void determineActiveTexture(RenderContext rc) {
        // TODO: Refactor!
        if (this.activeAttributes.imageSource != null) {
            // Earlier in doRender(), an attempt was made to 'get' the activeTexture from the cache.
            // If was not found in the cache we need to retrieve a texture from the image source.
            if (this.activeTexture == null) {
                this.activeTexture = rc.retrieveTexture(this.activeAttributes.imageSource, null); // puts retrieved textures in the cache
            }
        } else {
            this.activeTexture = null; // there is no imageSource; draw a simple colored square
        }

        // Compute an camera-position proximity scaling factor, so that distant placemarks can be scaled smaller than
        // nearer placemarks.
        double visibilityScale = this.isEyeDistanceScaling() ?
            Math.max(this.activeAttributes.minimumImageScale, Math.min(1, this.getEyeDistanceScalingThreshold() / this.cameraDistance)) : 1;

        // Initialize the unit square transform to the identity matrix.
        unitSquareTransform.setToIdentity();

        // Apply the icon's translation and scale according to the image size, image offset and image scale. The image
        // offset is defined with its origin at the image's bottom-left corner and axes that extend up and to the right
        // from the origin point. When the placemark has no active texture the image scale defines the image size and no
        // other scaling is applied.
        if (this.activeTexture != null) {
            int w = this.activeTexture.getWidth();
            int h = this.activeTexture.getHeight();
            double s = this.activeAttributes.imageScale * visibilityScale;
            this.activeAttributes.imageOffset.offsetForSize(w, h, offset);

            unitSquareTransform.multiplyByTranslation(
                screenPlacePoint.x - offset.x * s,
                screenPlacePoint.y - offset.y * s,
                screenPlacePoint.z);

            unitSquareTransform.multiplyByScale(w * s, h * s, 1);
        } else {
            // This branch serves both non-textured attributes and also textures that haven't been loaded yet.
            // We set the size for non-loaded textures to the typical size of a contemporary "small" icon (24px)
            double size = this.activeAttributes.imageSource != null ? 24 : this.activeAttributes.imageScale;
            size *= visibilityScale;
            this.activeAttributes.imageOffset.offsetForSize(size, size, offset);

            unitSquareTransform.multiplyByTranslation(
                screenPlacePoint.x - offset.x,
                screenPlacePoint.y - offset.y,
                screenPlacePoint.z);

            unitSquareTransform.multiplyByScale(size, size, 1);
        }

        // ... perform image rotation
        if (this.imageRotation != 0) {
            double rotation = this.imageRotationReference == WorldWind.RELATIVE_TO_GLOBE ?
                rc.camera.heading - this.imageRotation : -this.imageRotation;
            unitSquareTransform.multiplyByTranslation(0.5, 0.5, 0);
            unitSquareTransform.multiplyByRotation(0, 0, 1, rotation);
            unitSquareTransform.multiplyByTranslation(-0.5, -0.5, 0);
        }

        // ... and perform the tilt so that the image tilts back from its base into the view volume.
        if (this.imageTilt != 0) {
            double tilt = this.imageTiltReference == WorldWind.RELATIVE_TO_GLOBE ?
                rc.camera.tilt + this.imageTilt : this.imageTilt;
            unitSquareTransform.multiplyByRotation(-1, 0, 0, tilt);
        }
    }

    /**
     * Prepares this placemark's icon or symbol for processing in a subsequent drawing pass. Implementations must be
     * careful not to leak resources from Placemark into the Drawable.
     *
     * @param rc       the current render context
     * @param drawable the Drawable to be prepared
     */
    protected void prepareDrawableIcon(RenderContext rc, DrawableScreenTexture drawable) {
        // Use the basic GLSL program to draw the placemark's icon.
        drawable.program = (BasicShaderProgram) rc.getShaderProgram(BasicShaderProgram.KEY);
        if (drawable.program == null) {
            drawable.program = (BasicShaderProgram) rc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(rc.resources));
        }

        // Use the plaemark's unit square transform matrix.
        drawable.unitSquareTransform.set(unitSquareTransform);

        // Configure the drawable according to the placemark's active attributes. Use a color appropriate for the pick
        // mode. When picking use a unique color associated with the picked object ID. Use the texture associated with
        // the active attributes' image source and its associated tex coord transform. If the texture is not specified
        // or not available, draw a simple colored square.
        drawable.color.set(rc.pickMode ? this.pickColor : this.activeAttributes.imageColor);
        drawable.texture = this.activeTexture;
        drawable.enableDepthTest = this.activeAttributes.depthTest;
    }

    /**
     * Prepares this placemark's leader for drawing in a subsequent drawing pass. Implementations must be careful not to
     * leak resources from Placemark into the Drawable.
     *
     * @param rc       the current render context
     * @param drawable the Drawable to be prepared
     */
    protected void prepareDrawableLeader(RenderContext rc, DrawableLines drawable) {
        // Use the basic GLSL program to draw the placemark's leader.
        drawable.program = (BasicShaderProgram) rc.getShaderProgram(BasicShaderProgram.KEY);
        if (drawable.program == null) {
            drawable.program = (BasicShaderProgram) rc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(rc.resources));
        }

        // Compute the drawable's vertex points, in Cartesian coordinates relative to the placemark's ground point.
        drawable.vertexPoints[0] = 0; // groundPoint.x - groundPoint.x
        drawable.vertexPoints[1] = 0; // groundPoint.y - groundPoint.y
        drawable.vertexPoints[2] = 0; // groundPoint.z - groundPoint.z
        drawable.vertexPoints[3] = (float) (placePoint.x - groundPoint.x);
        drawable.vertexPoints[4] = (float) (placePoint.y - groundPoint.y);
        drawable.vertexPoints[5] = (float) (placePoint.z - groundPoint.z);

        // Compute the drawable's modelview-projection matrix, relative to the placemark's ground point.
        drawable.mvpMatrix.set(rc.modelviewProjection);
        drawable.mvpMatrix.multiplyByTranslation(groundPoint.x, groundPoint.y, groundPoint.z);

        // Configure the drawable according to the placemark's active leader attributes. Use a color appropriate for the
        // pick mode. When picking use a unique color associated with the picked object ID.
        drawable.color.set(rc.pickMode ? this.pickColor : this.activeAttributes.leaderAttributes.outlineColor);
        drawable.lineWidth = this.activeAttributes.leaderAttributes.outlineWidth;
        drawable.enableDepthTest = this.activeAttributes.leaderAttributes.depthTest;
    }

    /**
     * Determines if a label should and can be drawn.
     *
     * @return True if there is a valid label and label attributes.
     */

    protected boolean mustDrawLabel(RenderContext rc) {
        return false;
        // TODO: implement mustDrawLabel()
//        return this.label != null
//            && !this.label.isEmpty()
//            && this.activeAttributes.labelAttributes != null;
    }

    /**
     * Determines if a leader-line should and can be drawn.
     *
     * @return True if leader-line directive is enabled and there are valid leader-line attributes.
     */
    protected boolean mustDrawLeader(RenderContext rc) {
        return this.activeAttributes.drawLeader
            && this.activeAttributes.leaderAttributes != null
            && (this.enableLeaderPicking || !rc.pickMode);
    }
}
