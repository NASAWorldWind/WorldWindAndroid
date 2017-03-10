/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.WorldWind;
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
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;
import gov.nasa.worldwind.util.WWMath;

/**
 * Represents a label at a geographic position. Labels display a single line of text according to specified {@link
 * TextAttributes}.
 */
public class Label extends AbstractRenderable implements Highlightable, Movable {

    /**
     * The default amount of screen depth offset applied to the label's text during rendering. Values less than zero
     * bias depth values toward the viewer.
     */
    protected static final double DEFAULT_DEPTH_OFFSET = -0.1;

    /**
     * The label's properties associated with the current render pass.
     */
    private static RenderData renderData = new RenderData();

    /**
     * The label's geographic position.
     */
    protected Position position = new Position();

    /**
     * The label's altitude mode. See {@link gov.nasa.worldwind.WorldWind.AltitudeMode}
     */
    @WorldWind.AltitudeMode
    protected int altitudeMode = WorldWind.ABSOLUTE;

    /**
     * The label's text.
     */
    protected String text;

    /**
     * The label's rotation in degrees clockwise.
     */
    protected double rotation;

    /**
     * The label's rotation mode. Defaults to WorldWind.RELATIVE_TO_SCREEN.
     */
    @WorldWind.OrientationMode
    protected int rotationMode = WorldWind.RELATIVE_TO_SCREEN;

    /**
     * The label's normal attributes.
     */
    protected TextAttributes attributes;

    /**
     * The attributes to use when the label is highlighted.
     */
    protected TextAttributes highlightAttributes;

    /**
     * The attributes identified for use during the current render pass.
     */
    protected TextAttributes activeAttributes;

    /**
     * Determines whether the normal or highlighted attibutes should be used.
     */
    protected boolean highlighted;

