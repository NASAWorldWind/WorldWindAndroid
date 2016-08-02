/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Path;
import gov.nasa.worldwind.shape.ShapeAttributes;

public class PathsActivity extends BasicGlobeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setAboutBoxTitle("About " + this.getResources().getText(R.string.title_paths));
        this.setAboutBoxText("Demonstrates how to use the Path shape.");

        // Create a layer to display the tutorial paths.
        RenderableLayer layer = new RenderableLayer();
        this.wwd.getLayers().addLayer(layer);

        // Create a basic path with the default attributes, the default altitude mode (ABSOLUTE),
        // and the default path type (GREAT_CIRCLE).
        List<Position> positions = Arrays.asList(
            Position.fromDegrees(50, -180, 1e5),
            Position.fromDegrees(30, -100, 1e6),
            Position.fromDegrees(50, -40, 1e5)
        );
        Path path = new Path(positions);
        layer.addRenderable(path);

        // Create a basic path with the default attributes, the CLAMP_TO_GROUND altitude mode,
        // and the default path type (GREAT_CIRCLE).
        positions = Arrays.asList(
            Position.fromDegrees(40, -180, 0),
            Position.fromDegrees(20, -100, 0),
            Position.fromDegrees(40, -40, 0)
        );
        path = new Path(positions);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        layer.addRenderable(path);

        // Create an extruded path with the default attributes, the default altitude mode (ABSOLUTE),
        // and the default path type (GREAT_CIRCLE).
        positions = Arrays.asList(
            Position.fromDegrees(30, -180, 1e5),
            Position.fromDegrees(10, -100, 1e6),
            Position.fromDegrees(30, -40, 1e5)
        );
        path = new Path(positions);
        path.setExtrude(true); // extrude the path from the ground to each path position's altitude
        layer.addRenderable(path);

        // Create an extruded path with custom attributes that display the extruded vertical lines,
        // make the extruded interior 50% transparent, and increase the path line with.
        positions = Arrays.asList(
            Position.fromDegrees(20, -180, 1e5),
            Position.fromDegrees(0, -100, 1e6),
            Position.fromDegrees(20, -40, 1e5)
        );
        ShapeAttributes attrs = new ShapeAttributes();
        attrs.setDrawVerticals(true); // display the extruded verticals
        attrs.setInteriorColor(new Color(1, 1, 1, 0.5f)); // 50% transparent white
        attrs.setOutlineWidth(3);
        path = new Path(positions, attrs);
        path.setExtrude(true); // extrude the path from the ground to each path position's altitude
        layer.addRenderable(path);
    }
}
