/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Path;
import gov.nasa.worldwind.shape.ShapeAttributes;

public class PathsFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow (GLSurfaceView) object with a set of Path shapes
     *
     * @return The WorldWindow object containing the globe.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Create a layer to display the tutorial paths.
        RenderableLayer layer = new RenderableLayer();
        wwd.getLayers().addLayer(layer);

        // Create a basic path with the default attributes, the default altitude mode (ABSOLUTE),
        // and the default path type (GREAT_CIRCLE).
        List<Position> positions = Arrays.asList(
            Position.fromDegrees(50, -180, 1e5),
            Position.fromDegrees(30, -100, 1e6),
            Position.fromDegrees(50, -40, 1e5)
        );
        Path path = new Path(positions);
        layer.addRenderable(path);

        // Create a terrain following path with the default attributes, and the default path type (GREAT_CIRCLE).
        positions = Arrays.asList(
            Position.fromDegrees(40, -180, 0),
            Position.fromDegrees(20, -100, 0),
            Position.fromDegrees(40, -40, 0)
        );
        path = new Path(positions);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND); // clamp the path vertices to the ground
        path.setFollowTerrain(true); // follow the ground between path vertices
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

        return wwd;
    }
}
