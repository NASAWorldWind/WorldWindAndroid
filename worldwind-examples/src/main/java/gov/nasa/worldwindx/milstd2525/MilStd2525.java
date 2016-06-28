/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.milstd2525;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
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
     * The image to use when the renderer cannot render an image.
     */
    private static Bitmap defaultImage = BitmapFactory.decodeResource(Resources.getSystem(), android.R.drawable.ic_dialog_alert); // Warning triangle

    /**
     * The actual rendering engine for the MIL-STD-2525 graphics.
     */
    private static MilStdIconRenderer renderer = MilStdIconRenderer.getInstance();

    /**
     * The handler used to schedule runnable to be executed on the main thread.
     */
    private static Handler mainLoopHandler = new Handler(Looper.getMainLooper());

    /**
     * A cache of PlacemarkAttribute bundles containing MIL-STD-2525 symbols. Using a cache is essential for memory
     * management: we want to share the bitmap textures for identical symbols.  The cache maintains weak references to
     * the attribute bundles so that the garbage collector can reclaim the memory when a Placemark releases an attribute
     * bundle, for instance when it changes its level-of-detail.
     */
    private static HashMap<String, WeakReference<PlacemarkAttributes>> symbolCache = new HashMap<>();

    private static SparseArray<String> emptyArray = new SparseArray<>();    // may be used in a cache key

    private static boolean initialized = false;

    private final static int DEFAULT_PIXEL_SIZE = 100;

    private final static int DEFAULT_FONT_SIZE = 18;

    private final static int TEXT_OUTLINE_WIDTH = 4;

    private final static double MINIMUM_IMAGE_SCALE = 0.25;

    /**
     * Sets and overrides the default "missing image" icon.
     *
     * @param bitmap The image to display when the renderer cannot render the given symbol code.
     */
    public static void setDefaultImage(Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "MilStd2525", "setDefaultImage", "missingBitmap"));
        }
        defaultImage = bitmap;
    }

    /**
     * Initializes the static MIL-STD-2525 symbol renderer.  This method must be called one time before calling
     * renderImage().
     *
     * @param applicationContext The Context used to define the location of the renderer's cache directly.
     */
    public static synchronized void initializeRenderer(Context applicationContext) {
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
        rs.setDefaultPixelSize(DEFAULT_PIXEL_SIZE);

        // Depending on screen size and DPI you may want to change the font size.
        rs.setModifierFont("Arial", Typeface.BOLD, DEFAULT_FONT_SIZE);
        rs.setMPModifierFont("Arial", Typeface.BOLD, DEFAULT_FONT_SIZE);

        // Configure modifier text output
        rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE);
        rs.setTextOutlineWidth(TEXT_OUTLINE_WIDTH);  // 4 is the factory default

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

        // Generate a cache key for this symbol
        String symbolKey = symbolCode
            + (modifiers == null ? emptyArray.toString() : modifiers.toString())
            + (attributes == null ? emptyArray.toString() : attributes.toString());

        // Look for an attribute bundle in our cache and determine if the cached reference is valid
        WeakReference<PlacemarkAttributes> reference = symbolCache.get(symbolKey);
        PlacemarkAttributes placemarkAttributes = (reference == null ? null : reference.get());

        // Create the attributes if they haven't been created yet or if they've been released
        if (placemarkAttributes == null) {

            // Create the attributes bundle and add it to the cache.
            // The actual bitmap will be lazily (re)created using a factory.
            placemarkAttributes = MilStd2525.createPlacemarkAttributes(symbolCode, modifiers, attributes);
            if (placemarkAttributes == null) {
                throw new IllegalArgumentException("Cannot generate a symbol for: " + symbolKey);
            }
            // Add a weak reference to the attribute bundle to our cache
            symbolCache.put(symbolKey, new WeakReference<>(placemarkAttributes));

            // Perform some initialization of the bundle conducive to eye distance scaling
            placemarkAttributes.setMinimumImageScale(MINIMUM_IMAGE_SCALE);
        }

        return placemarkAttributes;
    }

    /**
     * Creates a placemark attributes bundle containing a MIL-STD-2525 symbol using the specified modifiers and
     * attributes.  The ImageSource bitmap is lazily created via an ImageSource.Bitmap factory. The call to the
     * factory's createBitmap method made when Placemark comes into view; it's also used to recreate the bitmap if the
     * resource was evicted from the World Wind render resource cache.
     *
     * @param symbolCode The 15-character SIDC (symbol identification coding scheme) code.
     * @param modifiers  The ModifierUnit (unit) or ModifierTG (tactical graphic) modifiers collection. May be null.
     * @param attributes The MilStdAttributes attributes collection. May be null.
     *
     * @return A new PlacemarkAttributes bundle representing the MIL-STD-2525 symbol.
     */
    public static PlacemarkAttributes createPlacemarkAttributes(String symbolCode, SparseArray<String> modifiers, SparseArray<String> attributes) {
        PlacemarkAttributes placemarkAttributes = new PlacemarkAttributes();

        // Create a BitmapFactory instance with the values needed to create and recreate the symbol's bitmap
        SymbolBitmapFactory factory = new SymbolBitmapFactory(symbolCode, modifiers, attributes, placemarkAttributes);
        placemarkAttributes.setImageSource(ImageSource.fromBitmapFactory(factory));

        return placemarkAttributes;
    }

    /**
     * Creates an MIL-STD-2525 symbol from the specified symbol code, modifiers and attributes.
     *
     * @param symbolCode The MIL-STD-2525 symbol code.
     * @param modifiers  The MIL-STD-2525 modifiers. If null, a default (empty) modifier list will be used.
     * @param attributes The MIL-STD-2525 attributes. If null, a default (empty) attribute list will be used.
     *
     * @return An ImageInfo object containing the symbol's bitmap and meta data; may be null
     */
    public static ImageInfo renderImage(String symbolCode, SparseArray<String> modifiers, SparseArray<String> attributes) {
        if (!initialized) {
            throw new IllegalStateException(
                Logger.logMessage(Logger.ERROR, "MilStd2525", "renderImage", "renderer has not been initialized."));
        }
        SparseArray<String> unitModifiers = modifiers == null ? new SparseArray<String>() : modifiers;
        SparseArray<String> renderAttributes = attributes == null ? new SparseArray<String>() : attributes;
        if (!renderer.CanRender(symbolCode, unitModifiers, renderAttributes)) {
            return null;
        }
        return renderer.RenderIcon(symbolCode, unitModifiers, renderAttributes);
    }

    /**
     * This ImageSource.BitmapFactory implementation creates MIL-STD-2525 bitmaps for use with MilStd2525Placemark.
     */
    protected static class SymbolBitmapFactory implements ImageSource.BitmapFactory {

        private final String symbolCode;

        private final SparseArray<String> modifiers;

        private final SparseArray<String> attributes;

        private final PlacemarkAttributes placemarkAttributes;

        private Offset placemarkOffset;

        /**
         * Constructs a SymbolBitmapFactory instance capable of creating a bitmap with the given code, modifiers and
         * attributes. The createBitmap() method will return a new instance of a bitmap and will also update the
         * associated placemarkAttributes bundle's imageOffset property based on the size of the new bitmap.
         *
         * @param symbolCode          SIDC code
         * @param modifiers           Unit modifiers to be copied; null is permitted
         * @param attributes          Rendering attributes to be copied; null is permitted
         * @param placemarkAttributes Placemark attribute bundle associated with this factory
         */
        public SymbolBitmapFactory(String symbolCode, SparseArray<String> modifiers, SparseArray<String> attributes, PlacemarkAttributes placemarkAttributes) {
            // Capture the values needed to (re)create the symbol bitmap
            this.symbolCode = symbolCode;
            this.modifiers = modifiers != null ? modifiers.clone() : null;
            this.attributes = attributes != null ? attributes.clone() : null;
            // The MilStd2525.symbolCache maintains a WeakReference to the placemark attributes. The finalizer is able to
            // resolve the circular dependency between the PlacemarkAttributes->ImageSource->Factory->PlacemarkAttributes
            // and garbage collect the attributes a Placemark releases its attribute bundle (e.g., when switching
            // between levels-of-detail)
            this.placemarkAttributes = placemarkAttributes;
        }

        /**
         * Returns the MIL-STD-2525 bitmap and updates the PlacemarkAttributes associated with this factory instance.
         *
         * @return a new bitmap rendered from the parameters given in the constructor; may be null
         */
        @Override
        public Bitmap createBitmap() {
            // Create the symbol's bitmap
            ImageInfo imageInfo = MilStd2525.renderImage(this.symbolCode, this.modifiers, this.attributes);
            if (imageInfo == null) {
                Logger.logMessage(Logger.ERROR, "MilStd2525", "createBitmap", "Failed to render image for " + this.symbolCode);
                // TODO: File JIRA issue - must return a valid bitmap, else the ImageRetriever repeatedly attempts to create the bitmap.
                return defaultImage;
            }

            // Apply the computed image offset after the renderer has created the image. This is essential for proper
            // placement as the offset may change depending on the level of detail, for instance, the absence or
            // presence of text modifiers.
            Point centerPoint = imageInfo.getCenterPoint(); // The center of the core symbol
            Rect bounds = imageInfo.getImageBounds();       // The extents of the image, including text modifiers
            this.placemarkOffset = new Offset(
                WorldWind.OFFSET_PIXELS, centerPoint.x, // x offset
                WorldWind.OFFSET_PIXELS, bounds.height() - centerPoint.y); // y offset converted to lower-left origin

            // Apply the placemark offset to the attributes on the main thread. This is necessary to synchronize write
            // access to placemarkAttributes from the thread that invokes this BitmapFactory and read access from the
            // main thread.
            mainLoopHandler.post(new Runnable() {
                @Override
                public void run() {
                    placemarkAttributes.setImageOffset(placemarkOffset);
                }
            });

            // Return the bitmap
            return imageInfo.getImage();
        }
    }
}
