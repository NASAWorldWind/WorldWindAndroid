/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.milstd2525;

import android.graphics.Point;
import android.util.SparseArray;

import armyc2.c2sd.renderer.utilities.ImageInfo;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;

public class MilStd2525Placemark extends Placemark {


    public static MilStd2525Placemark fromSymbolCode(Position position, String symbolCode, SparseArray<String> modifiers) {

        ImageInfo imageInfo = MilStd2525Renderer.renderImage(symbolCode, modifiers);

        // TODO: Interrogate the imageInfo meta data to determine the proper imageOffset.
        Point centerPoint = imageInfo.getCenterPoint();
        PlacemarkAttributes attr;
        if (position.altitude > 0) {
            attr = PlacemarkAttributes.withImageAndLeaderLine(ImageSource.fromBitmap(imageInfo.getImage())).setImageOffset(Offset.bottomCenter());
        } else {
            attr = PlacemarkAttributes.withImage(ImageSource.fromBitmap(imageInfo.getImage())).setImageOffset(Offset.bottomCenter());
        }

        return new MilStd2525Placemark(position, attr, null);
    }

    public static MilStd2525Placemark fromImageInfo(Position position, ImageInfo imageInfo) {

        // TODO: Interrogate the imageInfo meta data to determine the proper imageOffset.
        Point centerPoint = imageInfo.getCenterPoint();
        PlacemarkAttributes attr;
        if (position.altitude > 0) {
            attr = PlacemarkAttributes.withImageAndLeaderLine(ImageSource.fromBitmap(imageInfo.getImage())).setImageOffset(Offset.bottomCenter());
        } else {
            attr = PlacemarkAttributes.withImage(ImageSource.fromBitmap(imageInfo.getImage())).setImageOffset(Offset.bottomCenter());
        }

        return new MilStd2525Placemark(position, attr, null);
    }

    protected MilStd2525Placemark(Position position, PlacemarkAttributes attributes, String displayName) {
        super(position, attributes, displayName);
    }

}
