/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.milstd2525;

import android.util.SparseArray;

import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

public class MilStd2525Placemark extends Placemark {

    public final static int HIGHEST_LEVEL_OF_DETAIL = 0;
    public final static int MEDIUM_LEVEL_OF_DETAIL = 1;
    public final static int LOW_LEVEL_OF_DETAIL = 2;
    public final static int LOWEST_LEVEL_OF_DETAIL = 3;

    public final static double FAR_THRESHOLD = 750000;
    public final static double MID_THRESHOLD = 500000;
    public final static double NEAR_THRESHOLD = 250000;

    protected String symbolCode;

    protected SparseArray<String> modifiers;

    protected SparseArray<String> attributes;

    protected int lastLod = -1;

    /**
     * Constructs a Placemark with a label that draws its representation at the supplied position using the given
     * {@link PlacemarkAttributes} bundle. The displayName is set to the supplied name string.
     *
     * @param position   The placemark's geographic position
     * @param symbolCode A 15-character alphanumeric identifier that provides the information necessary to display
     *                   or transmit a tactical symbol between MIL-STD-2525 compliant systems.
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
     * Determines the placemark attributes to use for the current render pass.
     *
     * @param rc the current render context
     */
    @Override
    protected void determineActiveAttributes(RenderContext rc) {
        // TODO: Consider using an assignable LOD Selector component. See Java Tactical Symbols.
        // TODO: Identify configurable settings that map to the MilStd2525Renderer.

        PlacemarkAttributes placemarkAttributes = null;
//        if (this.cameraDistance > FAR_THRESHOLD) {
//            if (this.lastLod != LOWEST_LEVEL_OF_DETAIL) {
//                String sidc = this.symbolCode.substring(0, 3) + "------*****";
//                SparseArray<String> simpleAttributes = this.attributes.clone();
//                simpleAttributes.put(MilStdAttributes.PixelSize, "50");
//                placemarkAttributes = MilStd2525.getPlacemarkAttributes(sidc, null, simpleAttributes);
//                this.lastLod = LOWEST_LEVEL_OF_DETAIL;
//            }
//        } else
        if (this.cameraDistance > MID_THRESHOLD) {
            if (this.lastLod != LOW_LEVEL_OF_DETAIL) {
                String sidc = this.symbolCode.substring(0, 3) + "------*****";
                placemarkAttributes = MilStd2525.getPlacemarkAttributes(sidc, null, this.attributes);
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
        if (placemarkAttributes != null)
            super.setAttributes(placemarkAttributes);

        // The super method must be called to establish the picking id for this placemark
        super.determineActiveAttributes(rc);
    }
}
