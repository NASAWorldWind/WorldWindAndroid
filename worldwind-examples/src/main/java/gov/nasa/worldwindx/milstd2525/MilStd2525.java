/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.milstd2525;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.SparseArray;

import java.util.HashMap;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.util.Logger;

/**
 * This utility class generates PlacemarkAttributes bundles with MIL-STD-2525 symbols. The symbols are generated from
 * the MIL-STD-2525 Symbol Rendering Library (<a href="https://github.com/missioncommand/mil-sym-android">https://github.com/missioncommand/mil-sym-android</a>)
 * contained in the mil-sym-android module.
 */
public class MilStd2525 {

    /**
     * The actual rendering engine for the MIL-STD-2525 graphics.
     */
    private static MilStdIconRenderer renderer = MilStdIconRenderer.getInstance();

    /**
     * A (simple) cache of PlacemarkAttribute bundles containing MIL-STD-2525 symbols. Using a cache is essential for
     * memory management--we want to reuse the bitmap textures for identical symbols.
     */
    private static HashMap<String, PlacemarkAttributes> symbolCache = new HashMap<>();

    private static SparseArray<String> emptyArray = new SparseArray<>();    // may be used in a cache key

    private static boolean initialized = false;


    /**
     * Initializes the static MIL-STD-2525 symbol renderer.  This method must be called one time before calling
     * renderImage().
     *
     * @param applicationContext The Context used to define the location of the renderer's cache directly.
     */
    public static void initializeRenderer(Context applicationContext) {
        if (initialized) {
            return;
        }
        // Tell the renderer where the cache folder is located which is needed to process the embedded xml files.
        String cacheDir = applicationContext.getCacheDir().getAbsoluteFile().getAbsolutePath();
        renderer.init(cacheDir);

        // Establish the default rendering values.
        // See: https://github.com/missioncommand/mil-sym-android/blob/master/Renderer/src/main/java/armyc2/c2sd/renderer/utilities/RendererSettings.java
        RendererSettings rs = RendererSettings.getInstance();
        rs.setSymbologyStandard(RendererSettings.Symbology_2525C);
        rs.setDefaultPixelSize(100);

        // Depending on screen size and DPI you may want to change the font size.
        rs.setModifierFont("Arial", Typeface.BOLD, 18);
        rs.setMPModifierFont("Arial", Typeface.BOLD, 18);

        // Configure modifier text output
        rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE);
        rs.setTextOutlineWidth(4);  // 4 is the default

        initialized = true;
    }

    /**
     * Releases cached PlacemarkAttribute bundles.
     */
    public static void clearSymbolCache() {
        symbolCache.clear();
    }

    /**
     * Gets a PlacemarkAttributes bundle for the supplied symbol specification. The attribute bundle is retrieved from a
     * cache. If the symbol is not found in the cache, an attribute bundle is created and added to the cache before it
     * is returned.
     *
     * @param symbolCode A 15-character alphanumeric identifier that provides the information necessary to display or
     *                   transmit a tactical symbol between MIL-STD-2525 compliant systems.
     * @param modifiers  A optional collection of unit or tactical graphic modifiers. See:
     *                   https://github.com/missioncommand/mil-sym-android/blob/master/Renderer/src/main/java/armyc2/c2sd/renderer/utilities/ModifiersUnits.java
     *                   and https://github.com/missioncommand/mil-sym-android/blob/master/Renderer/src/main/java/armyc2/c2sd/renderer/utilities/ModifiersTG.java
     * @param attributes A optional collection of rendering attributes. See https://github.com/missioncommand/mil-sym-android/blob/master/Renderer/src/main/java/armyc2/c2sd/renderer/utilities/MilStdAttributes.java
     *
     * @return Either a new or a cached PlacemarkAttributes bundle containing the specified symbol embedded in the
     * bundle's imageSource property.
     */
    public static PlacemarkAttributes getPlacemarkAttributes(String symbolCode, SparseArray<String> modifiers, SparseArray<String> attributes) {

        // Generate a cache key for the symbol
        String key = symbolCode
            + (modifiers == null ? emptyArray.toString() : modifiers.toString())
            + (attributes == null ? emptyArray.toString() : attributes.toString());

        // Get the attribute bundle from the cache
        PlacemarkAttributes placemarkAttributes = symbolCache.get(key);

        if (placemarkAttributes == null) {

            // Create the attributes bundle and add it to the cache.
            placemarkAttributes = MilStd2525.createPlacemarkAttributes(symbolCode, modifiers, attributes);
            if (placemarkAttributes == null) {
                throw new IllegalArgumentException("Cannot generate a symbol for: " + key);
            }
            symbolCache.put(key, placemarkAttributes);
        }

        return placemarkAttributes;
    }


    /**
     * Creates a placemark attributes bundle containing a MIL-STD-2525 symbol using the specified modifiers and
     * attributes.
     *
     * @param symbolCode The 15-character SIDC (symbol identification coding scheme) code.
     * @param modifiers  The ModifierUnit (unit) or ModifierTG (tactical graphic) modifiers collection. May be null.
     * @param attributes The MilStdAttributes attributes collection. May be null.
     *
     * @return A new PlacemarkAttributes bundle representing the MIL-STD-2525 symbol.
     */
    public static PlacemarkAttributes createPlacemarkAttributes(String symbolCode, SparseArray<String> modifiers, SparseArray<String> attributes) {

        ImageInfo imageInfo = MilStd2525.renderImage(symbolCode, modifiers, attributes);

        return createPlacemarkAttributesFromSymbol(imageInfo);
    }

    /**
     * Creates a placemark attributes bundle containing the supplied MilStd2525C symbol.
     *
     * @param imageInfo An image and meta data object created by the symbol renderer.
     *
     * @return A new PlacemarkAttributes bundle referencing the bitmap in the imageInfo parameter.
     */
    public static PlacemarkAttributes createPlacemarkAttributesFromSymbol(ImageInfo imageInfo) {

        // Place the bottom of the image at the specified position and
        // anchor it horizontally at the center of the core symbol.
        Rect imageBounds = imageInfo.getImageBounds();      // The bounds of the entire image, including text
        Point centerPoint = imageInfo.getCenterPoint();     // The center of the core symbol
        Offset imageOffset = new Offset(
            WorldWind.OFFSET_FRACTION, ((double) centerPoint.x) / imageBounds.width(), // x offset
            WorldWind.OFFSET_FRACTION, 0.0); // y offset

        return PlacemarkAttributes.withImageAndLeaderLine(ImageSource.fromBitmap(imageInfo.getImage())).setImageOffset(imageOffset);
    }

    /**
     * Creates an MIL-STD-2525 symbol from the specified symbol code, modifiers and attributes.
     *
     * @param symbolCode The MIL-STD-2525 symbol code.
     * @param modifiers  The MIL-STD-2525 modifiers. If null, a default (empty) modifier list will be used.
     * @param attributes The MIL-STD-2525 attributes. If null, a default (empty) attribute list will be used.
     *
     * @return An ImageInfo object containing the symbol's bitmap and meta data.
     */
    public static ImageInfo renderImage(String symbolCode, SparseArray<String> modifiers, SparseArray<String> attributes) {
        if (!initialized) {
            throw new IllegalStateException(
                Logger.logMessage(Logger.ERROR, "MilStd2525", "renderImage", "renderer has not been initialized."));
        }
        return renderer.RenderIcon(symbolCode,
            modifiers == null ? new SparseArray<String>() : modifiers,
            attributes == null ? new SparseArray<String>() : attributes);
    }
}
