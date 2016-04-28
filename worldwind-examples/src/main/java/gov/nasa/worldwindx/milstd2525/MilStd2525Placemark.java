/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.milstd2525;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.SparseArray;

import armyc2.c2sd.renderer.utilities.ImageInfo;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

public class MilStd2525Placemark extends Placemark {

    private static SparseArray<String> defaultAttributes = new SparseArray<>();

    public static MilStd2525Placemark fromSymbolCode(Position position, String symbolCode, SparseArray<String> modifiers) {
        ImageInfo imageInfo = MilStd2525Renderer.renderImage(symbolCode, modifiers, defaultAttributes);
        return fromImageInfo(position, imageInfo);
    }

    public static MilStd2525Placemark fromImageInfo(Position position, ImageInfo imageInfo) {

        Rect imageBounds = imageInfo.getImageBounds();      // The bounds of the entire image, including text
        Rect symbolBounds = imageInfo.getSymbolBounds();    // The bounds of the core symbol
        Point centerPoint = imageInfo.getCenterPoint();     // The center of the core symbol

        PlacemarkAttributes attr;
        if (position.altitude > 0) {
            attr = PlacemarkAttributes.withImageAndLeaderLine(ImageSource.fromBitmap(imageInfo.getImage()));
        } else {
            attr = PlacemarkAttributes.withImage(ImageSource.fromBitmap(imageInfo.getImage()));
        }

        // Place the bottom of the image at the specified position and
        // anchor it horizontally at the center of the core symbol.
        Offset imageOffset = new Offset(
            WorldWind.OFFSET_FRACTION, ((double) centerPoint.x) / imageBounds.width(), // x offset
            WorldWind.OFFSET_FRACTION, 0.0); // y offset
        attr.setImageOffset(imageOffset);

        return new MilStd2525Placemark(position, attr, null);
    }

    protected MilStd2525Placemark(Position position, PlacemarkAttributes attributes, String displayName) {
        super(position, attributes, displayName);
    }

}