    /**
     * Constructs a label that displays text at a geographic position.
     *
     * @param position the position where the label is displayed
     * @param text     the text to display, or null to display nothing
     *
     * @throws IllegalArgumentException If the position is null
     */
    public Label(Position position, String text) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Label", "constructor", "missingPosition"));
        }

        this.position.set(position);
        this.text = text;
        this.attributes = new TextAttributes();
    }

    /**
     * Constructs a label with specified attributes that displays at a geographic position once its text is set to a
     * non-null value.
     *
     * @param position   the position where the label is displayed
     * @param attributes a reference to an attributes bundle used by this label when not highlighted
     *
     * @throws IllegalArgumentException If the position is null
     */
    public Label(Position position, TextAttributes attributes) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Label", "constructor", "missingPosition"));
        }

        this.position.set(position);
        this.attributes = attributes;
    }

    /**
     * Constructs a label with specified attributes that displays text at a geographic position.
     *
     * @param position   the position where the label is displayed
     * @param text       the text to display, or null to display nothing
     * @param attributes a reference to an attributes bundle used by this label when not highlighted
     *
     * @throws IllegalArgumentException If the position is null
     */
    public Label(Position position, String text, TextAttributes attributes) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Label", "constructor", "missingPosition"));
        }

        this.position.set(position);
        this.text = text;
        this.attributes = attributes;
    }

    /**
     * Indicates the geographic position where this label is displayed.
     *
     * @return this label's geographic position
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Sets this label's geographic position to the values in the supplied position.
     *
     * @param position the new position where this label is displayed
     *
     * @return this label, with its position set to the specified value
     *
     * @throws IllegalArgumentException If the position is null
     */
    public Label setPosition(Position position) {
        if (position == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Text", "setPosition", "missingPosition"));
        }

        this.position.set(position);
        return this;
    }

    /**
     * Indicates the altitude mode associated with this label's position.
     *
     * @return the altitude mode, see {@link gov.nasa.worldwind.WorldWind.AltitudeMode} for possible
     */
    @WorldWind.AltitudeMode
    public int getAltitudeMode() {
        return this.altitudeMode;
    }

    /**
     * Sets the altitude mode associated with this label's position.
     *
     * @param altitudeMode the new altitude mode, see {@link gov.nasa.worldwind.WorldWind.AltitudeMode} for acceptable
     *                     values
     *
     * @return this label with its altitude mode set to the specified value
     */
    public Label setAltitudeMode(@WorldWind.AltitudeMode int altitudeMode) {
        this.altitudeMode = altitudeMode;
        return this;
    }

    /**
     * Indicates the text displayed by this label. The returned string may be null, indicating that this label displays
     * nothing.
     *
     * @return a reference to the label's text, or null if the label displays nothing
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets the text displayed by this label. The string may be null, in which case the label displays nothing.
     *
     * @param text the text to display, or null to display nothing
     *
     * @return this label with its text set to the specified reference
     */
    public Label setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Indicates the rotation applied to this label. The rotation represents clockwise clockwise degrees relative to
     * this label's labelRotationMode.
     *
     * @return this label's rotation amount in degrees, or zero if this label has no rotation relative to its
     * orientation reference
     *
     * @see #getRotationMode()
     */
    public double getRotation() {
        return this.rotation;
    }

    /**
     * Sets the amount of rotation applied to this label. The rotation represents clockwise degrees relative to this
     * label's labelRotationMode.
     *
     * @param degrees this label's new rotation amount in degrees, or zero to apply no rotation relative to this label's
     *                orientation reference
     *
     * @return this label with its rotation set to the specified value
     *
     * @see #setRotationMode(int)
     */
    public Label setRotation(double degrees) {
        this.rotation = degrees;
        return this;
    }

    /**
     * Indicates the orientation mode used to interpret this label's rotation. Label rotation may be either relative to
     * the screen or relative to the globe, as indicated by the following allowable values: <ul> <li>{@code
     * WorldWind.RELATIVE_TO_SCREEN} - The label's orientation is fixed relative to the screen. Rotation indicates
     * clockwise degrees relative to the screen's vertical axis. This is the default mode.</li> <li>{@code
     * WorldWind.RELATIVE_TO_GLOBE} - The label's orientation is fixed relative to the globe. Rotation indicates
     * clockwise degrees relative to North.</li> </ul>
     *
     * @return this label's rotation mode
     */
    @WorldWind.OrientationMode
    public int getRotationMode() {
        return this.rotationMode;
    }

    /**
     * Sets the orientation mode this label uses to interpret its rotation. Label rotation may be either relative to the
     * screen or relative to the globe, as indicated by the following allowable values: <ul> <li>{@code
     * WorldWind.RELATIVE_TO_SCREEN} - The label's orientation is fixed relative to the screen. Rotation indicates
     * clockwise degrees relative to the screen's vertical axis. This is the default mode.</li> <li>{@code
     * WorldWind.RELATIVE_TO_GLOBE} - The label's orientation is fixed relative to the globe. Rotation indicates
     * clockwise degrees relative to North.</li> </ul>
     *
     * @param orientationMode the orientation mode used to interpret this label's rotation
     *
     * @return this label with its rotation mode set to the specified value
     */
    public Label setRotationMode(@WorldWind.OrientationMode int orientationMode) {
        this.rotationMode = orientationMode;
        return this;
    }

    /**
     * Indicates this label's "normal" attributes, that is the attributes used when the label's highlighted flag is
     * false. If null and this label is not highlighted, this label displays nothing.
     *
     * @return a reference to this label's attributes bundle
     */
    public TextAttributes getAttributes() {
        return this.attributes;
    }

    /**
     * Sets this label's attributes to the supplied attributes bundle. If null and this label is not highlighted, this
     * this label displays nothing.
     * <p/>
     * It is permissible to share attribute bundles between labels.
     *
     * @param attributes a reference to an attributes bundle used by this label when not highlighted
     *
     * @return this label with its normal attributes bundle set to the specified reference
     */
    public Label setAttributes(TextAttributes attributes) {
        this.attributes = attributes;
        return this;
    }

    /**
     * Gets the attributes used when this label's highlighted flag is true. If null and the highlighted flag is true,
     * this label's normal attributes are used. If they, too, are null, this label displays nothing.
     *
     * @return a reference to this label's highlight attributes bundle
     */
    public TextAttributes getHighlightAttributes() {
        return this.highlightAttributes;
    }

    /**
     * Sets the attributes used when this label's highlighted flag is true. If null and the highlighted flag is true,
     * this label's normal attributes are used. If they, too, are null, this label displays nothing.
     * <p/>
     * It is permissible to share attribute bundles between labels.
     *
     * @param highlightAttributes a reference to the attributes bundle used by this label when highlighted
     *
     * @return this label with its highlight attributes bundle set to the specified reference
     */
    public Label setHighlightAttributes(TextAttributes highlightAttributes) {
        this.highlightAttributes = highlightAttributes;
        return this;
    }

    /**
     * Indicates whether this label uses its highlight attributes rather than its normal attributes.
     *
     * @return true if this label is highlighted, and false otherwise
     */
    @Override
    public boolean isHighlighted() {
        return this.highlighted;
    }

    /**
     * Sets the highlighted state of this label, which indicates whether this label uses its highlight attributes rather
     * than its normal attributes.
     *
     * @param highlighted true to highlight this label, and false otherwise
     */
    @Override
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    /**
     * A position associated with the object that indicates its aggregate geographic position. For a Label, this is
     * simply it's position property.
     *
     * @return {@link Label#getPosition()}
     */
    @Override
    public Position getReferencePosition() {
        return this.getPosition();
    }

    /**
     * Moves the shape over the globe's surface. For a Label, this simply calls {@link Label#setPosition(Position)}.
     *
     * @param globe    not used.
     * @param position the new position of the shape's reference position.
     */
    @Override
    public void moveTo(Globe globe, Position position) {
        this.setPosition(position);
    }

    @Override
    protected void doRender(RenderContext rc) {
        if (this.text == null || this.text.length() == 0) {
            return; // no text to render
        }

        // Compute the label's Cartesian model point.
        rc.geographicToCartesian(this.position.latitude, this.position.longitude, this.position.altitude,
            this.altitudeMode, renderData.placePoint);

        // Compute the camera distance to the place point, the value which is used for ordering the label drawable and
        // determining the amount of depth offset to apply.
        renderData.cameraDistance = rc.cameraPoint.distanceTo(renderData.placePoint);

        // Compute a screen depth offset appropriate for the current viewing parameters.
        double depthOffset = 0;
        if (renderData.cameraDistance < rc.horizonDistance) {
            depthOffset = DEFAULT_DEPTH_OFFSET;
        }

        // Project the label's model point to screen coordinates, using the screen depth offset to push the screen
        // point's z component closer to the eye point.
        if (!rc.projectWithDepth(renderData.placePoint, depthOffset, renderData.screenPlacePoint)) {
            return; // clipped by the near plane or the far plane
        }

        // Select the currently active attributes. Don't render anything if the attributes are unspecified.
        this.determineActiveAttributes(rc);
        if (this.activeAttributes == null) {
            return;
        }

        // Keep track of the drawable count to determine whether or not this label has enqueued drawables.
        int drawableCount = rc.drawableCount();
        if (rc.pickMode) {
            renderData.pickedObjectId = rc.nextPickedObjectId();
            renderData.pickColor = PickedObject.identifierToUniqueColor(renderData.pickedObjectId, renderData.pickColor);
        }

        // Enqueue drawables for processing on the OpenGL thread.
        this.makeDrawable(rc);

        // Enqueue a picked object that associates the label's drawables with its picked object ID.
        if (rc.pickMode && rc.drawableCount() != drawableCount) {
            rc.offerPickedObject(PickedObject.fromRenderable(renderData.pickedObjectId, this, rc.currentLayer));
        }
    }

    protected void determineActiveAttributes(RenderContext rc) {
        if (this.highlighted && this.highlightAttributes != null) {
            this.activeAttributes = this.highlightAttributes;
        } else {
            this.activeAttributes = this.attributes;
        }
    }

    protected void makeDrawable(RenderContext rc) {
        // Render the label's texture when the label's position is in the frustum. If the label's position is outside
        // the frustum we don't do anything. This ensures that label textures are rendered only as necessary.
        Texture texture = rc.getText(this.text, this.activeAttributes);
        if (texture == null && rc.frustum.containsPoint(renderData.placePoint)) {
            texture = rc.renderText(this.text, this.activeAttributes);
        } else if (texture == null) {
            return;
        }

        // Initialize the unit square transform to the identity matrix.
        renderData.unitSquareTransform.setToIdentity();

        // Apply the label's translation according to its text size and text offset. The text offset is defined with its
        // origin at the text's bottom-left corner and axes that extend up and to the right from the origin point.
        int w = texture.getWidth();
        int h = texture.getHeight();
        this.activeAttributes.textOffset.offsetForSize(w, h, renderData.offset);
        renderData.unitSquareTransform.setTranslation(
            renderData.screenPlacePoint.x - renderData.offset.x,
            renderData.screenPlacePoint.y - renderData.offset.y,
            renderData.screenPlacePoint.z);

        // Apply the label's rotation according to its rotation value and orientation mode. The rotation is applied
        // such that the text rotates around the text offset point.
        double rotation = (this.rotationMode == WorldWind.RELATIVE_TO_GLOBE) ?
            (rc.camera.heading - this.rotation) : -this.rotation;
        if (rotation != 0) {
            renderData.unitSquareTransform.multiplyByTranslation(renderData.offset.x, renderData.offset.y, 0);
            renderData.unitSquareTransform.multiplyByRotation(0, 0, 1, rotation);
            renderData.unitSquareTransform.multiplyByTranslation(-renderData.offset.x, -renderData.offset.y, 0);
        }

        // Apply the label's translation and scale according to its text size.
        renderData.unitSquareTransform.multiplyByScale(w, h, 1);

        WWMath.boundingRectForUnitSquare(renderData.unitSquareTransform, renderData.screenBounds);
        if (!rc.frustum.intersectsViewport(renderData.screenBounds)) {
            return; // the text is outside the viewport
        }

        // Obtain a pooled drawable and configure it to draw the label's text.
        Pool<DrawableScreenTexture> pool = rc.getDrawablePool(DrawableScreenTexture.class);
        DrawableScreenTexture drawable = DrawableScreenTexture.obtain(pool);

        // Use the basic GLSL program to draw the text.
        drawable.program = (BasicShaderProgram) rc.getShaderProgram(BasicShaderProgram.KEY);
        if (drawable.program == null) {
            drawable.program = (BasicShaderProgram) rc.putShaderProgram(BasicShaderProgram.KEY, new BasicShaderProgram(rc.resources));
        }

        // Use the text's unit square transform matrix.
        drawable.unitSquareTransform.set(renderData.unitSquareTransform);

        // Configure the drawable according to the active attributes. Use a color appropriate for the pick mode. When
        // picking use a unique color associated with the picked object ID. Use the texture associated with the active
        // attributes' text image and its associated tex coord transform.
        drawable.color.set(rc.pickMode ? renderData.pickColor : this.activeAttributes.textColor);
        drawable.texture = texture;
        drawable.enableDepthTest = this.activeAttributes.enableDepthTest;

        // Enqueue a drawable for processing on the OpenGL thread.
        rc.offerShapeDrawable(drawable, renderData.cameraDistance);
    }

    /**
     * Properties associated with the label during a render pass.
     */
    protected static class RenderData {

        /**
         * The model coordinate point corresponding to the label's position.
         */
        public Vec3 placePoint = new Vec3();

        /**
         * The screen coordinate point corresponding to the label's position.
         */
        public Vec3 screenPlacePoint = new Vec3();

        /**
         * The screen coordinate offset corresponding to the active attributes.
         */
        public Vec2 offset = new Vec2();

        /**
         * The screen coordinate transform to apply to the drawable unit square.
         */
        public Matrix4 unitSquareTransform = new Matrix4();

        /**
         * The screen viewport indicating the label's screen bounds.
         */
        public Viewport screenBounds = new Viewport();

        /**
         * Unique identifier associated with the label during picking.
         */
        public int pickedObjectId;

        /**
         * Unique color used to display the label during picking.
         */
        public Color pickColor = new Color();

        /**
         * The distance from the camera position to the label position, in meters.
         */
        public double cameraDistance;
    }
}
