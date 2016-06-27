/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.milstd2525;

import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.util.Logger;

/**
 * The MilStd2525LevelOfDetailSelector determines which set of PlacemarkAttributes to use for a MilStd2525Placemark. A
 * {@link MilStd2525Placemark} creates an instance of this class in its constructor, and calls
 * {@link Placemark.LevelOfDetailSelector#selectLevelOfDetail(RenderContext, Placemark, double)} in its doRender() method.
 */
public class MilStd2525LevelOfDetailSelector implements Placemark.LevelOfDetailSelector {

    protected final static int HIGHEST_LEVEL_OF_DETAIL = 0;

    protected final static int MEDIUM_LEVEL_OF_DETAIL = 1;

    protected final static int LOW_LEVEL_OF_DETAIL = 2;

    protected static double FAR_THRESHOLD = 500000;

    protected static double NEAR_THRESHOLD = 300000;

    protected int lastLevelOfDetail = -1;

    protected boolean lastHighlightState = false;

    protected PlacemarkAttributes placemarkAttributes;

    /**
     * Sets the far distance threshold; camera distances greater than this value use the low level of detail, and
     * distances less than this value but greater than the near threshold use the medium level of detail.
     *
     * @param farThreshold camera distance threshold in meters
     */
    public static void setFarThreshold(double farThreshold) {
        FAR_THRESHOLD = farThreshold;
    }

    /**
     * Sets the near distance threshold; camera distances greater than this value but less that the far threshold use
     * the medium level of detail, and distances less than this value use the high level of detail.
     *
     * @param nearThreshold camera distance threshold in meters
     */
    public static void setNearThreshold(double nearThreshold) {
        NEAR_THRESHOLD = nearThreshold;
    }

    /**
     * Gets the active attributes for the current distance to the camera and highlighted state.
     *
     * @param rc             The current render contents
     * @param placemark      The placemark needing a level of detail selection
     * @param cameraDistance The distance from the placemark to the camera (meters)
     */
    @Override
    public void selectLevelOfDetail(RenderContext rc, Placemark placemark, double cameraDistance) {
        if (!(placemark instanceof MilStd2525Placemark)) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "MilStd2525LevelOfDetailSelector", "selectLevelOfDetail",
                    "The placemark is not a MilStd2525Placemark"));
        }
        MilStd2525Placemark milStdPlacemark = (MilStd2525Placemark) placemark;

        boolean highlighted = milStdPlacemark.isHighlighted();
        boolean highlightChanged = this.lastHighlightState != highlighted;

        // Determine the normal attributes based on the distance from the camera to the placemark
        if (cameraDistance > FAR_THRESHOLD) {
            // Low-fidelity: use a simplified SIDC code (without status) and no modifiers
            if (this.lastLevelOfDetail != LOW_LEVEL_OF_DETAIL || highlightChanged) {
                String simpleCode = milStdPlacemark.symbolCode.substring(0, 3) + "*------*****";    // SIDC
                this.placemarkAttributes = MilStd2525.getPlacemarkAttributes(simpleCode, null, milStdPlacemark.attributes);
                this.lastLevelOfDetail = LOW_LEVEL_OF_DETAIL;
            }
        } else if (cameraDistance > NEAR_THRESHOLD) {
            // Medium-fidelity: use the regulation SIDC code but without modifiers
            if (this.lastLevelOfDetail != MEDIUM_LEVEL_OF_DETAIL || highlightChanged) {
                this.placemarkAttributes = MilStd2525.getPlacemarkAttributes(milStdPlacemark.symbolCode, null, milStdPlacemark.attributes);
                this.lastLevelOfDetail = MEDIUM_LEVEL_OF_DETAIL;
            }
        } else {
            // High-fidelity: use the regulation SIDC code and the modifiers
            if (this.lastLevelOfDetail != HIGHEST_LEVEL_OF_DETAIL || highlightChanged) {
                this.placemarkAttributes = MilStd2525.getPlacemarkAttributes(milStdPlacemark.symbolCode, milStdPlacemark.modifiers, milStdPlacemark.attributes);
                this.lastLevelOfDetail = HIGHEST_LEVEL_OF_DETAIL;
            }
        }

        if (highlightChanged) {
            // Use a distinct set of attributes when highlighted, otherwise use the shared attributes
            if (highlighted) {
                // Create a copy of the shared attributes bundle and increase the scale
                double scale = this.placemarkAttributes.getImageScale();
                this.placemarkAttributes = new PlacemarkAttributes(this.placemarkAttributes).setImageScale(scale * 1.2);
            }
        }
        this.lastHighlightState = highlighted;

        // Update the placemark's attributes bundle
        if (this.placemarkAttributes != null) {
            milStdPlacemark.setAttributes(this.placemarkAttributes);
        }
    }
}
