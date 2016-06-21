/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.milstd2525;

import android.util.SparseArray;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

public class MilStd2525Placemark extends Placemark {

    /**
     * Camera distance threshold for determining between low or medium level of detail.
     */
    public final static double FAR_THRESHOLD = 500000;

    /**
     * Camera distance threshold for determining between medium or high level of detail.
     */
    public final static double NEAR_THRESHOLD = 300000;

    protected final static int HIGHEST_LEVEL_OF_DETAIL = 0;

    protected final static int MEDIUM_LEVEL_OF_DETAIL = 1;

    protected final static int LOW_LEVEL_OF_DETAIL = 2;

    protected String symbolCode;

    protected SparseArray<String> modifiers;

    protected SparseArray<String> attributes;

    protected int lastLod = -1;

    /**
     * Constructs a Placemark with a label that draws its representation at the supplied position using the given {@link
     * PlacemarkAttributes} bundle. The displayName is set to the supplied name string.
     *
     * @param position   The placemark's geographic position
     * @param symbolCode A 15-character alphanumeric identifier that provides the information necessary to display or
     *                   transmit a tactical symbol between MIL-STD-2525 compliant systems.
     * @param modifiers  A optional collection of unit or tactical graphic modifiers. See:
     *                   https://github.com/missioncommand/mil-sym-android/blob/master/Renderer/src/main/java/armyc2/c2sd/renderer/utilities/ModifiersUnits.java
     *                   and https://github.com/missioncommand/mil-sym-android/blob/master/Renderer/src/main/java/armyc2/c2sd/renderer/utilities/ModifiersTG.java
     * @param attributes A optional collection of rendering attributes. See https://github.com/missioncommand/mil-sym-android/blob/master/Renderer/src/main/java/armyc2/c2sd/renderer/utilities/MilStdAttributes.java
     */
    public MilStd2525Placemark(Position position, String symbolCode, SparseArray<String> modifiers, SparseArray<String> attributes) {
        super(position, null, symbolCode);
        this.symbolCode = symbolCode;
        this.modifiers = modifiers;
        this.attributes = attributes;
    }

    /**
     * Determines the placemark attributes to use for the current render pass based on the camera distance.
     *
     * @param rc the current render context
     */
    @Override
    protected void determineActiveAttributes(RenderContext rc) {
        // TODO: Consider using an assignable LOD Selector component. See Java Tactical Symbols.

        // Determine the normal attributes based on the distance from the camera to the placemark
        PlacemarkAttributes placemarkAttributes = null;
        if (this.cameraDistance > FAR_THRESHOLD) {
            if (this.lastLod != LOW_LEVEL_OF_DETAIL) {
                String simpleCode = this.symbolCode.substring(0, 3) + "------*****";    // SIDC
                placemarkAttributes = MilStd2525.getPlacemarkAttributes(simpleCode, null, this.attributes);
                this.lastLod = LOW_LEVEL_OF_DETAIL;
            }
        } else if (this.cameraDistance > NEAR_THRESHOLD) {
            if (this.lastLod != MEDIUM_LEVEL_OF_DETAIL) {
                placemarkAttributes = MilStd2525.getPlacemarkAttributes(this.symbolCode, null, this.attributes);
                this.lastLod = MEDIUM_LEVEL_OF_DETAIL;
            }
        } else {
            if (this.lastLod != HIGHEST_LEVEL_OF_DETAIL) {
                placemarkAttributes = MilStd2525.getPlacemarkAttributes(this.symbolCode, this.modifiers, this.attributes);
                this.lastLod = HIGHEST_LEVEL_OF_DETAIL;
            }
        }
        if (placemarkAttributes != null) {
            // Override the normal attributes with our selected level of detail
            super.setAttributes(placemarkAttributes);
        }

        // The super method must be called to establish the picking id for this placemark
        super.determineActiveAttributes(rc);
    }
}
