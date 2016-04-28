/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.milstd2525;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.RendererSettings;

public class MilStd2525Renderer {

    private static MilStdIconRenderer renderer = MilStdIconRenderer.getInstance();

    private static boolean initialized = false;

    private static SparseArray<String> attributes = new SparseArray<String>();

    public static void initialize(Context applicationContext) {
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

        rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE);
        //rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE_QUICK);
        //rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_COLORFILL);
        //rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_NONE);

        attributes.put(MilStdAttributes.SymbologyStandard, "0");

        initialized = true;
    }


    public static ImageInfo renderImage(String symbolCode, SparseArray<String> modifiers) {
        return renderer.RenderIcon(symbolCode, modifiers, attributes);
    }
}
