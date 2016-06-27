/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.milstd2525;

import android.util.SparseArray;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.shape.Placemark;

public class MilStd2525Placemark extends Placemark {

    protected String symbolCode;

    protected SparseArray<String> modifiers;

    protected SparseArray<String> attributes;

    /**
     * Constructs a MIL-STD-2525 Placemark with an appropriate level of detail for the current distance from the camera.
     * Shared low-fidelity images are used when far away from the camera, whereas unique high-fidelity images are used
     * when near the camera. The high-fidelity images that are no longer in view are automatically freed, if necessary,
     * to release memory resources. The Placemark's symbol is lazily created (and recreated if necessary) via an
     * ImageSource.BitmapFactory that is established in the MilStd2525 utility class. See the {@link
     * MilStd2525#getPlacemarkAttributes(String, SparseArray, SparseArray)} for more information about resource
     * caching/sharing and the bitmap factory.
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
        super(position, null /* attribute bundle */, symbolCode /* name */);

        // Set the properties used to create the bitmap in the level of detail selector
        this.symbolCode = symbolCode;
        this.modifiers = modifiers;
        this.attributes = attributes;

        this.setLevelOfDetailSelector(new MilStd2525LevelOfDetailSelector());
    }
}
